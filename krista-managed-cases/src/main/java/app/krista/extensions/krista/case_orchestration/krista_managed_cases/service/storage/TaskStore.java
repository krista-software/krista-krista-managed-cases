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
import java.util.Objects;
import javax.inject.Inject;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

@Service
public class TaskStore {

    private final KeyValueStore keyValueStore;
    private final JsonMapper jsonMapper;

    @Inject
    public TaskStore(KeyValueStore keyValueStore, JsonMapper jsonMapper) {
        this.keyValueStore = keyValueStore;
        this.jsonMapper = jsonMapper;
    }

    public TaskDto getTask(String taskId) throws IOException {
        Objects.requireNonNull(taskId);
        String taskDtoString = (String) keyValueStore.get(getTaskKey(taskId));
        if (taskDtoString == null) {
            return null;
        }
        return jsonMapper.fromJson(taskDtoString, TaskDto.class);
    }

    public void putTask(TaskDto taskDto) throws IOException {
        Objects.requireNonNull(taskDto);
        Objects.requireNonNull(taskDto.getTaskId());
        keyValueStore.put(getTaskKey(taskDto.getTaskId()), jsonMapper.toJson(taskDto));
    }

    public void removeTask(String taskId) throws IOException {
        Objects.requireNonNull(taskId);
        keyValueStore.remove(taskId);
    }

    private String getTaskKey(String taskId) {
        return "task:" + taskId;
    }

}
