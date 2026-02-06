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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.inject.Inject;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Queue;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Task;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.TaskManager;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.storage.QueueStore;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.storage.TaskStore;
import app.krista.ksdk.authorization.Role;
import app.krista.ksdk.context.RuntimeContext;
import app.krista.ksdk.notifications.Notification;
import app.krista.ksdk.notifications.NotificationManager;
import org.jvnet.hk2.annotations.Service;

@Service
public class QueueImpl implements Queue {

    private final Role role;
    private final QueueStore queueStore;
    private final TaskManager taskManager;
    private final NotificationManager notificationManager;
    private final RuntimeContext runtimeContext;

    @Inject
    public QueueImpl(Role role, QueueStore queueStore, TaskManager taskManager,
            TaskStore taskStore, NotificationManager notificationManager,
            RuntimeContext runtimeContext) {
        this.role = role;
        this.queueStore = queueStore;
        this.taskManager = taskManager;
        this.notificationManager = notificationManager;
        this.runtimeContext = runtimeContext;
    }

    @Override
    public Role getAssociatedRole() {
        return role;
    }

    @Override
    public void submitTask(Task task) throws IOException {
        task.waitForParticipant(this);
        queueStore.addTaskToQueue(getQueueId(), task.getTaskId());
        notificationManager.notify(new Notification(
                        Notification.TYPE_NEW_TASK,
                        System.currentTimeMillis(),
                        newTaskPayload(task)),
                List.of("role:" + getQueueId()));
    }

    private Map<String, Object> newTaskPayload(Task task) {
        return Map.of("workspaceId", this.runtimeContext.getWorkspaceId(),
                "queueId", getQueueId(),
                "taskId", task.getTaskId());
    }

    @Override
    public List<Task> getQueuedTasks(String offset, int pageSize) throws IOException {
        List<Task> list = new ArrayList<>();
        for (String taskId : queueStore.getTasksInQueue(getQueueId(), offset, pageSize)) {
            try {
                Task task = taskManager.getTask(taskId);
                list.add(task);
            } catch (NoSuchElementException ex) {
	                System.out.println("No such task exists. " + ex.getLocalizedMessage());
            }
        }
        return list;
    }

    @Override
    public List<Task> getAllQueuedTasks() throws IOException {
        List<Task> list = new ArrayList<>();
        for (String taskId : queueStore.getTaskIdsInQueue(getQueueId())) {
            try {
                Task task = taskManager.getTask(taskId);
                list.add(task);
            } catch (NoSuchElementException ex) {
	                System.out.println("No such task exists. " + ex.getLocalizedMessage());
            }
        }
        return list;
    }

}
