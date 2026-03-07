package com.group1.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class SecurityHeaderFilter  implements GlobalFilter, Ordered {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth ->
                {
                    // 1. Extract the Username
                    String username = jwtAuth.getToken().getClaimAsString("preferred_username");

                    // 2. Extract the Role from Keycloak's "realm_access"
                    String roleHeader = "USER"; // Default fallback
                    Map<String, Object> realmAccess = jwtAuth.getToken().getClaimAsMap("realm_access");
                    if (realmAccess != null && realmAccess.containsKey("roles")) {
                        List<String> roles = (List<String>) realmAccess.get("roles");
                        if (roles.contains("admin")) {
                            roleHeader = "ADMIN";
                        }
                    }

                    // 3. Inject BOTH headers for your backend controllers
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", username)
                            .header("X-User-Role", roleHeader)
                            .build();

                    return exchange.mutate().request(mutatedRequest).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        return -1; // Ensure this filter runs before the authentication filter
    }
}
