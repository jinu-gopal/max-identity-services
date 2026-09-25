package com.max.erp.identity.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class AuthorizationServerConfig {

    private static final Path KEY_DIRECTORY =
            Paths.get(".runtime", "keys");

    private static final Path PRIVATE_KEY_FILE =
            KEY_DIRECTORY.resolve("max-identity-private.key");

    private static final Path PUBLIC_KEY_FILE =
            KEY_DIRECTORY.resolve("max-identity-public.key");

    private static final String KEY_ID =
            "max-identity-rsa-1";

    @Bean
    public JWKSource<SecurityContext> jwkSource() {

        KeyPair keyPair = loadOrGenerateRsaKey();

        RSAPublicKey publicKey =
                (RSAPublicKey) keyPair.getPublic();

        RSAPrivateKey privateKey =
                (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(KEY_ID)
                .algorithm(JWSAlgorithm.RS256)
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);

        return (jwkSelector, securityContext) ->
                jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(
            JWKSource<SecurityContext> jwkSource) {

        return OAuth2AuthorizationServerConfiguration
                .jwtDecoder(jwkSource);
    }

    private KeyPair loadOrGenerateRsaKey() {

        try {

            if (Files.exists(PRIVATE_KEY_FILE)
                    && Files.exists(PUBLIC_KEY_FILE)) {

                return loadExistingKeyPair();
            }

            Files.createDirectories(KEY_DIRECTORY);

            KeyPairGenerator keyPairGenerator =
                    KeyPairGenerator.getInstance("RSA");

            keyPairGenerator.initialize(2048);

            KeyPair keyPair =
                    keyPairGenerator.generateKeyPair();

            saveKeyPair(keyPair);

            return keyPair;

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Unable to load or generate persistent RSA key pair",
                    ex
            );
        }
    }

    private KeyPair loadExistingKeyPair()
            throws Exception {

        byte[] privateKeyBytes =
                readPemFile(PRIVATE_KEY_FILE, "PRIVATE KEY");

        byte[] publicKeyBytes =
                readPemFile(PUBLIC_KEY_FILE, "PUBLIC KEY");

        KeyFactory keyFactory =
                KeyFactory.getInstance("RSA");

        PrivateKey privateKey =
                keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(privateKeyBytes)
                );

        PublicKey publicKey =
                keyFactory.generatePublic(
                        new X509EncodedKeySpec(publicKeyBytes)
                );

        return new KeyPair(publicKey, privateKey);
    }

    private void saveKeyPair(KeyPair keyPair)
            throws Exception {

        Files.createDirectories(KEY_DIRECTORY);

        String privateKeyPem =
                toPem(
                        "PRIVATE KEY",
                        keyPair.getPrivate().getEncoded()
                );

        String publicKeyPem =
                toPem(
                        "PUBLIC KEY",
                        keyPair.getPublic().getEncoded()
                );

        Files.writeString(
                PRIVATE_KEY_FILE,
                privateKeyPem,
                StandardCharsets.UTF_8
        );

        Files.writeString(
                PUBLIC_KEY_FILE,
                publicKeyPem,
                StandardCharsets.UTF_8
        );
    }

    private byte[] readPemFile(
            Path file,
            String type)
            throws Exception {

        String pem =
                Files.readString(
                        file,
                        StandardCharsets.UTF_8
                );

        String base64 =
                pem
                        .replace(
                                "-----BEGIN " + type + "-----",
                                ""
                        )
                        .replace(
                                "-----END " + type + "-----",
                                ""
                        )
                        .replaceAll("\\s", "");

        return Base64.getDecoder().decode(base64);
    }

    private String toPem(
            String type,
            byte[] encoded) {

        String base64 =
                Base64.getMimeEncoder(
                        64,
                        "\n".getBytes(StandardCharsets.UTF_8)
                ).encodeToString(encoded);

        return "-----BEGIN " + type + "-----\n"
                + base64
                + "\n-----END " + type + "-----\n";
    }
}