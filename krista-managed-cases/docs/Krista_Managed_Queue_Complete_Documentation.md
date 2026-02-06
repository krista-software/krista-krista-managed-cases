# Krista Managed Queue Extension - Complete Documentation

**Version**: 3.2.4  
**Ecosystem**: Krista  
**Domain**: Support Queues (Case Orchestration)  
**Java Version**: 21  
**Requires Workspace Admin Rights**: Yes

---

## Table of Contents

1. [Extension Overview](#1-extension-overview)
2. [Architecture (5-Layer Breakdown)](#2-architecture-5-layer-breakdown)
3. [Configuration and Setup](#3-configuration-and-setup)
4. [Catalog Request Reference](#4-catalog-request-reference)
5. [Data Structures](#5-data-structures)
6. [Error Handling Guide](#6-error-handling-guide)
7. [Performance Guidelines](#7-performance-guidelines)
8. [Troubleshooting Guide](#8-troubleshooting-guide)
9. [Technical Implementation Details](#9-technical-implementation-details)

---

## 1. Extension Overview

### 1.1 Purpose

The **Krista Managed Queue** extension is a case orchestration solution that manages customer support requests by connecting end users with support agents. It uses role-based queue assignments to route and manage tasks efficiently within the Krista platform.

### 1.2 Key Capabilities

| Capability | Description |
|------------|-------------|
| **Role-Based Queue Management** | Queues are mapped to Krista roles; agents with specific roles can access corresponding queues |
| **Task Lifecycle Management** | Full lifecycle: create → submit → join → work → leave |
| **Real-Time Notifications** | Push notifications for new tasks, joins, leaves, and updates |
| **Multi-Agent Support** | Multiple agents can participate in a single task |
| **Session Management** | Integrates with Krista's session and inbox system |
| **Caching Layer** | Performance-optimized with cached task and account providers |

### 1.3 Functional Areas

The extension organizes catalog requests into three functional areas:

| Area | Purpose | Request Count |
|------|---------|---------------|
| **Agents** | Operations performed by support agents | 5 requests |
| **Clients** | Operations for end users and routing | 4 requests |
| **Queues** | Queue-level operations | 1 request |

### 1.4 Extension Identifiers

```
Extension Name: Krista Managed Queue
Extension Package: app.krista.extensions.krista.case_orchestration.krista_managed_cases
Domain ID: catEntryDomain_e4893b19-13b3-4824-93ac-7982bd9f5fb0
Ecosystem ID: catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9
```

---

## 2. Architecture (5-Layer Breakdown)

### Layer 1: Extension Entry Point

**Class**: `KristaManagedQueuesExtension`

| Component | Description |
|-----------|-------------|
| **Annotations** | `@Extension`, `@Java(JAVA_21)`, `@Field`, `@StaticResource` |
| **Configuration Field** | `roles_separated_by_comma` - comma-separated list of role names |
| **Custom Tabs** | Provides "Documentation" tab pointing to `static/docs` |
| **Validation** | Validates configured roles exist in workspace via `AttributeValidator` |

### Layer 2: Catalog Request Areas

| Class | Area | Requests |
|-------|------|----------|
| `AgentsArea` | Agents | Join Task, Get Tasks, Get Task, Submit Request By Agent, Leave Task |
| `ClientsArea` | Clients | Get Available Routing Info, Get Routing Info, Submit Task, Get Inbox Id |
| `QueuesArea` | Queues | Get All Queues |

### Layer 3: Service Layer (Business Logic)

| Interface | Implementation | Purpose |
|-----------|----------------|---------|
| `TaskManager` | `TaskManagerImpl` | Task CRUD operations, lifecycle management |
| `QueueProvider` | `QueueProviderImpl` | Queue lookup, user queue retrieval |
| `Task` | `TaskImpl` | Task entity with join/leave operations |
| `Queue` | `QueueImpl` | Queue entity with task submission |

### Layer 4: Caching Layer

| Class | Purpose |
|-------|---------|
| `CachedTaskProvider` | Thread-safe task caching using `ConcurrentHashMap` |
| `CachedAccountProvider` | Account caching with request-scoped clearing |

### Layer 5: Storage Layer

| Class | Purpose |
|-------|---------|
| `TaskStore` | Persistent task storage |
| `QueueStore` | Queue-to-task mapping storage |
| `ActiveTaskStore` | Active task tracking per account |
| `TaskDto` | Task data transfer object |
| `AccountDto` | Account data transfer object |

---

## 3. Configuration and Setup

### 3.1 Extension Configuration

**Configuration Parameter:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roles_separated_by_comma` | Text | Yes | Comma-separated list of role names that define support queues |

**Example Configuration:**
```
L1 Support, L2 Support, L3 Support, Billing Support
```

### 3.2 Validation Rules

The extension validates configuration on save:

1. **Role Existence Check**: Each role name must exist in the Krista workspace
2. **Non-Empty Validation**: At least one role must be specified
3. **Whitespace Handling**: Role names are trimmed before validation

**Validation Error:**
```
IllegalStateException: "Role name does not exist in workspace: {roleName}"
```

### 3.3 Security Requirements

| Requirement | Description |
|-------------|-------------|
| **Workspace Admin Rights** | Extension requires `requireWorkspaceAdminRights = true` |
| **Trust Extension** | Must enable "Trust extension" toggle in Security panel |
| **Role Assignment** | Agents must have appropriate roles assigned in Krista Studio |

### 3.4 Prerequisites

1. Valid Krista workspace with admin access
2. Roles created in Krista Studio matching configuration
3. User accounts assigned to appropriate roles
4. Extension trusted in workspace security settings

---

## 4. Catalog Request Reference

### 4.1 Agents Area Requests

#### 4.1.1 Join Task

**Request ID**: `localDomainRequest_5cc35881-c539-4315-89c7-eb1889ef01f2`

| Property | Value |
|----------|-------|
| **Name** | Join Task |
| **Area** | Agents |
| **Type** | CHANGE_SYSTEM |
| **Retry Support** | ✅ Yes (idempotent) |

**Input Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| Task Id | Text | Yes | Unique identifier of the task to join |
| Account Id | Text | Yes | Unique identifier of the agent's account |

**Output Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| Task | MultiField | Complete task object with updated status |

**Output Structure:**
```json
{
  "_id": "Text",
  "inboxId": "Text",
  "creationTime": "Date (epoch ms)",
  "message": "Text",
  "status": "Text (always 'active')",
  "createdBy": { "_id": "Text", "name": "Text" },
  "activeParticipants": [
    { "queue": { "_id": "Text", "name": "Text" }, "account": { "_id": "Text", "name": "Text" } }
  ]
}
```

**Technical Implementation:**
1. Validates Task Id and Account Id are not null
2. Retrieves task from `CachedTaskProvider`
3. Synchronizes on task object for thread safety
4. Calls `task.join(account)` which:
   - Adds agent to task's active participants
   - Updates `ActiveTaskStore` with agent-task mapping
   - Sends `TYPE_TASK_JOINED` notification to queue role
   - Sends `EVENT_TYPE_JOIN_TASK` event message to inbox
   - Removes task from queue store
   - Updates inbox ACL to include agent
5. Removes task from cache after join
6. Clears account provider cache

**Errors:**

| Error | Condition | Solution |
|-------|-----------|----------|
| `NullPointerException` | Task Id or Account Id is null | Provide valid non-null values |
| `IllegalStateException: "Incorrect task ID. Error in joining."` | Task not found in cache/store | Verify task exists using Get Task first |
| `IllegalStateException: "Invalid request"` | No queue waiting for participant | Task may already have agent or be in wrong state |

---

#### 4.1.2 Get Tasks

**Request ID**: `localDomainRequest_58c80c0b-cb99-4090-b008-051efbc94cc5`

| Property | Value |
|----------|-------|
| **Name** | Get Tasks |
| **Area** | Agents |
| **Type** | QUERY_SYSTEM |
| **Retry Support** | ✅ Yes (read-only) |

**Input Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| Offset | Text | No | null | Pagination offset (task ID to start after) |
| Limit | Number | No | - | Maximum number of tasks to return |
| Queue Id | Text | No | null | Filter by specific queue |
| Status | Text | No | null | Filter: "active" or null for open tasks |

**Output Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| Tasks | List | Array of task objects |

**Filtering Logic:**

| Condition | Behavior |
|-----------|----------|
| Status = "active" | Returns agent's active tasks (tasks they've joined) |
| Queue Id provided | Returns queued tasks for specific queue |
| No filters | Returns open tasks from all queues user has access to |

**Technical Implementation:**
1. If status is "active": calls `taskManager.getActiveTasks(offset, limit)`
2. If queueId provided: calls `queueProvider.getQueue(queueId).getQueuedTasks(offset, limit)`
3. Otherwise: calls `taskManager.getOpenTasks(offset, limit)` which:
   - Iterates through user's roles
   - For each role with a queue, gets all queued tasks
   - Filters out tasks where user is already a participant
   - Applies pagination

---

#### 4.1.3 Get Task

**Request ID**: `localDomainRequest_025f2ac6-8dea-446e-bd83-c1f6b4d151e5`

| Property | Value |
|----------|-------|
| **Name** | Get Task |
| **Area** | Agents |
| **Type** | QUERY_SYSTEM |
| **Retry Support** | ✅ Yes (read-only) |

**Input Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| Task Id | Text | Yes | Unique identifier of the task |

**Output Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| Task | MultiField | Complete task object |

**Technical Implementation:**
1. Validates Task Id is not null
2. Calls `taskManager.getTask(taskId)`
3. Reconstructs Task object from TaskDto
4. Returns task with status "not set"

---

#### 4.1.4 Submit Request By Agent

**Request ID**: `localDomainRequest_1f7c08ab-5cfb-4fed-bf35-e797c859aecb`

| Property | Value |
|----------|-------|
| **Name** | Submit Request By Agent |
| **Area** | Agents |
| **Type** | CHANGE_SYSTEM |
| **Retry Support** | ⚠️ Caution (may cause duplicate submissions) |

**Input Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| Task Id | Text | Yes | Unique identifier of the task |
| inbox Id | Text | Yes | Inbox identifier for the task |
| Routing Info | FreeForm | Yes | Target queue information |

**Routing Info Structure:**
```json
{ "Role ID": "queue_id" }
// OR
{ "Role Name": "L2 Support" }
```

**Output Parameters:** None (void operation)

**Technical Implementation:**
1. Validates all parameters are not null
2. Extracts queue identifier from Routing Info (Role ID takes precedence over Role Name)
3. Gets task from TaskManager
4. Looks up queue using `queueProvider.lookupQueue(queueIdOrName)`
5. Submits task to queue via `queue.submitTask(task)`

**Use Case:** Agent escalating task to another support tier

---

#### 4.1.5 Leave Task

**Request ID**: `localDomainRequest_32924d9a-1aeb-4a1b-b222-d0a26d06c4c0`

| Property | Value |
|----------|-------|
| **Name** | Leave Task |
| **Area** | Agents |
| **Type** | CHANGE_SYSTEM |
| **Retry Support** | ✅ Yes (idempotent) |

**Input Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| Task Id | Text | Yes | Unique identifier of the task to leave |
| Account Id | Text | Yes | Unique identifier of the agent's account |

**Output Parameters:** None (void operation)

**Technical Implementation:**
1. Validates Task Id and Account Id are not null
2. Gets account from CachedAccountProvider
3. Gets task from CachedTaskProvider
4. Synchronizes on task object
5. Calls `task.leave(account)` which:
   - Removes agent from task's active participants
   - Updates ActiveTaskStore
   - Sends `EVENT_TYPE_LEAVE_TASK` event message
   - Removes agent from inbox ACL
   - Sends update notifications to remaining participants
6. Removes task from cache

**Errors:**

| Error | Condition | Solution |
|-------|-----------|----------|
| `IllegalStateException: "Incorrect task ID. Error in leaving."` | Task not found | Verify task exists |
| `NoSuchElementException` | Task DTO not found in store | Task may have been deleted |

---

### 4.2 Clients Area Requests

#### 4.2.1 Submit Task

**Request ID**: `localDomainRequest_4671be3f-1e3f-4354-aada-c7d3a263b97c`

| Property | Value |
|----------|-------|
| **Name** | Submit Task |
| **Area** | Clients |
| **Type** | CHANGE_SYSTEM |
| **Retry Support** | ❌ No (creates new task) |

**Input Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| Message | Text | Yes | The support request message |
| Inbox Id | Text | Yes | Inbox identifier (must start with `inbox_S_` or `inbox_defnMult_`) |
| routingInfo | FreeForm | Yes | Target queue information |

**Output Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| Task | MultiField | The newly created task object |

**Technical Implementation:**
1. Extracts queue identifier from routingInfo
2. Looks up queue
3. Validates inbox Id format
4. Checks if user already has open task (returns existing if found)
5. Checks if user already has active task (throws error if found)
6. Creates new task with UUID
7. Stores task in TaskStore
8. Submits task to queue (triggers notification)
9. Returns task with status "open"

**Errors:**

| Error | Condition | Solution |
|-------|-----------|----------|
| `IllegalArgumentException: "Invalid inbox Id: {inboxId}"` | Inbox Id doesn't start with valid prefix | Use Get Inbox Id to obtain valid inbox |
| `IllegalStateException: "You have already connected to {agent}"` | User has active task with agent | Complete or leave existing task first |
| `IllegalStateException: "CreateTask: We are can't find the inbox: {inboxId}"` | Inbox not found | Verify inbox exists |

---

#### 4.2.2 Get Available Routing Info

**Request ID**: `localDomainRequest_40c6a8a4-e6c8-4c95-a354-c9eed7fb764a`

| Property | Value |
|----------|-------|
| **Name** | Get Available Routing Info |
| **Area** | Clients |
| **Type** | QUERY_SYSTEM |
| **Retry Support** | ✅ Yes (read-only) |

**Input Parameters:** None

**Output Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| Routing Info | List | Array of available routing options |

**Output Structure:**
```json
[
  { "_id": "queue_id", "displayName": "L1 Support" },
  { "_id": "queue_id2", "displayName": "L2 Support" }
]
```

**Technical Implementation:**
1. Calls `queueProvider.getAllQueues()`
2. Maps each queue to `{ _id, displayName }` format
3. Returns list of all configured queues

---

#### 4.2.3 Get Routing Info

**Request ID**: `localDomainRequest_b1c41851-3c18-469a-b182-56fba1df1356`

| Property | Value |
|----------|-------|
| **Name** | Get Routing Info |
| **Area** | Clients |
| **Type** | QUERY_SYSTEM |
| **Retry Support** | ✅ Yes (read-only) |

**Input Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| Routing Id | Text | Yes | Queue identifier |

**Output Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| Routing Info | FreeForm | Queue details |

**Output Structure:**
```json
{
  "Role ID": "queue_id",
  "Role Name": "L1 Support"
}
```

---

#### 4.2.4 Get Inbox Id

**Request ID**: `localDomainRequest_1c9530a8-9e80-440b-927d-92a44ddf8087`

| Property | Value |
|----------|-------|
| **Name** | Get Inbox Id |
| **Area** | Clients |
| **Type** | QUERY_SYSTEM |
| **Retry Support** | ✅ Yes (deterministic) |

**Input Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| Channel Name | Text | No | "kristasoft" | Communication channel name |
| Email Id | Text | Yes | - | User's email address |

**Output Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| Inbox Id | Text | Generated inbox identifier |

**Technical Implementation:**
1. Defaults Channel Name to "kristasoft" if null/blank
2. Creates channel session via `sessionManager.createChannelSession(channelName, emailId)`
3. Returns inbox ID from session inbox

---

### 4.3 Queues Area Requests

#### 4.3.1 Get All Queues

**Request ID**: `localDomainRequest_5bfddc22-c2cd-4aaa-b7de-7efde556d54a`

| Property | Value |
|----------|-------|
| **Name** | Get All Queues |
| **Area** | Queues |
| **Type** | QUERY_SYSTEM |
| **Retry Support** | ✅ Yes (read-only) |

**Input Parameters:** None

**Output Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| queues | List | Array of queues user belongs to |

**Output Structure:**
```json
[
  { "_id": "queue_id", "name": "L1 Support" }
]
```

**Technical Implementation:**
1. Gets authorized account from AuthorizationContext
2. Calls `queueProvider.getUserQueues(account)`
3. Maps each queue to `{ _id, name }` format

**Difference from Get Available Routing Info:**

| Get All Queues | Get Available Routing Info |
|----------------|---------------------------|
| Returns only user's queues | Returns all system queues |
| Based on user authorization | No authorization filter |
| For agent queue selection | For routing/submission |

---

## 5. Data Structures

### 5.1 Task Object

```json
{
  "_id": "string (UUID)",
  "inboxId": "string (inbox_S_* or inbox_defnMult_*)",
  "creationTime": "number (epoch milliseconds)",
  "message": "string",
  "status": "string (open|active|not set)",
  "createdBy": {
    "_id": "string (account ID)",
    "name": "string (person name)"
  },
  "activeParticipants": [
    {
      "queue": { "_id": "string", "name": "string" },
      "account": { "_id": "string", "name": "string" } // null if waiting
    }
  ]
}
```

### 5.2 Routing Info Object

```json
{
  "Role ID": "string (queue/role ID)",
  "Role Name": "string (queue/role display name)"
}
```

### 5.3 Queue Object

```json
{
  "_id": "string (role ID)",
  "name": "string (role name)"
}
```

### 5.4 Internal DTOs

**TaskDto:**
- taskId: String
- sessionId: String (inbox ID)
- message: String
- creationTime: long
- creator: AccountDto
- activeParticipants: Map<String, Set<AccountDto>>

**AccountDto:**
- accountId: String
- accountName: String

**Special Value:** `TaskDto.IN_QUEUE` - placeholder AccountDto indicating task is waiting in queue

---

## 6. Error Handling Guide

### 6.1 Common Errors and Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| `NullPointerException` | Required parameter is null | Ensure all required parameters are provided |
| `IllegalStateException: "Role name does not exist in workspace: {role}"` | Invalid role in configuration | Verify role exists in Krista Studio |
| `IllegalStateException: "Incorrect task ID. Error in joining/leaving."` | Task not found | Use Get Task to verify task exists |
| `IllegalStateException: "Invalid request"` | Task not in joinable state | Task may already have agent or be completed |
| `IllegalStateException: "You have already connected to {agent}"` | User has active task | Complete existing task before creating new one |
| `IllegalArgumentException: "Invalid inbox Id: {id}"` | Invalid inbox format | Use Get Inbox Id to obtain valid inbox |
| `NoSuchElementException` | Task/Account not found in store | Data may have been deleted |
| `IOException` | Storage/network error | Retry operation; check system connectivity |
| `RuntimeException` (wrapping IOException) | Queue submission failed | Check queue configuration and connectivity |

### 6.2 Error Recovery Strategies

1. **Retry for Transient Errors**: IOException and network-related errors are often transient
2. **Validate Before Operations**: Use Get Task before Join/Leave to verify state
3. **Check User State**: Use Get Tasks with status="active" to check for existing active tasks
4. **Verify Configuration**: Ensure roles in configuration match Krista Studio roles exactly

---

## 7. Performance Guidelines

### 7.1 Caching Behavior

| Cache | Scope | Clearing |
|-------|-------|----------|
| CachedTaskProvider | Request-scoped | Cleared after join/leave operations |
| CachedAccountProvider | Request-scoped | Cleared after each catalog request |

### 7.2 Pagination

- Use `Offset` and `Limit` parameters for large result sets
- Offset is the last task ID from previous page
- Recommended page size: 10-50 items

### 7.3 Synchronization

- Task join/leave operations are synchronized on task object
- Thread-safe for concurrent access
- May cause brief blocking during high-concurrency scenarios

### 7.4 Notification Overhead

Each task operation triggers notifications:
- Join: 2 notifications (queue role + inbox event)
- Leave: 2 notifications (inbox event + participant updates)
- Submit: 1 notification (queue role)

### 7.5 Limitations

| Limitation | Value | Notes |
|------------|-------|-------|
| Max roles per configuration | No hard limit | Performance may degrade with 100+ roles |
| Max active participants per task | No hard limit | Practical limit ~50 for UI performance |
| Task message size | Platform limit | Typically 64KB |

---

## 8. Troubleshooting Guide

### 8.1 Task Not Appearing in Queue

**Symptoms:** Task submitted but agents don't see it

**Checklist:**
1. Verify routing info contains valid Role ID or Role Name
2. Check agent has the corresponding role assigned
3. Verify extension configuration includes the role
4. Check for errors in Submit Task response

### 8.2 Agent Cannot Join Task

**Symptoms:** Join Task fails with "Invalid request"

**Checklist:**
1. Verify task is in "open" state (not already active)
2. Check task has queue waiting for participant
3. Verify agent account ID is valid
4. Check agent has role matching task's queue

### 8.3 Notifications Not Received

**Symptoms:** Real-time updates not working

**Checklist:**
1. Verify WebSocket connection is active
2. Check user is subscribed to correct role/account channels
3. Verify NotificationManager is properly configured

### 8.4 Duplicate Tasks Created

**Symptoms:** Multiple tasks for same user

**Checklist:**
1. Check if Submit Task is being called multiple times
2. Verify client-side retry logic isn't creating duplicates
3. Note: Extension checks for existing open/active tasks but race conditions possible

### 8.5 Configuration Validation Fails

**Symptoms:** "Role name does not exist in workspace"

**Checklist:**
1. Verify exact role name spelling (case-sensitive)
2. Check role exists in Krista Studio
3. Remove extra whitespace from role names
4. Ensure roles are comma-separated

---

## 9. Technical Implementation Details

### 9.1 Dependency Injection

The extension uses HK2 dependency injection:
- `@Inject` for constructor injection
- `@Service` for service registration
- `@ContractsProvided` for interface binding
- `@Named` for qualifier-based injection

### 9.2 Key Krista SDK Dependencies

| Interface | Purpose |
|-----------|---------|
| `RoleProvider` | Role lookup and validation |
| `AuthorizationContext` | Current user context |
| `SessionManager` | Channel session creation |
| `InboxManager` | Inbox operations |
| `NotificationManager` | Push notifications |
| `RuntimeContext` | Workspace context |

### 9.3 Notification Types

| Type | Constant | Trigger |
|------|----------|---------|
| New Task | `Notification.TYPE_NEW_TASK` | Task submitted to queue |
| Task Joined | `Notification.TYPE_TASK_JOINED` | Agent joins task |
| Task Update | `Notification.TYPE_TASK_UPDATE` | Task state changes |

### 9.4 Event Message Types

| Type | Constant | Trigger |
|------|----------|---------|
| Join Task | `EventMessage.EVENT_TYPE_JOIN_TASK` | Agent joins |
| Leave Task | `EventMessage.EVENT_TYPE_LEAVE_TASK` | Agent leaves |

### 9.5 ACL Management

- On join: `inbox.updateAcl(List.of("account:" + accountId))`
- On leave: `inbox.removeAcl(List.of("account:" + accountId))`

### 9.6 Thread Safety

- `CachedTaskProvider` uses `ConcurrentHashMap`
- Task operations synchronized on task instance
- Account provider cleared after each request

---

## Appendix A: Request ID Quick Reference

| Request Name | Request ID |
|--------------|------------|
| Join Task | `localDomainRequest_5cc35881-c539-4315-89c7-eb1889ef01f2` |
| Get Tasks | `localDomainRequest_58c80c0b-cb99-4090-b008-051efbc94cc5` |
| Get Task | `localDomainRequest_025f2ac6-8dea-446e-bd83-c1f6b4d151e5` |
| Submit Request By Agent | `localDomainRequest_1f7c08ab-5cfb-4fed-bf35-e797c859aecb` |
| Leave Task | `localDomainRequest_32924d9a-1aeb-4a1b-b222-d0a26d06c4c0` |
| Get Available Routing Info | `localDomainRequest_40c6a8a4-e6c8-4c95-a354-c9eed7fb764a` |
| Get Routing Info | `localDomainRequest_b1c41851-3c18-469a-b182-56fba1df1356` |
| Submit Task | `localDomainRequest_4671be3f-1e3f-4354-aada-c7d3a263b97c` |
| Get Inbox Id | `localDomainRequest_1c9530a8-9e80-440b-927d-92a44ddf8087` |
| Get All Queues | `localDomainRequest_5bfddc22-c2cd-4aaa-b7de-7efde556d54a` |

---

## Appendix B: Workflow Examples

### B.1 Complete Support Flow

```
1. Client: Get Inbox Id (channelName, emailId) → inboxId
2. Client: Get Available Routing Info → list of queues
3. Client: Submit Task (message, inboxId, routingInfo) → task (status: open)
4. Agent: Get Tasks (status: null) → list of open tasks
5. Agent: Join Task (taskId, accountId) → task (status: active)
6. [Agent works with client via inbox]
7. Agent: Leave Task (taskId, accountId) → void
```

### B.2 Task Escalation Flow

```
1. Agent: Get Task (taskId) → current task state
2. Agent: Get Available Routing Info → list of queues
3. Agent: Submit Request By Agent (taskId, inboxId, routingInfo) → void
4. [Task appears in new queue]
5. New Agent: Join Task (taskId, accountId) → task
```

---

*Document generated for LLM consumption. Contains complete technical details for answering user queries about the Krista Managed Queue extension.*

