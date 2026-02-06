/*
 * Krista Managed Cases Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.krista.case_orchestration.krista_managed_cases;

import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import app.krista.extension.util.InvokerAttributeProvider;
import app.krista.ksdk.authorization.Role;
import app.krista.ksdk.authorization.RoleProvider;
import org.jvnet.hk2.annotations.Service;

@Service
public class KristaManagedQueuesAttributes {

    private final InvokerAttributeProvider<String> invokerAttributeProvider;
    private final RoleProvider roleProvider;
    private List<Role> roles;

    @Inject
    public KristaManagedQueuesAttributes(
            @Named(KristaManagedQueuesExtension.ROLES) InvokerAttributeProvider<String> invokerAttributeProvider,
            RoleProvider roleProvider) {
        this.invokerAttributeProvider = invokerAttributeProvider;
        this.roleProvider = roleProvider;
    }

    public List<Role> getRoles() {
        String extensionRoles = invokerAttributeProvider.get().trim();
        List<String> myRoles = Arrays.stream(extensionRoles.split(",")).collect(Collectors.toList());

        if (roles == null) {
            loadRoles(myRoles);
        }
        List<String> rolesName = roles.stream().map(Role::getName).collect(Collectors.toList());
        if (myRoles.stream().anyMatch(role -> !rolesName.contains(role))) {
            loadRoles(myRoles);
        }
        return roles;
    }

    private void loadRoles(List<String> myRoles) {
        List<Role> roleList = new ArrayList<>();
        Set<Role> presentRoles = new HashSet<>();
        for (String myRole : myRoles) {
            String roleName = myRole.strip();
	            Role role = roleProvider.lookupRole(roleName);
	            if (role == null) {
	                throw new IllegalStateException(
	                        "Role name does not exist in workspace: " + roleName);
	            }
	            if (presentRoles.contains(role)) {
	                continue;
	            }
	            roleList.add(role);
	            presentRoles.add(role);
        }
        roles = roleList;
    }

}
