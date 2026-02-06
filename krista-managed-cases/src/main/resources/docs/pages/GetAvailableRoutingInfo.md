# Get Available Routing Info

## Overview

The **Get Available Routing Info** request retrieves all available routing options (queues) in the system. This is useful for presenting routing choices to users or for programmatic queue selection.

## Request Details

| Property | Value |
|----------|-------|
| **Area** | Clients |
| **Type** | QUERY_SYSTEM |
| **Retry Support** | ✅ Yes (read-only operation) |

## Input Parameters

This request has no input parameters.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Routing Info | List | Array of available routing options |

### Routing Info Structure

```
[
  {
    _id: Text,
    displayName: Text
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
    "displayName": "L1 Support"
  },
  {
    "_id": "queue_l2support",
    "displayName": "L2 Support"
  },
  {
    "_id": "queue_billing",
    "displayName": "Billing Support"
  }
]
```

## Business Rules

- Returns all queues configured in the system
- Each queue includes its unique ID and display name
- The `_id` field can be used as `Role ID` in routing info
- The `displayName` field can be used as `Role Name` in routing info

## Common Use Cases

1. **Dynamic Queue Selection**: Present users with available support options
2. **Routing Validation**: Verify a queue exists before submitting a task
3. **Admin Dashboard**: Display all configured queues

## Related Catalog Requests

- [Get Routing Info](pages/GetRoutingInfo.md) - Get details for a specific queue
- [Submit Task](pages/SubmitTask.md) - Submit a task to a queue
- [Submit Request By Agent](pages/SubmitRequestByAgent.md) - Route task to another queue

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Overview](pages/Overview.md)

