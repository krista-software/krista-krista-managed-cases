# Get All Queues

## Overview

The **Get All Queues** request retrieves all queues that the current user is a member of. This is based on the user's role assignments and authorization context.

## Request Details

| Property | Value |
|----------|-------|
| **Area** | Queues |
| **Type** | QUERY_SYSTEM |
| **Retry Support** | ✅ Yes (read-only operation) |

## Input Parameters

This request has no input parameters.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| queues | List | Array of queue objects the user belongs to |

### Queue Structure

```
[
  {
    _id: Text,
    name: Text
  }
]
```

## Usage Example

**Input:**
No input required.

**Output:**
```json
[
  {
    "_id": "queue_l1support",
    "name": "L1 Support"
  },
  {
    "_id": "queue_l2support",
    "name": "L2 Support"
  }
]
```

## Business Rules

- Returns only queues the authorized user is a member of
- Queue membership is determined by the user's roles
- Uses the AuthorizationContext to get the current user's account
- Empty list is returned if user has no queue memberships

## Technical Details

The request uses:
- `AuthorizationContext` to get the authorized account
- `QueueProvider.getUserQueues()` to retrieve queues for the user

```java
for (Queue queue : queueProvider.getUserQueues(authorizationContext.getAuthorizedAccount())) {
    queues.add(Map.of("_id", queue.getQueueId(), "name", queue.getQueueName()));
}
```

## Common Use Cases

1. **Agent Dashboard**: Display queues an agent can work on
2. **Queue Selection**: Let agents choose which queue to monitor
3. **Permission Check**: Verify user has access to specific queues

## Difference from Get Available Routing Info

| Get All Queues | Get Available Routing Info |
|----------------|---------------------------|
| Returns user's queues only | Returns all system queues |
| Based on user authorization | No authorization filter |
| For agent queue selection | For routing/submission |

## Related Catalog Requests

- [Get Available Routing Info](pages/GetAvailableRoutingInfo.md) - Get all system queues
- [Get Tasks](pages/GetTasks.md) - Get tasks from queues
- [Join Task](pages/JoinTask.md) - Join a task from a queue

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Overview](pages/Overview.md)

