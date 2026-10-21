package com.max.erp.identity.validation.validator;

import com.max.erp.identity.entity.BusinessObjectField;
import com.max.erp.identity.entity.User;
import com.max.erp.identity.repository.UserRepository;
import com.max.erp.identity.validation.ValidationContext;
import com.max.erp.identity.validation.ValidationError;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@Order(70)
public class UserFieldValidator implements RecordValidator {

    private final UserRepository userRepository;

    public UserFieldValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<ValidationError> validate(ValidationContext context) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, Object> values = context.values();

        if (values == null || context.fields() == null) {
            return errors;
        }

        Map<BusinessObjectField, UUID> pendingUserValidations =
                new LinkedHashMap<>();
        Set<UUID> allUserIds = new LinkedHashSet<>();

        for (BusinessObjectField field : context.fields()) {
            String fieldType = normalizeFieldType(field.getFieldType());

            if (!"USER".equals(fieldType)) {
                continue;
            }

            Object value = values.get(field.getFieldKey());

            if (value == null) {
                continue;
            }

            if (value instanceof String stringValue && stringValue.isBlank()) {
                continue;
            }

            UUID userId = extractUserId(value);

            if (userId == null) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "INVALID_USER",
                        "Invalid user identifier for field " + field.getFieldKey()
                ));
                continue;
            }

            pendingUserValidations.put(field, userId);
            allUserIds.add(userId);
        }

        if (pendingUserValidations.isEmpty()) {
            return errors;
        }

        Map<UUID, User> usersById = new HashMap<>();

        if (!allUserIds.isEmpty()) {
            List<User> users = userRepository.findAllById(allUserIds);

            if (users != null) {
                for (User user : users) {
                    if (user != null && user.getId() != null) {
                        usersById.put(user.getId(), user);
                    }
                }
            }
        }

        for (Map.Entry<BusinessObjectField, UUID> entry :
                pendingUserValidations.entrySet()) {
            BusinessObjectField field = entry.getKey();
            UUID targetUserId = entry.getValue();

            User user = usersById.get(targetUserId);

            if (user == null) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "INVALID_USER",
                        "Referenced user does not exist for field " + field.getFieldKey()
                ));
                continue;
            }

            if (user.getStatus() == null
                    || !"ACTIVE".equalsIgnoreCase(user.getStatus().trim())) {
                errors.add(new ValidationError(
                        field.getFieldKey(),
                        "INVALID_USER",
                        "Referenced user is not active for field " + field.getFieldKey()
                ));
            }
        }

        return errors;
    }

    private UUID extractUserId(Object value) {
        if (value instanceof UUID singleUuid) {
            return singleUuid;
        }

        if (value instanceof String singleString) {
            if (singleString.isBlank()) {
                return null;
            }

            try {
                return UUID.fromString(singleString.trim());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        return null;
    }

    private String normalizeFieldType(String fieldType) {
        return fieldType == null
                ? ""
                : fieldType.trim().toUpperCase(Locale.ROOT);
    }
}
