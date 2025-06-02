package org.ms.authentificationservice.services;

import org.ms.authentificationservice.entities.AppRole;
import org.ms.authentificationservice.entities.AppUser;
import java.util.List;

public interface UserService {
    AppUser addUser(AppUser appUser);
    AppRole addRole(AppRole appRole);
    void addRoleToUser(String username, String roleName);
    AppUser getUserByName(String username);
    List<AppUser> getAllUsers();
}
