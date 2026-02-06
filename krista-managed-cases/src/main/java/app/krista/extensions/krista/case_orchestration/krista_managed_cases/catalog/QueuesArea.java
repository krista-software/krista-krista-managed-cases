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
import javax.inject.Inject;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.Queue;
import app.krista.extensions.krista.case_orchestration.krista_managed_cases.service.QueueProvider;
import app.krista.ksdk.context.AuthorizationContext;

@Domain(id = "catEntryDomain_e4893b19-13b3-4824-93ac-7982bd9f5fb0",
        name = "Support Queues",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista",
        ecosystemVersion = "33ed3964-b012-4042-9c5d-e7103d774e9f")
public class QueuesArea {

    private final QueueProvider queueProvider;
    private final AuthorizationContext authorizationContext;

    @Inject
    public QueuesArea(QueueProvider queueProvider, AuthorizationContext authorizationContext) {
        this.queueProvider = queueProvider;
        this.authorizationContext = authorizationContext;
    }

    @CatalogRequest(
            id = "localDomainRequest_5bfddc22-c2cd-4aaa-b7de-7efde556d54a",
            name = "Get All Queues",
            description = "This request is used to get all the queues in which user is part of",
            area = "Queues",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "queues", type = "[ { _id: Text, name: Text } ]", required = false)
    public List<Map<String, Object>> getAllQueues() throws IOException {
        List<Map<String, Object>> queues = new ArrayList<>();
        for (Queue queue : queueProvider.getUserQueues(authorizationContext.getAuthorizedAccount())) {
            queues.add(Map.of("_id", queue.getQueueId(), "name", queue.getQueueName()));
        }
        return queues;
    }

}
