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
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Queue;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.*;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.storage.*;
import app.krista.ksdk.context.RuntimeContext;
import app.krista.ksdk.inboxes.Inbox;
import app.krista.ksdk.message.EventMessage;
import app.krista.ksdk.notifications.Notification;
import app.krista.ksdk.notifications.NotificationManager;

public class TaskImpl implements Task {

    private final String taskId;
    private final String message;
    private final Inbox inbox;
    private final Account creator;
    private final long creationTime;
    private final List<ActiveParticipant> activeParticipants;
    private final QueueProvider queueProvider;
    private final TaskStore taskStore;
    private final ActiveTaskStore activeTaskStore;
    private final NotificationManager notificationManager;
    private final RuntimeContext runtimeContext;
    private final QueueStore queueStore;

    public TaskImpl(String taskId, String message, Inbox inbox, Account creator, long creationTime,
            List<ActiveParticipant> activeParticipants, QueueProvider queueProvider, TaskStore taskStore,
            ActiveTaskStore activeTaskStore, NotificationManager notificationManager, RuntimeContext runtimeContext,
            QueueStore queueStore) {
        this.taskId = taskId;
        this.message = message;
        this.inbox = inbox;
        this.creator = creator;
        this.creationTime = creationTime;
        this.activeParticipants = activeParticipants;
        this.queueProvider = queueProvider;
        this.taskStore = taskStore;
        this.activeTaskStore = activeTaskStore;
        this.notificationManager = notificationManager;
        this.runtimeContext = runtimeContext;
        this.queueStore = queueStore;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Inbox getInbox() {
        return inbox;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public Account getCreator() {
        return creator;
    }

    @Override
    public void waitForParticipant(Queue queue) throws IOException {
        TaskDto taskDto = taskStore.getTask(taskId);
        if (!taskDto.getActiveParticipants().containsKey(queue.getQueueId())) {
            taskDto.getActiveParticipants().put(queue.getQueueId(), new LinkedHashSet<>());
        }
        taskDto.getActiveParticipants().get(queue.getQueueId()).add(TaskDto.IN_QUEUE);
        taskStore.putTask(taskDto);
        activeParticipants.add(new ActiveParticipantImpl(queue, null));
    }

    @Override
    public List<ActiveParticipant> getActiveParticipants() {
        return activeParticipants;
    }

    @Override
    public void join(app.krista.ksdk.accounts.Account account) throws IOException {
        Objects.requireNonNull(account);
        String joinedQueueId = addAsParticipant(account);
        if (joinedQueueId == null) {
            throw new IllegalStateException("Invalid request");
        }

        activeTaskStore.addActiveTask(account.getAccountId(), taskId);
        notificationManager.notify(new Notification(Notification.TYPE_TASK_JOINED, System.currentTimeMillis(),
                userJoinedPayload(joinedQueueId)), List.of("role:" + joinedQueueId));
	        int activeParticipantCount = activeTaskStore.getActiveTasksFromParticipants().size();
	        inbox.sendEventMessage(EventMessage.EVENT_TYPE_JOIN_TASK, Map.of("account", getEventPayload(account),
	                "activeParticipantCounts", activeParticipantCount));
        queueStore.removeTaskFromQueue(joinedQueueId, taskId);

        inbox.updateAcl(List.of("account:" + account.getAccountId()));
        sendUpdateTaskNotification(account);
    }

    private Map<String, String> getEventPayload(app.krista.ksdk.accounts.Account account) {
        return Map.of("_id", account.getAccountId(), "name", account.getPerson().getPersonName());
    }

    private Map<String, Object> userJoinedPayload(String joinedQueueId) {
        Map<String, Object> userJoined = new LinkedHashMap<>();
        userJoined.put("workspaceId", runtimeContext.getWorkspaceId());
        userJoined.put("queueId", joinedQueueId);
        userJoined.put("taskId", taskId);
        userJoined.put("accountId", creator.getAccountId());
        return userJoined;
    }

    private void sendUpdateTaskNotification(app.krista.ksdk.accounts.Account ignoreAccount) throws IOException {
        TaskDto task = taskStore.getTask(taskId);
	        List<AccountDto> activeParticipantAccounts =
	                task.getActiveParticipants().values().stream().flatMap(Set::stream).collect(Collectors.toList());
        List<String> accountIds =
	                activeParticipantAccounts.stream().map(AccountDto::getAccountId)
                        .filter(accountId -> !Objects.equals(accountId, ignoreAccount.getAccountId())).collect(
                                Collectors.toList());
        for (String accountId : accountIds) {
            notificationManager.notify(
                    new Notification(Notification.TYPE_TASK_UPDATE, System.currentTimeMillis(), userUpdatedPayload()),
                    List.of("account:" + accountId));
        }
    }

    private Map<String, Object> userUpdatedPayload() {
        Map<String, Object> userJoined = new LinkedHashMap<>();
        userJoined.put("workspaceId", runtimeContext.getWorkspaceId());
        userJoined.put("taskId", taskId);
        return userJoined;
    }

    private String addAsParticipant(app.krista.ksdk.accounts.Account account) throws IOException {
        TaskDto taskDto = taskStore.getTask(taskId);
        Map.Entry<String, Set<AccountDto>> queueEntry = taskDto.getActiveParticipants().entrySet().stream()
                .filter(e -> e.getValue().contains(TaskDto.IN_QUEUE)).findFirst().orElse(null);
        if (queueEntry == null) {
            return null;
        }
        String queueId = queueEntry.getKey();
        taskDto.getActiveParticipants().get(queueId)
                .add(new AccountDto(account.getAccountId(), account.getPerson().getPersonName()));
        Set<AccountDto> accountDtos = taskDto.getActiveParticipants().get(queueEntry.getKey());
        if (accountDtos != null) {
            accountDtos.remove(TaskDto.IN_QUEUE);
        }
        taskStore.putTask(taskDto);
        activeParticipants.add(new ActiveParticipantImpl(queueProvider.getQueue(queueId),
                new AccountImpl(account.getAccountId(), account.getPerson().getPersonName())));
        return queueId;
    }

    @Override
    public void leave(app.krista.ksdk.accounts.Account account) throws IOException {
        Objects.requireNonNull(account);
        removeAsParticipant(account);
        activeTaskStore.removeActiveTask(account.getAccountId(), taskId);
        int activeParticipants = activeTaskStore.getActiveTasksFromParticipants().size();
        inbox.sendEventMessage(EventMessage.EVENT_TYPE_LEAVE_TASK, Map.of("account", getEventPayload(account),
                "activeParticipantCounts", activeParticipants));
        inbox.removeAcl(List.of("account:" + account.getAccountId()));
        sendUpdateTaskNotification(account);
    }

    private void removeAsParticipant(app.krista.ksdk.accounts.Account account) throws IOException {
        TaskDto taskDto = taskStore.getTask(taskId);
        if (taskDto == null) {
            throw new NoSuchElementException();
        }
	        Map<String, Set<AccountDto>> activeParticipantsByQueue = taskDto.getActiveParticipants();
	        for (Map.Entry<String, Set<AccountDto>> stringSetEntry : activeParticipantsByQueue.entrySet()) {
            stringSetEntry.getValue()
                    .remove(new AccountDto(account.getAccountId(), account.getPerson().getPersonName()));
        }
	        if (activeParticipantsByQueue.isEmpty()) {
            taskStore.removeTask(taskDto.getTaskId());
        } else {
	            taskDto.getActiveParticipants().putAll(activeParticipantsByQueue);
            taskStore.putTask(taskDto);
        }
    }

}
