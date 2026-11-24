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
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.impl.IdLists;
import app.krista.extensions.util.KeyValueStore;
import app.krista.model.field.util.JavaTypes;
import org.jvnet.hk2.annotations.Service;

@Service
public class QueueStore {

    private final KeyValueStore keyValueStore;
    private final JsonMapper jsonMapper;

    @Inject
    public QueueStore(KeyValueStore keyValueStore, JsonMapper jsonMapper) {
        this.keyValueStore = keyValueStore;
        this.jsonMapper = jsonMapper;
    }

    public List<String> getTasksInQueue(String queueId, String offset, int pageSize) throws IOException {
        Objects.requireNonNull(queueId);
        return IdLists.getPaginatedList(getTaskIdsInQueue(queueId), offset, pageSize);
    }

    public void addTaskToQueue(String queueId, String taskId) throws IOException {
        Objects.requireNonNull(queueId);
        Objects.requireNonNull(taskId);
        List<String> taskIds = getTaskIdsInQueue(queueId);
        if (taskIds.contains(taskId)) {
            return;
        }
        taskIds.add(taskId);
        putTaskIdsInQueue(queueId, taskIds);
    }

    public void removeTaskFromQueue(String queueId, String taskId) throws IOException {
        Objects.requireNonNull(queueId);
        Objects.requireNonNull(taskId);
        List<String> taskIds = getTaskIdsInQueue(queueId);
        taskIds.remove(taskId);
        putTaskIdsInQueue(queueId, taskIds);
    }

    private void putTaskIdsInQueue(String queueId, List<String> taskIds) throws IOException {
        keyValueStore.put(getQueueKey(queueId), jsonMapper.toJson(taskIds));
    }

    public List<String> getTaskIdsInQueue(String queueId) throws IOException {
        String key = getQueueKey(queueId);
        String queueString = (String) keyValueStore.get(key);
        if (queueString == null || queueString.isBlank()) {
            return new ArrayList<>();
        }
        return jsonMapper.fromJson(queueString, JavaTypes.getTypedClass(List.class, String.class));
    }

    private String getQueueKey(String queueId) {
        return "queue:" + queueId;
    }

}
