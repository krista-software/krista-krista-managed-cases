# Get Routing Info

## Overview

The **Get Routing Info** request retrieves detailed routing information for a specific queue identified by its routing ID. This returns a FreeForm object containing the queue's Role ID and Role Name.

## Request Details

| Property | Value |
|----------|-------|
| **Area** | Clients |
| **Type** | QUERY_SYSTEM |
| **Retry Support** | ✅ Yes (read-only operation) |

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Routing Id | Text | Yes | Unique identifier of the queue | `queue_l1support` |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Routing Info | FreeForm | Queue details as a FreeForm object |

### Routing Info Structure

```
{
  "Role ID": Text,
  "Role Name": Text
}
```

## Validation Rules

| Rule | Description |
|------|-------------|
| Routing Id required | Routing Id cannot be null |
| Queue must exist | The specified queue must exist in the system |

## Error Handling

| Error Type | Condition | Message |
|------------|-----------|---------|
| NullPointerException | Routing Id is null | Parameter cannot be null |
| IOException | Queue lookup fails | System error message |

## Usage Example

**Input:**
```
Routing Id: "queue_l1support"
```

**Output:**
```json
{
  "Role ID": "queue_l1support",
  "Role Name": "L1 Support"
}
```

## Business Rules

- Returns both Role ID and Role Name for the specified queue
- The returned FreeForm can be used directly in Submit Task or Submit Request By Agent
- Queue lookup is performed using the QueueProvider service

## Common Use Cases

1. **Queue Details**: Get full details of a queue before routing
2. **Routing Preparation**: Build routing info for task submission
3. **Validation**: Verify queue exists and get its display name

## Related Catalog Requests

- [Get Available Routing Info](pages/GetAvailableRoutingInfo.md) - List all queues
- [Submit Task](pages/SubmitTask.md) - Submit a task using routing info
- [Submit Request By Agent](pages/SubmitRequestByAgent.md) - Route task to queue

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Overview](pages/Overview.md)

