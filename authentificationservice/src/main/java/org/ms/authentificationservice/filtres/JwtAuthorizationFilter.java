package org.ms.authentificationservice.filtres;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class JwtAuthorizationFilter extends OncePerRequestFilter {
    public final String PREFIXE_JWT = "Bearer ";
    public final String CLE_SIGNATURE = "MaClé";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        if (path.equals("/auth/login") || path.equals("/users/refreshToken")) {
            System.out.println("Skipping JWT validation for " + path);
            filterChain.doFilter(request, response);
            return;
        }
        String authorizationToken = request.getHeader("Authorization");
        if (authorizationToken != null && authorizationToken.startsWith(PREFIXE_JWT)) {
            try {
                String jwt = authorizationToken.substring(PREFIXE_JWT.length());
                Algorithm algo = Algorithm.HMAC256(CLE_SIGNATURE);
                JWTVerifier jwtVerifier = JWT.require(algo).build();
                DecodedJWT decodedJWT = jwtVerifier.verify(jwt);
                String username = decodedJWT.getSubject();
                String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                Collection<GrantedAuthority> permissions = new ArrayList<>();
                for (String r : roles) {
                    permissions.add(new SimpleGrantedAuthority(r));
                }
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, permissions);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                response.setHeader("error-message", e.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}