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
import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.KristaManagedQueuesAttributes;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Queue;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.QueueProvider;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.TaskManager;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.storage.QueueStore;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.storage.TaskStore;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.authorization.Role;
import app.krista.ksdk.context.RuntimeContext;
import app.krista.ksdk.notifications.NotificationManager;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

@Service
@ContractsProvided(QueueProvider.class)
public class QueueProviderImpl implements QueueProvider {

    private final KristaManagedQueuesAttributes attributes;
    private final QueueStore queueStore;
    private final TaskManager taskManager;
    private final TaskStore taskStore;
    private final NotificationManager notificationManager;
    private final RuntimeContext runtimeContext;

    @Inject
    public QueueProviderImpl(KristaManagedQueuesAttributes attributes,
            QueueStore queueStore,
            TaskManager taskManager,
            TaskStore taskStore, NotificationManager notificationManager,
            RuntimeContext runtimeContext) {
        this.attributes = attributes;
        this.queueStore = queueStore;
        this.taskManager = taskManager;
        this.taskStore = taskStore;
        this.notificationManager = notificationManager;
        this.runtimeContext = runtimeContext;
        ((TaskManagerImpl) taskManager).init(this);
    }

    @Override
    public List<Queue> getAllQueues() {
        return mapRoleToQueue(attributes.getRoles());
    }

    @Override
    public List<Queue> getUserQueues(Account account) {
        Set<String> userRoleIds = account.getRoles().stream().map(Role::getRoleId).collect(Collectors.toSet());
        return mapRoleToQueue(attributes.getRoles().stream()
                .filter(r -> userRoleIds.contains(r.getRoleId()))
                .collect(Collectors.toList()));
    }

    private List<Queue> mapRoleToQueue(Collection<Role> roles) {
        return roles.stream()
                .map(r -> new QueueImpl(r, queueStore, taskManager, taskStore, notificationManager, runtimeContext))
                .collect(Collectors.toList());
    }

    @Override
    public Queue getQueue(String queueId) throws IOException {
        Objects.requireNonNull(queueId);
        for (Queue queue : getAllQueues()) {
            if (Objects.equals(queue.getQueueId(), queueId)) {
                return queue;
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    public Queue lookupQueue(String queueIdOrName) throws IOException {
        Objects.requireNonNull(queueIdOrName);
        for (Queue queue : getAllQueues()) {
            if (Objects.equals(queue.getQueueId(), queueIdOrName)) {
                return queue;
            }
            if (Objects.equals(queue.getQueueName(), queueIdOrName)) {
                return queue;
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    public boolean hasQueue(String queueId) throws IOException {
        if (queueId == null) {
            return false;
        }
        for (Queue queue : getAllQueues()) {
            if (Objects.equals(queue.getQueueId(), queueId)) {
                return true;
            }
        }
        return false;
    }

}
