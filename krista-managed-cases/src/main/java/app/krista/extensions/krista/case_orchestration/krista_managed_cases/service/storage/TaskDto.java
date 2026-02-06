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

package app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.storage;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TaskDto {

    private final String taskId;
    private final String sessionId;
    private final String message;
    private final long creationTime;
    private final AccountDto creator;
    private final Map<String, Set<AccountDto>> activeParticipants;
    public static final AccountDto IN_QUEUE = new AccountDto("_inqueue", "_inqueue");

    public TaskDto(String taskId, String sessionId, String message, long creationTime, AccountDto creator,
            Map<String, Set<AccountDto>> activeParticipants) {
        this.taskId = taskId;
        this.sessionId = sessionId;
        this.message = message;
        this.creationTime = creationTime;
        this.creator = creator;
        this.activeParticipants = activeParticipants;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getMessage() {
        return message;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public AccountDto getCreator() {
        return creator;
    }

    public Map<String, Set<AccountDto>> getActiveParticipants() {
        return activeParticipants;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !Objects.equals(getClass(), obj.getClass())) {
            return false;
        }
        TaskDto other = (TaskDto) obj;
        return Objects.equals(taskId, other.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

}
