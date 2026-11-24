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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Queue;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.QueueProvider;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Task;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.TaskManager;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.impl.CachedAccountProvider;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.impl.CachedTaskProvider;
import app.krista.ksdk.accounts.Account;
import app.krista.model.base.FreeForm;

@Domain(id = "catEntryDomain_e4893b19-13b3-4824-93ac-7982bd9f5fb0",
        name = "Support Queues",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista",
        ecosystemVersion = "33ed3964-b012-4042-9c5d-e7103d774e9f")
public class AgentsArea {

    private final QueueProvider queueProvider;
    private final CachedAccountProvider accountProvider;
    private final CachedTaskProvider cachedTaskProvider;
    private final TaskManager taskManager;

    @Inject
    public AgentsArea(QueueProvider queueProvider, CachedAccountProvider accountProvider,
            TaskManager taskManager, CachedTaskProvider cachedTaskProvider) {
        this.queueProvider = queueProvider;
        this.accountProvider = accountProvider;
        this.taskManager = taskManager;
        this.cachedTaskProvider = cachedTaskProvider;
    }

    @CatalogRequest(
            id = "localDomainRequest_5cc35881-c539-4315-89c7-eb1889ef01f2",
            name = "Join Task",
            description = "This request is used to join to the task",
            area = "Agents",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Task", type = "{ _id: Text, inboxId: Text, creationTime: Date, message: Text, status: Text, createdBy: { _id: Text, name: Text }, activeParticipants: [ { queue: { _id: Text, name: Text }, account: { _id: Text, name: Text } } ] }", required = false)
    public Map<String, Object> joinTask(
            @Field(name = "Task Id", type = "Text", required = true, attributes = {}, options = {}) String taskId,
            @Field(name = "Account Id", type = "Text", required = true, attributes = {}, options = {}) String accountId)
            throws IOException {
        Objects.requireNonNull(taskId);
        Objects.requireNonNull(accountId);
        try {
            Task task = cachedTaskProvider.getTask(taskId);
            if (task == null) {
                throw new IllegalStateException(
                        "Incorrect task ID. Error in joining.");
            }
            synchronized (task) {
                task.join(accountProvider.getAccount(accountId));
                cachedTaskProvider.remove(taskId);
            }
            return MultiFields.toMultiField(task, "active");
        } finally {
            accountProvider.clear();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_58c80c0b-cb99-4090-b008-051efbc94cc5",
            name = "Get Tasks",
            description = "This request is used to get both active and open tasks depending on status",
            area = "Agents",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Tasks", type = "[ { _id: Text, inboxId: Text, creationTime: Date, message: Text, status: Text, createdBy: { _id: Text, name: Text }, activeParticipants: [ { queue: { _id: Text, name: Text }, account: { _id: Text, name: Text } } ] } ]", required = false)
    public List<Map<String, Object>> getTasks(
            @Field(name = "Offset", type = "Text", required = false, attributes = {}, options = {}) String offset,
            @Field(name = "Limit", type = "Number", required = false, attributes = {}, options = {}) Double limit,
            @Field(name = "Queue Id", type = "Text", required = false, attributes = {}, options = {}) String queueId,
            @Field(name = "Status", type = "Text", required = false, attributes = {}, options = {}) String status)
            throws IOException {
        try {
            List<Task> tasks;
            if ("active".equals(status)) {
                tasks = taskManager.getActiveTasks(offset, limit.intValue());
            } else if (queueId != null && !queueId.isBlank()) {
                tasks = queueProvider.getQueue(queueId).getQueuedTasks(offset, limit.intValue());
            } else {
                tasks = taskManager.getOpenTasks(offset, limit.intValue());
            }
            return MultiFields.toMultiFields(tasks, status);
        } finally {
            accountProvider.clear();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_1f7c08ab-5cfb-4fed-bf35-e797c859aecb",
            name = "Submit Request By Agent",
            description = "This request is used to submit task by agent",
            area = "Agents",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    public void submitRequestByAgent(
            @Field(name = "Task Id", type = "Text", required = true, attributes = {}, options = {}) String taskId,
            @Field(name = "inbox Id", type = "Text", required = true, attributes = {}, options = {}) String inboxId,
            @Field(name = "Routing Info", type = "FreeForm", required = true, attributes = {}, options = {}) FreeForm routingInfo) {
        Objects.requireNonNull(taskId, "Task Id is required and cannot be null");
        Objects.requireNonNull(inboxId, "Inbox Id is required and cannot be null");
        if (routingInfo == null) {
            throw new IllegalArgumentException("Routing Info is required and cannot be null. Please pass the FreeForm returned by 'Get Routing Info'.");
        }
        try {
            String queueIdOrName;
            if (routingInfo.containsKey("Role ID")) {
                queueIdOrName = (String) routingInfo.get("Role ID");
            } else if (routingInfo.containsKey("Role Name")) {
                queueIdOrName = (String) routingInfo.get("Role Name");
            } else {
                throw new IllegalArgumentException("Routing Info must contain either 'Role ID' or 'Role Name'.");
            }
            Task task = taskManager.getTask(taskId);
            Queue queue = queueProvider.lookupQueue(queueIdOrName);
            queue.submitTask(task);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        } finally {
            accountProvider.clear();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_32924d9a-1aeb-4a1b-b222-d0a26d06c4c0",
            name = "Leave Task",
            description = "This request is used to by agent to leave task",
            area = "Agents",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    public void leaveTask(
            @Field(name = "Task Id", type = "Text", required = true, attributes = {}, options = {}) String taskId,
            @Field(name = "Account Id", type = "Text", required = true, attributes = {}, options = {}) String accountId)
            throws IOException {
        Objects.requireNonNull(taskId);
        Objects.requireNonNull(accountId);
        try {
            // TODO: check permissions
            Account leaver = accountProvider.getAccount(accountId);
            Task task = cachedTaskProvider.getTask(taskId);
            if (task == null) {
                throw new IllegalStateException("Incorrect task ID. Error in leaving.");
            }
            synchronized (task) {
                task.leave(leaver);
                cachedTaskProvider.remove(taskId);
            }
        } finally {
            accountProvider.clear();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_025f2ac6-8dea-446e-bd83-c1f6b4d151e5",
            name = "Get Task",
            description = "This request is used to get the single active task",
            area = "Agents",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Task", type = "{ _id: Text, inboxId: Text, creationTime: Date, message: Text, status: Text, createdBy: { _id: Text, name: Text },  activeParticipants: [ { queue: { _id: Text, name: Text }, account: { _id: Text, name: Text } } ] }", required = false)
    public Map<String, Object> getTask(
            @Field(name = "Task Id", type = "Text", required = true, attributes = {}, options = {}) String taskId)
            throws IOException {
        Objects.requireNonNull(taskId);
        try {
            // TODO: check permissions
            return MultiFields.toMultiField(taskManager.getTask(taskId), "not set");
        } finally {
            accountProvider.clear();
        }
    }

}