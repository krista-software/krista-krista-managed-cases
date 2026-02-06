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

package app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.impl;

import java.util.List;

public final class IdLists {

    private IdLists() {
    }

    public static List<String> getPaginatedList(List<String> idList, String offset, int pageSize) {
        if (idList == null || idList.isEmpty() || pageSize == 0) {
            return List.of();
        }
        final boolean hasOffset = offset != null && !offset.isEmpty();
        if (!hasOffset && Math.abs(pageSize) >= idList.size()) {
            return idList;
        }

        int fromIndex;
        int toIndex;

        if (!hasOffset) {
            if (pageSize < 0) {
                toIndex = idList.size();
                fromIndex = toIndex - Math.abs(pageSize);
            } else {
                fromIndex = 0;
                toIndex = fromIndex + pageSize;
            }
        } else {
            int offsetIndex;
            offsetIndex = idList.indexOf(offset);
            if (offsetIndex == -1) {
                throw new IllegalArgumentException("Invalid offset: " + offset);
            }

            if (pageSize > 0) {
                fromIndex = offsetIndex + 1;
                toIndex = fromIndex + pageSize;
            } else {
                toIndex = offsetIndex;
                fromIndex = toIndex - Math.abs(pageSize);
            }
        }
        fromIndex = Math.max(0, fromIndex);
        toIndex = Math.min(toIndex, idList.size());
        return idList.subList(fromIndex, toIndex);
    }

}
