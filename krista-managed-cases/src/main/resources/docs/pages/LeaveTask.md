# Leave Task

## Overview

The **Leave Task** request allows an agent to leave a task they have previously joined. This removes the agent from the active participants list.

## Request Details

| Property | Value |
|----------|-------|
| **Area** | Agents |
| **Type** | CHANGE_SYSTEM |
| **Retry Support** | ✅ Yes (idempotent leave operation) |

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Task Id | Text | Yes | Unique identifier of the task to leave | `task_abc123` |
| Account Id | Text | Yes | Unique identifier of the agent's account | `account_xyz789` |

## Output Parameters

This request does not return output parameters (void operation).

## Validation Rules

| Rule | Description |
|------|-------------|
| Task Id required | Task Id cannot be null or empty |
| Account Id required | Account Id cannot be null or empty |
| Task must exist | The specified task must exist in the system |

## Error Handling

| Error Type | Condition | Message |
|------------|-----------|---------|
| IllegalStateException | Task not found | "Incorrect task ID. Error in leaving." |
| NullPointerException | Missing required parameter | Parameter cannot be null |
| IOException | System error during operation | I/O error message |

## Usage Example

**Input:**
```
Task Id: "task_32924d9a-1aeb-4a1b-b222-d0a26d06c4c0"
Account Id: "account_agent123"
```

**Result:**
- Agent is removed from the task's activeParticipants list
- Task cache is updated to reflect the change

## Business Rules

- Agent must be a participant in the task to leave it
- Task remains in the system after agent leaves
- If all agents leave, task may return to open status
- Account cache is cleared after operation completes
- Task is removed from cached task provider after leaving

## Related Catalog Requests

- [Join Task](pages/JoinTask.md) - Join a task
- [Get Task](pages/GetTask.md) - View task details
- [Get Tasks](pages/GetTasks.md) - List all tasks

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Overview](pages/Overview.md)

