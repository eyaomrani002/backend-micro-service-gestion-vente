package org.ms.authentificationservice.filtres;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        setFilterProcessesUrl("/auth/login"); // Set custom login URL
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("attemptAuthentication for " + request.getServletPath());
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        System.out.println("Username: " + username + ", Password: " + password);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("successfulAuthentication");
        User user = (User) authResult.getPrincipal();
        String[] roles = new String[user.getAuthorities().size()];
        int index = 0;
        for (GrantedAuthority gi : user.getAuthorities()) {
            roles[index] = gi.toString();
            index++;
        }
        Algorithm algo = Algorithm.HMAC256("MaClé");
        String jwtAccessToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000)) // Increased to 10 minutes for testing
                .withIssuer(request.getRequestURL().toString())
                .withArrayClaim("roles", roles)
                .sign(algo);
        String jwtRefreshToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .withIssuer(request.getRequestURL().toString())
                .sign(algo);
        Map<String, String> mapTokens = new HashMap<>();
        mapTokens.put("access-token", jwtAccessToken);
        mapTokens.put("refresh-token", jwtRefreshToken);
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), mapTokens);
    }
}