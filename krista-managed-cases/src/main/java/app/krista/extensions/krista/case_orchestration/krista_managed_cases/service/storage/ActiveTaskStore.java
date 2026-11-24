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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.impl.IdLists;
import app.krista.extensions.util.KeyValueStore;
import app.krista.model.field.util.JavaTypes;
import org.jvnet.hk2.annotations.Service;

@Service
public class ActiveTaskStore {

    private final KeyValueStore keyValueStore;
    private final JsonMapper jsonMapper;

    @Inject
    public ActiveTaskStore(KeyValueStore keyValueStore, JsonMapper jsonMapper) {
        this.keyValueStore = keyValueStore;
        this.jsonMapper = jsonMapper;
    }

    public List<String> getActiveTasks(String accountId, String offset, int pageSize) throws IOException {
        List<String> taskIds = getActiveTaskIds(accountId);
        return IdLists.getPaginatedList(taskIds, offset, pageSize);
    }

    public List<String> getActiveTasksFromParticipants() throws IOException {
        List<String> activeTaskIds = new LinkedList<>();
        for (String accountId : keyValueStore.getKeys()) {
            if (accountId.startsWith("participant:")) {
                String taskIds = (String) keyValueStore.get(accountId);
                List<String> ids = jsonMapper.fromJson(taskIds,
                        JavaTypes.getTypedClass(List.class, String.class));
                activeTaskIds.addAll(ids);
            }
        }
        return activeTaskIds;
    }

    private List<String> getActiveTaskIds(String accountId) throws IOException {
        String key = getAccountKey(accountId);
        String taskIdString = (String) keyValueStore.get(key);
        if (taskIdString == null || taskIdString.isBlank()) {
            return new ArrayList<>();
        }
        return jsonMapper.fromJson(taskIdString, JavaTypes.getTypedClass(List.class, String.class));
    }

    private void putTaskIds(String accountId, List<String> activeTaskIds) throws IOException {
        keyValueStore.put(getAccountKey(accountId), jsonMapper.toJson(activeTaskIds));
    }

    public void addActiveTask(String accountId, String taskId) throws IOException {
        Objects.requireNonNull(accountId);
        Objects.requireNonNull(taskId);
        List<String> taskIds = getActiveTaskIds(accountId);
        if (taskIds.contains(taskId)) {
            return;
        }
        taskIds.add(taskId);
        putTaskIds(accountId, taskIds);
    }

    public void removeActiveTask(String accountId, String taskId) throws IOException {
        Objects.requireNonNull(accountId);
        Objects.requireNonNull(taskId);
        List<String> taskIds = getActiveTaskIds(accountId);
        taskIds.remove(taskId);
        putTaskIds(accountId, taskIds);
    }

    private String getAccountKey(String accountId) {
        return "participant:" + accountId;
    }

}
