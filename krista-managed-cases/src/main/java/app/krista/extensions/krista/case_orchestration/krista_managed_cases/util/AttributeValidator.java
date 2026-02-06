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

package app.krista.extensions.krista.case_orchestration.krista_managed_cases.util;

import app.krista.ksdk.authorization.Role;
import app.krista.ksdk.authorization.RoleProvider;

import java.util.ArrayList;
import java.util.List;

public class AttributeValidator {

    public static void validateRoles(String roles, RoleProvider roleProvider) {

        if (roles == null) {
            throw new IllegalStateException("Roles configuration must not be null");
        }

        String[] myRoles = roles.split(",");
        List<String> invalidRoles = new ArrayList<>();
        for (String myRole : myRoles) {
            String roleName = myRole == null ? "" : myRole.strip();
            Role role = roleProvider.lookupRole(roleName);
            if (role == null) {
                invalidRoles.add(myRole);
            }
        }
        if (!invalidRoles.isEmpty()) {
            throw new IllegalStateException("Role name(s) do not exist in workspace: " + invalidRoles);
        }
    }
}