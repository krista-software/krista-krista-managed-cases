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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Queue;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.QueueProvider;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Task;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.TaskManager;
import app.krista.ksdk.sessions.ChannelSession;
import app.krista.ksdk.sessions.SessionManager;
import app.krista.model.base.FreeForm;

@Domain(id = "catEntryDomain_e4893b19-13b3-4824-93ac-7982bd9f5fb0",
        name = "Support Queues",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista",
        ecosystemVersion = "33ed3964-b012-4042-9c5d-e7103d774e9f")
public class ClientsArea {

    public static final String ROLE_ID = "Role ID";
    private final QueueProvider queueProvider;
    private final TaskManager taskManager;
    private final SessionManager sessionManager;

    @Inject
    public ClientsArea(QueueProvider queueProvider, TaskManager taskManager, SessionManager sessionManager) {
        this.queueProvider = queueProvider;
        this.taskManager = taskManager;
        this.sessionManager = sessionManager;
    }

    @CatalogRequest(
            id = "localDomainRequest_40c6a8a4-e6c8-4c95-a354-c9eed7fb764a",
            name = "Get Available Routing Info",
            description = "This request is used to get all the routing info",
            area = "Clients",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Routing Info", type = "[ { _id: Text, displayName: Text } ]", required = false)
    public List<Map<String, Object>> getAvailableRoutingInfo() throws IOException {
        List<Map<String, Object>> routingInfo = new ArrayList<>();
        for (Queue queue : queueProvider.getAllQueues()) {
            String routingId = queue.getQueueId();
            routingInfo.add(Map.of("_id", routingId, "displayName", queue.getQueueName()));
        }
        return routingInfo;
    }

    @CatalogRequest(
            id = "localDomainRequest_b1c41851-3c18-469a-b182-56fba1df1356",
            name = "Get Routing Info",
            description = "This request is used to get the routing info for given routing Id",
            area = "Clients",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field(name = "Routing Info", type = "FreeForm", required = false, attributes = {}, options = {})
    public FreeForm getRoutingInfo(
            @Field(name = "Routing Id", type = "Text", required = true, attributes = {}, options = {}) String routingId)
            throws IOException {
        Objects.requireNonNull(routingId);
        Queue queue = queueProvider.getQueue(routingId);
        FreeForm freeForm = new FreeForm();
        freeForm.put(ROLE_ID, "Text", queue.getQueueId());
        freeForm.put("Role Name", "Text", queue.getQueueName());
        return freeForm;
    }

    @CatalogRequest(
            id = "localDomainRequest_4671be3f-1e3f-4354-aada-c7d3a263b97c",
            name = "Submit Task",
            description = "This request is used to create task which is in open state",
            area = "Clients",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Task", type = "{ _id: Text, inboxId: Text, creationTime: Date, message: Text, status: Text, createdBy: { _id: Text, name: Text }, activeParticipants: [ { queue: { _id: Text, name: Text }, account: { _id: Text, name: Text } } ] }", required = false)
    public Map<String, Object> submitTask(
            @Field(name = "Message", type = "Text", required = true, attributes = {}, options = {}) String message,
            @Field(name = "Inbox Id", type = "Text", required = true, attributes = {}, options = {}) String inboxId,
            @Field(name = "routingInfo", type = "FreeForm", attributes = {}, options = {}) FreeForm routingInfo)
            throws IOException {
        Objects.requireNonNull(message, "Message is required and cannot be null");
        Objects.requireNonNull(inboxId, "Inbox Id is required and cannot be null");
        if (routingInfo == null) {
            throw new IllegalArgumentException(
                    "Routing Info is required and cannot be null. Please pass the FreeForm returned by 'Get Routing Info'.");
        }

        String queueIdOrName;
        if (routingInfo.containsKey(ROLE_ID)) {
            queueIdOrName = (String) routingInfo.get(ROLE_ID);
        } else if (routingInfo.containsKey("Role Name")) {
            queueIdOrName = (String) routingInfo.get("Role Name");
        } else {
            throw new IllegalArgumentException("Routing Info must contain either 'Role ID' or 'Role Name'.");
        }
        Queue queue = queueProvider.lookupQueue(queueIdOrName);
        if (inboxId.startsWith("inbox_S_") || inboxId.startsWith("inbox_defnMult_")) {
            Task newTask = taskManager.createTask(message, inboxId);
            queue.submitTask(newTask);
            return MultiFields.toMultiField(newTask, "open");
        }
        throw new IllegalArgumentException("Invalid inbox Id: " + inboxId);
    }

    @CatalogRequest(
            id = "localDomainRequest_1c9530a8-9e80-440b-927d-92a44ddf8087",
            name = "Get Inbox Id",
            description = "The request returns inboxId.",
            area = "Clients",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Text(name = "Inbox Id", required = false, attributes = {
            @Attribute(name = "visualWidth", value = "S")}, options = {})
    public String getInboxId(
            @Field.Text(name = "Channel Name", required = false, attributes = {
                    @Attribute(name = "visualWidth", value = "S")}, options = {}) String channelName,
            @Field.Text(name = "Email Id", required = true, attributes = {
                    @Attribute(name = "visualWidth", value = "S")}, options = {}) String emailId) {
        if (channelName == null || channelName.isBlank()) {
            channelName = "kristasoft";
        }
        ChannelSession channelSession = sessionManager.createChannelSession(channelName, emailId);
        return channelSession.getSessionInbox().getInboxId();
    }

}
