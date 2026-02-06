# Join Task

## Overview

The **Join Task** request allows an agent to join an existing task and become an active participant. Once joined, the agent can work on the task and interact with the client.

## Request Details

| Property | Value |
|----------|-------|
| **Area** | Agents |
| **Type** | CHANGE_SYSTEM |
| **Retry Support** | ✅ Yes (idempotent join operation) |

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Task Id | Text | Yes | Unique identifier of the task to join | `task_abc123` |
| Account Id | Text | Yes | Unique identifier of the agent's account | `account_xyz789` |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Task | Multi Field | Complete task object with updated status |

### Task Output Structure

```
{
  _id: Text,
  inboxId: Text,
  creationTime: Date,
  message: Text,
  status: Text,
  createdBy: { _id: Text, name: Text },
  activeParticipants: [ { queue: { _id: Text, name: Text }, account: { _id: Text, name: Text } } ]
}
```

## Validation Rules

| Rule | Description |
|------|-------------|
| Task Id required | Task Id cannot be null or empty |
| Account Id required | Account Id cannot be null or empty |
| Task must exist | The specified task must exist in the system |

## Error Handling

| Error Type | Condition | Message |
|------------|-----------|---------|
| IllegalStateException | Task not found | "Incorrect task ID. Error in joining." |
| NullPointerException | Missing required parameter | Parameter cannot be null |

## Usage Example

**Input:**
```
Task Id: "task_5cc35881-c539-4315-89c7-eb1889ef01f2"
Account Id: "account_user123"
```

**Output:**
```json
{
  "_id": "task_5cc35881-c539-4315-89c7-eb1889ef01f2",
  "inboxId": "inbox_S_dd2e0d8a-03cc-43b0-996f-b830a91b97e9",
  "creationTime": "2024-01-15T10:30:00Z",
  "message": "Need help with order",
  "status": "active",
  "createdBy": { "_id": "client_001", "name": "John Doe" },
  "activeParticipants": [
    { "queue": { "_id": "queue_l1", "name": "L1 Support" }, "account": { "_id": "account_user123", "name": "Agent Smith" } }
  ]
}
```

## Business Rules

- Task status changes to "active" after an agent joins
- Agent is added to the activeParticipants list
- Account cache is cleared after operation completes

## Related Catalog Requests

- [Leave Task](pages/LeaveTask.md) - Leave a task after completion
- [Get Task](pages/GetTask.md) - Retrieve task details
- [Get Tasks](pages/GetTasks.md) - List all tasks

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Overview](pages/Overview.md)

