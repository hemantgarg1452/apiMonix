package com.apimonix.config;

import com.apimonix.model.User;
import com.apimonix.service.UserService;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupabaseJwtFilter extends OncePerRequestFilter {

    private final UserService userService;

    @Value("${apimonix.supabase.url}")
    private String supabaseUrl;
    private JWKSet jwkSet;
    private JWKSet getJwkSet() throws Exception {
        if (jwkSet == null) {
            String jwksUrl = supabaseUrl + "/auth/v1/.well-known/jwks.json";
            log.info("Fetching JWKS from: {}", jwksUrl);
            jwkSet = JWKSet.load(new URL(jwksUrl));
        }
        return jwkSet;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String kid = signedJWT.getHeader().getKeyID();

            JWKSet jwks = getJwkSet();
            ECKey ecKey = (ECKey) jwks.getKeyByKeyId(kid);

            if (ecKey == null) {
                log.warn("No matching key found for kid: {} — refreshing JWKS", kid);
                jwkSet = null; // cache clear
                ecKey = (ECKey) getJwkSet().getKeyByKeyId(kid);
            }

            if (ecKey == null) {
                log.warn("JWT key not found even after refresh");
                filterChain.doFilter(request, response);
                return;
            }

            com.nimbusds.jose.crypto.ECDSAVerifier verifier =
                    new com.nimbusds.jose.crypto.ECDSAVerifier(ecKey);

            if (!signedJWT.verify(verifier)) {
                log.warn("JWT signature verification failed");
                filterChain.doFilter(request, response);
                return;
            }

            com.nimbusds.jwt.JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            if (claims.getExpirationTime() != null &&
                    claims.getExpirationTime().before(new java.util.Date())) {
                log.warn("JWT token has expired");
                filterChain.doFilter(request, response);
                return;
            }

            String userId = claims.getSubject();
            String email = (String) claims.getClaim("email");

            if (userId == null) {
                log.warn("JWT has no subject");
                filterChain.doFilter(request, response);
                return;
            }
            User dbUser = userService.getOrCreateUser(
                    UUID.fromString(userId), email
            );

            AuthUser authUser = new AuthUser(
                    UUID.fromString(userId),
                    email,
                    dbUser.getPlan()
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authUser,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );


            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.warn("JWT verification failed: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/status/");
    }
}
