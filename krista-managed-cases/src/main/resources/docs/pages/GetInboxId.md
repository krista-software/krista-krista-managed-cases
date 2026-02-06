# Get Inbox Id

## Overview

The **Get Inbox Id** request generates or retrieves an inbox identifier for a user based on their channel and email. This inbox ID is required when submitting tasks.

## Request Details

| Property | Value |
|----------|-------|
| **Area** | Clients |
| **Type** | QUERY_SYSTEM |
| **Retry Support** | ✅ Yes (deterministic result for same inputs) |

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Channel Name | Text | No | Communication channel name (defaults to "kristasoft") | `kristasoft` |
| Email Id | Text | Yes | User's email address | `user@example.com` |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Inbox Id | Text | Generated inbox identifier |

## Validation Rules

| Rule | Description |
|------|-------------|
| Email Id required | Email Id cannot be null or empty |
| Valid email format | Email should be a valid email address |

## Default Values

| Parameter | Default Value |
|-----------|---------------|
| Channel Name | `kristasoft` |

## Usage Example

**Input:**
```
Channel Name: "kristasoft"
Email Id: "john.doe@example.com"
```

**Output:**
```
inbox_S_dd2e0d8a-03cc-43b0-996f-b830a91b97e9
```

## Business Rules

- If Channel Name is null or blank, defaults to "kristasoft"
- Creates a channel session using the SessionManager
- Returns the inbox ID from the created channel session
- Inbox ID format: `inbox_S_{uuid}` or `inbox_defnMult_{uuid}`

## Common Use Cases

1. **New User Setup**: Generate inbox ID for first-time users
2. **Task Submission**: Get inbox ID before calling Submit Task
3. **Session Management**: Create channel sessions for users

## Technical Details

The request uses the `SessionManager` to create a `ChannelSession` and retrieves the inbox ID from the session's inbox object.

```java
ChannelSession channelSession = sessionManager.createChannelSession(channelName, emailId);
return channelSession.getSessionInbox().getInboxId();
```

## Related Catalog Requests

- [Submit Task](pages/SubmitTask.md) - Uses inbox ID to submit tasks
- [Get Available Routing Info](pages/GetAvailableRoutingInfo.md) - Get routing options

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Overview](pages/Overview.md)

