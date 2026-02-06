# Submit Task

## Overview

The **Submit Task** request creates a new task in the open state. This is typically used by clients/end-users to submit support requests that will be routed to the appropriate queue.

## Request Details

| Property | Value |
|----------|-------|
| **Area** | Clients |
| **Type** | CHANGE_SYSTEM |
| **Retry Support** | ❌ No (creates new task each time) |

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Message | Text | Yes | The support request message | `Need help with my order` |
| Inbox Id | Text | Yes | Inbox identifier for the task | `inbox_S_abc123` |
| routingInfo | FreeForm | Yes | Routing information for queue assignment | See below |

### Routing Info Structure

```javascript
{
  "Role ID": "queue_l1support"
}
// OR
{
  "Role Name": "L1 Support"
}
```

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Task | Multi Field | The newly created task object |

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
| Message required | Message cannot be null or empty |
| Inbox Id required | Inbox Id cannot be null or empty |
| Valid Inbox Id format | Must start with `inbox_S_` or `inbox_defnMult_` |
| Routing Info required | Routing Info cannot be null |
| Valid queue | Target queue must exist |

## Error Handling

| Error Type | Condition | Message |
|------------|-----------|---------|
| IllegalArgumentException | Invalid inbox Id format | "Invalid inbox Id: {inboxId}" |
| IOException | Task creation or queue submission fails | System error message |

## Usage Example

**Input:**
```
Message: "I need help with my recent order #12345"
Inbox Id: "inbox_S_dd2e0d8a-03cc-43b0-996f-b830a91b97e9"
routingInfo: { "Role Name": "L1 Support" }
```

**Output:**
```json
{
  "_id": "task_new123",
  "inboxId": "inbox_S_dd2e0d8a-03cc-43b0-996f-b830a91b97e9",
  "creationTime": "2024-01-15T14:30:00Z",
  "message": "I need help with my recent order #12345",
  "status": "open",
  "createdBy": { "_id": "client_001", "name": "Customer Name" },
  "activeParticipants": []
}
```

## Business Rules

- New task is created with status "open"
- Task is submitted to the queue identified by routing info
- Role ID takes precedence over Role Name if both are present
- Inbox Id must follow valid format patterns

## Related Catalog Requests

- [Get Inbox Id](pages/GetInboxId.md) - Get an inbox identifier
- [Get Available Routing Info](pages/GetAvailableRoutingInfo.md) - List available queues
- [Join Task](pages/JoinTask.md) - Agent joins the submitted task

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Overview](pages/Overview.md)

