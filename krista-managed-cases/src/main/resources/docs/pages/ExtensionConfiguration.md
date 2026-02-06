# Extension Configuration

## Overview

This page describes how to configure the Krista Managed Queue extension in Krista Studio.

## Configuration Steps

![Extension Configuration](../_media/ExtensionConfiguration_config.png)

### Step 1: Configure Roles

1. Navigate to the extension configuration panel
2. In the **Roles** field, enter the roles for customer support agents
3. Multiple roles can be entered as comma-separated values

**Example:**
```
L1 Support, L2 Support, L3 Support
```

### Step 2: Validate Configuration

1. Click **Validate Attributes** to verify the roles exist in Krista Studio
2. Ensure all specified roles are valid and active

### Step 3: Configure Security

1. In the **Security** panel, select the access point the extension runs on
2. Enable the **Trust extension** toggle to grant access permissions
3. Click **Save changes**

## Configuration Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roles_separated_by_comma` | Text | Yes | Comma-separated list of agent role names |

## Validation Rules

| Rule | Description |
|------|-------------|
| Roles must exist | All specified roles must be defined in Krista Studio |
| Non-empty roles | At least one role must be specified |
| Valid format | Roles must be comma-separated text values |

## Security Considerations

- **Workspace Admin Rights**: This extension requires workspace admin rights to operate
- **Role Verification**: Only users with specified roles can access agent functionality
- **Trust Settings**: Extension must be trusted before it can execute catalog requests

## Troubleshooting

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Validation fails | Role name doesn't exist | Verify role names in Krista Studio |
| Extension not trusted | Trust toggle disabled | Enable Trust extension in Security panel |
| Access denied | Missing permissions | Ensure workspace admin rights are granted |

## Related Pages

- [Overview](pages/Overview.md)
- [Join Task](pages/JoinTask.md)
- [Get All Queues](pages/GetAllQueues.md)

