package org.ms.authentificationservice.web;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.ms.authentificationservice.dto.UserRoleData;
import org.ms.authentificationservice.entities.AppRole;
import org.ms.authentificationservice.entities.AppUser;
import org.ms.authentificationservice.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserServiceREST {
    private final UserService userService;

    public UserServiceREST(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    // @PostAuthorize("hasAuthority('USER')")
    public List<AppUser> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    // @PostAuthorize("hasAuthority('ADMIN')")
    public AppUser addUser(@RequestBody AppUser appUser) {
        return userService.addUser(appUser);
    }

    @PostMapping("/roles")
    public AppRole addRole(@RequestBody AppRole appRole) {
        return userService.addRole(appRole);
    }

    @PostMapping("/addRoleToUser")
    public void addRoleToUser(@RequestBody UserRoleData userRoleData) {
        userService.addRoleToUser(userRoleData.getUsername(), userRoleData.getRoleName());
    }

    public final String PREFIXE_JWT = "Bearer ";
    public final String CLE_SIGNATURE = "MaClé";

    @GetMapping(path="/refreshToken")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = request.getHeader("Authorization");
        if (refreshToken != null && refreshToken.startsWith(PREFIXE_JWT)) {
            try {
                String jwtRefresh = refreshToken.substring(PREFIXE_JWT.length());
                Algorithm algo = Algorithm.HMAC256(CLE_SIGNATURE);
                JWTVerifier jwtVerifier = JWT.require(algo).build();
                DecodedJWT decodedJWT = jwtVerifier.verify(jwtRefresh);
                String username = decodedJWT.getSubject();
                AppUser user = userService.getUserByName(username);
                String[] roles = new String[user.getRoles().size()];
                int index = 0;
                for (AppRole r : user.getRoles()) {
                    roles[index] = r.getRoleName();
                    index++;
                }
                String jwtAccessToken = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + 1 * 60 * 1000))
                        .withIssuer(request.getRequestURL().toString())
                        .withArrayClaim("roles", roles)
                        .sign(algo);
                Map<String, String> mapTokens = new HashMap<>();
                mapTokens.put("access-token", jwtAccessToken);
                mapTokens.put("refresh-token", jwtRefresh);
                response.setContentType("application/json");
                new ObjectMapper().writeValue(response.getOutputStream(), mapTokens);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Refresh Token non disponible..");
        }
    }
}
