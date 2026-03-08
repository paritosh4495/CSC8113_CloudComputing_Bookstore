package com.group1.catalogservice;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.Date;
import java.util.List;
import java.util.Map;

@TestConfiguration
public class SecurityTestConfig {
    private static final RSAKey TEST_RSA_KEY;

    static {
        try {
            TEST_RSA_KEY = new RSAKeyGenerator(2048).keyID("test-key").generate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() throws Exception {
        return NimbusJwtDecoder.withPublicKey(TEST_RSA_KEY.toRSAPublicKey()).build();
    }

    /**
     * Generates a token with realm_access.roles = ["admin"]
     * KeycloakRoleConverter maps this → ROLE_admin
     * hasRole("admin") in SecurityConfig then passes
     */
    public static String adminToken() {
        return generateToken("admin-user", List.of("admin"));
    }

    /**
     * Generates a token with realm_access.roles = ["user"]
     * No "admin" role → hasRole("admin") fails → 403
     */
    public static String userToken() {
        return generateToken("regular-user", List.of("user"));
    }

    /**
     * Generates a valid token with no special roles.
     * Passes authenticated() checks — used for reserve/release endpoints.
     */
    public static String serviceToken() {
        return generateToken("cart-service", List.of());
    }

    private static String generateToken(String username, List<String> roles) {
        try {
            JWSSigner signer = new RSASSASigner(TEST_RSA_KEY);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("preferred_username", username)
                    // This is exactly what KeycloakRoleConverter reads
                    .claim("realm_access", Map.of("roles", roles))
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + 3_600_000))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("test-key").build(),
                    claims
            );
            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
