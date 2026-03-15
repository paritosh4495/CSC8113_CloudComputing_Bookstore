package com.group1.cartservice;

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


@TestConfiguration
public class SecurityTestConfig {

    private static final RSAKey TEST_RSA_KEY ;

    static {
        try {
            TEST_RSA_KEY = new RSAKeyGenerator(2048)
                    .keyID("test-key-id")
                    .generate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate test RSA key pair", e);
        }
    }

    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() throws Exception {
        return NimbusJwtDecoder
                .withPublicKey(TEST_RSA_KEY.toRSAPublicKey())
                .build();
    }

    public static String generateToken(String username) {
        try {
            JWSSigner signer = new RSASSASigner(TEST_RSA_KEY);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("preferred_username", username)  // must match setPrincipalClaimName()
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + 3_600_000)) // 1 hour
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID("test-key-id")
                            .build(),
                    claims
            );

            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate test JWT for user: " + username, e);
        }
    }





}
