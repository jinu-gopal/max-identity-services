package com.max.erp.identity.service;

import com.max.erp.identity.entity.Organization;
import com.max.erp.identity.entity.Tenant;
import com.max.erp.identity.entity.User;
import com.max.erp.identity.entity.UserTenantAccess;
import com.max.erp.identity.repository.OrganizationRepository;
import com.max.erp.identity.repository.TenantRepository;
import com.max.erp.identity.repository.UserRepository;
import com.max.erp.identity.repository.UserTenantAccessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserTenantAccessService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final OrganizationRepository organizationRepository;
    private final UserTenantAccessRepository accessRepository;

    public UserTenantAccessService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            OrganizationRepository organizationRepository,
            UserTenantAccessRepository accessRepository) {

        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.organizationRepository = organizationRepository;
        this.accessRepository = accessRepository;
    }

    @Transactional(readOnly = true)
    public List<UserTenantAccess> getUserAccess(UUID userId) {

        getUser(userId);

        return accessRepository
                .findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean hasTenantAccess(
            UUID userId,
            UUID tenantId) {

        return accessRepository
                .existsByUserIdAndTenantIdAndStatus(
                        userId,
                        tenantId,
                        "ACTIVE"
                );
    }

    public UserTenantAccess grantAccess(
            UUID userId,
            UUID tenantId,
            UUID organizationId) {

        User user = getUser(userId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tenant not found"
                        ));

        validateTenantActive(tenant);

        if (organizationId != null) {

            Organization organization =
                    organizationRepository
                            .findById(organizationId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Organization not found"
                                    ));

            if (!organization.getTenant().getId()
                    .equals(tenantId)) {

                throw new IllegalArgumentException(
                        "Organization does not belong to tenant"
                );
            }

            if (!"ACTIVE".equalsIgnoreCase(
                    organization.getStatus())) {

                throw new IllegalArgumentException(
                        "Organization is not active"
                );
            }
        }

        if (accessRepository
                .existsByUserIdAndTenantIdAndOrganizationId(
                        userId,
                        tenantId,
                        organizationId
                )) {

            throw new IllegalArgumentException(
                    "User access already exists"
            );
        }

        UserTenantAccess access =
                new UserTenantAccess();

        access.setUser(user);
        access.setTenant(tenant);
        access.setOrganizationId(organizationId);
        access.setStatus("ACTIVE");

        return accessRepository.save(access);
    }

    public void revokeAccess(
            UUID userId,
            UUID tenantId,
            UUID organizationId) {

        UserTenantAccess access =
                accessRepository
                        .findByUserIdAndTenantIdAndOrganizationId(
                                userId,
                                tenantId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User access not found"
                                ));

        access.setStatus("INACTIVE");

        accessRepository.save(access);
    }

    private User getUser(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        ));
    }

    private void validateTenantActive(Tenant tenant) {

        if (!"ACTIVE".equalsIgnoreCase(
                tenant.getStatus())) {

            throw new IllegalArgumentException(
                    "Tenant is not active"
            );
        }
    }
}