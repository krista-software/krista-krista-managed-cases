# Submit Request By Agent

## Overview

The **Submit Request By Agent** request allows an agent to submit a task to a different queue. This is typically used for escalation or routing tasks to specialized support teams.

## Request Details

| Property | Value |
|----------|-------|
| **Area** | Agents |
| **Type** | CHANGE_SYSTEM |
| **Retry Support** | ⚠️ Caution (may cause duplicate submissions) |

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Task Id | Text | Yes | Unique identifier of the task | `task_abc123` |
| inbox Id | Text | Yes | Inbox identifier for the task | `inbox_S_xyz789` |
| Routing Info | FreeForm | Yes | Routing information specifying target queue | See below |

### Routing Info Structure

The Routing Info parameter is a FreeForm object containing queue identification:

```javascript
{
  "Role ID": "queue_l2support"
}
// OR
{
  "Role Name": "L2 Support"
}
```

## Output Parameters

This request does not return output parameters (void operation).

## Validation Rules

| Rule | Description |
|------|-------------|
| Task Id required | Task Id cannot be null |
| Inbox Id required | Inbox Id cannot be null |
| Routing Info required | Routing Info cannot be null |
| Valid queue | Target queue must exist |

## Error Handling

| Error Type | Condition | Message |
|------------|-----------|---------|
| NullPointerException | Missing required parameter | Parameter cannot be null |
| RuntimeException | Queue lookup or submission fails | Wrapped IOException |

## Usage Example

**Input:**
```
Task Id: "task_1f7c08ab-5cfb-4fed-bf35-e797c859aecb"
inbox Id: "inbox_S_dd2e0d8a-03cc-43b0-996f-b830a91b97e9"
Routing Info: { "Role Name": "L2 Support" }
```

**Routing Info Script Example:**
```javascript
function getRoutingInfo() {
  return { "Role Name": "L1 Support" };
}
getRoutingInfo();
```

## Business Rules

- Task is submitted to the queue identified by Role ID or Role Name
- If Role ID is present in routing info, it takes precedence over Role Name
- Account cache is cleared after operation completes
- Original task remains associated with its inbox

## Related Catalog Requests

- [Submit Task](pages/SubmitTask.md) - Client-initiated task submission
- [Join Task](pages/JoinTask.md) - Join a task
- [Get Routing Info](pages/GetRoutingInfo.md) - Get routing details

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Overview](pages/Overview.md)

