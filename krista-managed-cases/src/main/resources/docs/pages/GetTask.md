# Get Task

## Overview

The **Get Task** request retrieves details of a single task by its unique identifier. This is useful for viewing the current state of a specific task.

## Request Details

| Property | Value |
|----------|-------|
| **Area** | Agents |
| **Type** | QUERY_SYSTEM |
| **Retry Support** | ✅ Yes (read-only operation) |

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Task Id | Text | Yes | Unique identifier of the task | `task_abc123` |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Task | Multi Field | Complete task object with current status |

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
| Task must exist | The specified task must exist in the system |

## Error Handling

| Error Type | Condition | Message |
|------------|-----------|---------|
| NullPointerException | Task Id is null | Parameter cannot be null |
| IOException | Task retrieval fails | System error during task lookup |

## Usage Example

**Input:**
```
Task Id: "task_025f2ac6-8dea-446e-bd83-c1f6b4d151e5"
```

**Output:**
```json
{
  "_id": "task_025f2ac6-8dea-446e-bd83-c1f6b4d151e5",
  "inboxId": "inbox_S_dd2e0d8a-03cc-43b0-996f-b830a91b97e9",
  "creationTime": "2024-01-15T10:30:00Z",
  "message": "Need assistance with product return",
  "status": "active",
  "createdBy": { "_id": "client_002", "name": "Jane Smith" },
  "activeParticipants": [
    { "queue": { "_id": "queue_l2", "name": "L2 Support" }, "account": { "_id": "agent_002", "name": "Agent Johnson" } }
  ]
}
```

## Business Rules

- Returns the current state of the task including all participants
- Status field reflects current task state ("open", "active", etc.)
- Account cache is cleared after operation completes

## Related Catalog Requests

- [Get Tasks](pages/GetTasks.md) - List multiple tasks
- [Join Task](pages/JoinTask.md) - Join this task
- [Leave Task](pages/LeaveTask.md) - Leave this task

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Overview](pages/Overview.md)

