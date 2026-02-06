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

import java.util.Map;
import javax.inject.Inject;

import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.impl.anno.*;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.util.AttributeValidator;
import app.krista.ksdk.authorization.RoleProvider;

@Java(version = Java.Version.JAVA_21)
@Extension(name = "Krista Managed Queue", version = "3.2.4", description = "Queue manager based on roles", requireWorkspaceAdminRights = true)
@Field(name = KristaManagedQueuesExtension.ROLES, type = "Text")
@StaticResource(path = "docs", file = "docs")
public class KristaManagedQueuesExtension {

    private final RoleProvider roleProvider;

    public static final String ROLES = "roles_separated_by_comma";

    @Inject
    public KristaManagedQueuesExtension(RoleProvider roleProvider) {
        this.roleProvider = roleProvider;
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Documentation", "static/docs");
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validate(Map<String, Object> attributes) {
            String roles = attributes == null ? null : (String) attributes.get(ROLES);
            AttributeValidator.validateRoles(roles, roleProvider);
    }

}
