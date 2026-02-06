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

package app.krista.extensions.krista.case_orchestration.krista_managed_cases.catalog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Account;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.ActiveParticipant;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Queue;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Task;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.impl.AccountImpl;

public final class MultiFields {

    public static Map<String, Object> toMultiField(Task task, String status) {
        Map<String, Object> multiField = new LinkedHashMap<>();
        multiField.put("_id", task.getTaskId());
        multiField.put("inboxId", task.getInbox().getInboxId());
        multiField.put("message", task.getMessage());
        multiField.put("creationTime", task.getCreationTime());
        multiField.put("status", status);
        multiField.put("createdBy",
                toMultiField(new AccountImpl(task.getCreator().getAccountId(), task.getCreator().getAccountName())));
        multiField.put("activeParticipants", toMultiField(task.getActiveParticipants()));
        return multiField;
    }

    private static List<Map<String, Map<String, Object>>> toMultiField(List<ActiveParticipant> activeParticipants) {
        List<Map<String, Map<String, Object>>> result = new ArrayList<>();
        for (ActiveParticipant activeParticipant : activeParticipants) {
            if (activeParticipant.getParticipant() != null) {
                Map<String, Map<String, Object>> multiField = new LinkedHashMap<>();
                multiField.put("account", toMultiField(activeParticipant.getParticipant()));
                multiField.put("queue", toMultiField(activeParticipant.getQueue()));
                result.add(multiField);
            }
        }
        return result;
    }

    private static Map<String, Object> toMultiField(Queue queue) {
        return Map.of("_id", queue.getQueueId(), "name", queue.getQueueName());
    }

    private static Map<String, Object> toMultiField(Account account) {
        return Map.of("_id", account.getAccountId(), "name", account.getAccountName());
    }

    public static List<Map<String, Object>> toMultiFields(List<Task> tasks, String status) {
        return tasks.stream().map(k -> toMultiField(k, status)).collect(Collectors.toList());
    }

    private MultiFields() {
    }

}
