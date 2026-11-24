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
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Queue;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.*;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.storage.*;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.authorization.Role;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RuntimeContext;
import app.krista.ksdk.inboxes.Inbox;
import app.krista.ksdk.inboxes.InboxManager;
import app.krista.ksdk.notifications.NotificationManager;
import app.krista.model.field.FieldWithValue;
import app.krista.model.field.util.FieldValues;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

@Service
@ContractsProvided(TaskManager.class)
public class TaskManagerImpl implements TaskManager {

    private final AuthorizationContext authorizationContext;
    private final InboxManager inboxManager;
    private final TaskStore taskStore;
    private final ActiveTaskStore activeTaskStore;
    private final NotificationManager notificationManager;
    private final RuntimeContext runtimeContext;
    private final QueueStore queueStore;
    private final CachedAccountProvider accountProvider;
    private QueueProvider queueProvider;

    @Inject
    public TaskManagerImpl(AuthorizationContext authorizationContext, InboxManager inboxManager, TaskStore taskStore,
            ActiveTaskStore activeTaskStore, NotificationManager notificationManager, RuntimeContext runtimeContext,
            QueueStore queueStore, CachedAccountProvider accountProvider) {
        this.authorizationContext = authorizationContext;
        this.inboxManager = inboxManager;
        this.taskStore = taskStore;
        this.activeTaskStore = activeTaskStore;
        this.notificationManager = notificationManager;
        this.runtimeContext = runtimeContext;
        this.queueStore = queueStore;
        this.accountProvider = accountProvider;
    }

    public void init(QueueProvider queueProvider) {
        this.queueProvider = queueProvider;
    }

    @Override
    public Task createTask(String message, String inboxId) throws IOException {
        if (!Objects.isNull(message) && message.contains("<krista-field ")) {
            FieldWithValue fieldValue = FieldValues.fromKristaElement(message);
            message = FieldValues.toString(fieldValue.getField(), message);
        }
        Account creator = authorizationContext.getAuthorizedAccount();
        String openTaskId = isUserHasOpenTask();
        if (openTaskId != null) {
            return getTask(openTaskId);
        }
        String activeTaskId = isUserHasActiveTask(creator.getAccountId());
        if (activeTaskId != null) {
            Task task = getTask(activeTaskId);
            String agent = task.getActiveParticipants().stream().map(k -> k.getParticipant().getAccountName())
                    .collect(Collectors.joining(","));
            throw new IllegalStateException("You have already connected to " + agent);
        }
        String taskId = UUID.randomUUID().toString();
        Inbox inbox = inboxManager.getInbox(inboxId);
        if (inbox == null) {
            String errorMessage = "CreateTask: We can't find the inbox: " + inboxId;
            System.err.println(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        Task task = new TaskImpl(taskId, message, inbox,
                new AccountImpl(creator.getAccountId(), creator.getPerson().getPersonName()),
                System.currentTimeMillis(), new ArrayList<>(), queueProvider, taskStore,
                activeTaskStore, notificationManager, runtimeContext, queueStore);
        taskStore.putTask(
                new TaskDto(task.getTaskId(), task.getInbox().getInboxId(), task.getMessage(), task.getCreationTime(),
                        new AccountDto(task.getCreator().getAccountId(), task.getCreator().getAccountName()),
                        new LinkedHashMap<>()));
        return task;
    }

    @Override
    public List<Task> getActiveTasks(String offset, int pageSize) throws IOException {
        Account account = authorizationContext.getAuthorizedAccount();
        List<Task> tasks = new ArrayList<>();
        for (String taskId : activeTaskStore.getActiveTasks(account.getAccountId(), offset, pageSize)) {
            try {
                tasks.add(getTask(taskId));
            } catch (NoSuchElementException ex) {
                System.out.println("No such task exists: " + taskId + ". " + ex.getLocalizedMessage());
            }
        }
        return tasks;
    }

    @Override
    public List<Task> getOpenTasks(String offset, int pageSize) throws IOException {
        Account account = authorizationContext.getAuthorizedAccount();
        Map<String, Task> tasks = new LinkedHashMap<>();
        for (Role role : account.getRoles()) {
            if (queueProvider.hasQueue(role.getRoleId())) {
                Queue queue = queueProvider.getQueue(role.getRoleId());
                List<Task> queueTasks = queue.getAllQueuedTasks();
                if (queueTasks.isEmpty()) {
                    continue;
                }
                tasks.putAll(addOpenTasks(account, queueTasks));
            }
        }
        List<String> taskIds = IdLists.getPaginatedList(new ArrayList<>(tasks.keySet()), offset, pageSize);
        return taskIds.stream().map(tasks::get).collect(Collectors.toList());
    }

    @Override
    public Task getTask(String taskId) throws IOException {
        Objects.requireNonNull(taskId);
        TaskDto taskDto = taskStore.getTask(taskId);
        return new TaskImpl(taskDto.getTaskId(), taskDto.getMessage(), inboxManager.getInbox(taskDto.getSessionId()),
                new AccountImpl(taskDto.getCreator().getAccountId(), taskDto.getCreator().getAccountName()),
                taskDto.getCreationTime(), toActiveParticipants(taskDto.getActiveParticipants()),
                queueProvider, taskStore, activeTaskStore, notificationManager, runtimeContext, queueStore);
    }

    private String isUserHasActiveTask(String accountId) throws IOException {
        Account userAccount = accountProvider.getAccount(accountId);
        for (String taskId : activeTaskStore.getActiveTasksFromParticipants()) {
            TaskDto task = taskStore.getTask(taskId);
            if (Objects.equals(task.getCreator().getAccountId(), userAccount.getAccountId())) {
                return taskId;
            }
        }
        return null;
    }

    private String isUserHasOpenTask() throws IOException {
        Account account = authorizationContext.getAuthorizedAccount();
        for (Role role : account.getRoles()) {
            if (queueProvider.hasQueue(role.getRoleId())) {
                Queue queue = queueProvider.getQueue(role.getRoleId());
                List<String> taskIdsInQueue = queueStore.getTaskIdsInQueue(queue.getQueueId());
                for (String taskId : taskIdsInQueue) {
                    TaskDto task = taskStore.getTask(taskId);
                    if (Objects.equals(task.getCreator().getAccountId(), account.getAccountId())) {
                        return task.getTaskId();
                    }
                }
            }
        }
        return null;
    }

    private static Map<String, Task> addOpenTasks(Account account, List<Task> queueTasks) {
        Map<String, Task> tasks = new LinkedHashMap<>();
        for (Task queueTask : queueTasks) {
            List<ActiveParticipant> activeParticipants = queueTask.getActiveParticipants();
            if (isOneOrZeroParticipantInTheList(activeParticipants)) {
                tasks.put(queueTask.getTaskId(), queueTask);
            } else {
                List<String> accountIds =
                        activeParticipants.stream().filter(participant -> Objects.nonNull(participant.getParticipant()))
                                .map(k -> k.getParticipant().getAccountId()).collect(Collectors.toList());
                if (!accountIds.contains(account.getAccountId())) {
                    tasks.put(queueTask.getTaskId(), queueTask);
                }
            }
        }
        return tasks;
    }

    private static boolean isOneOrZeroParticipantInTheList(List<ActiveParticipant> activeParticipants) {
        return activeParticipants == null || activeParticipants.isEmpty() ||
                activeParticipants.size() == 1;
    }

    private List<ActiveParticipant> toActiveParticipants(Map<String, Set<AccountDto>> activeParticipants)
            throws IOException {
        List<ActiveParticipant> participants = new ArrayList<>();
        for (Map.Entry<String, Set<AccountDto>> queueEntry : activeParticipants.entrySet()) {
            String queueId = queueEntry.getKey();
            Queue queue = queueProvider.getQueue(queueId);
            for (AccountDto account : queueEntry.getValue()) {
                participants.add(new ActiveParticipantImpl(queue, Objects.equals(account, TaskDto.IN_QUEUE) ? null
                        : new AccountImpl(account.getAccountId(), account.getAccountName())));
            }
        }
        return participants;
    }

}
