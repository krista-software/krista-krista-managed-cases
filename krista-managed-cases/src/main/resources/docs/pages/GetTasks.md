# Get Tasks

## Overview

The **Get Tasks** request retrieves a list of tasks based on status and optional filtering criteria. It can return both active and open tasks depending on the parameters provided.

## Request Details

| Property | Value |
|----------|-------|
| **Area** | Agents |
| **Type** | QUERY_SYSTEM |
| **Retry Support** | ✅ Yes (read-only operation) |

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Offset | Text | No | Pagination offset for results | `0` |
| Limit | Number | No | Maximum number of tasks to return | `10` |
| Queue Id | Text | No | Filter tasks by specific queue | `queue_l1support` |
| Status | Text | No | Filter by task status ("active" or "open") | `active` |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Tasks | List | Array of task objects matching the criteria |

### Task List Structure

```
[
  {
    _id: Text,
    inboxId: Text,
    creationTime: Date,
    message: Text,
    status: Text,
    createdBy: { _id: Text, name: Text },
    activeParticipants: [ { queue: { _id: Text, name: Text }, account: { _id: Text, name: Text } } ]
  }
]
```

## Validation Rules

| Rule | Description |
|------|-------------|
| Valid status values | Status must be "active" or "open" if provided |
| Positive limit | Limit must be a positive number |

## Filtering Logic

| Condition | Behavior |
|-----------|----------|
| Status = "active" | Returns only active tasks (tasks with agents) |
| Queue Id provided | Returns queued tasks for the specified queue |
| No filters | Returns open tasks (tasks awaiting assignment) |

## Usage Example

**Input:**
```
Offset: "0"
Limit: 10
Status: "active"
```

**Output:**
```json
[
  {
    "_id": "task_001",
    "inboxId": "inbox_S_abc123",
    "creationTime": "2024-01-15T10:30:00Z",
    "message": "Help with billing",
    "status": "active",
    "createdBy": { "_id": "client_001", "name": "John Doe" },
    "activeParticipants": [
      { "queue": { "_id": "queue_l1", "name": "L1 Support" }, "account": { "_id": "agent_001", "name": "Agent Smith" } }
    ]
  }
]
```

## Business Rules

- Results are paginated using offset and limit
- Queue-specific filtering takes precedence when Queue Id is provided
- Account cache is cleared after operation completes

## Related Catalog Requests

- [Get Task](pages/GetTask.md) - Get a single task by ID
- [Join Task](pages/JoinTask.md) - Join a task from the list
- [Leave Task](pages/LeaveTask.md) - Leave a task

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Overview](pages/Overview.md)

