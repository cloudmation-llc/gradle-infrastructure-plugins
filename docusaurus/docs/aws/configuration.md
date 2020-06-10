---
id: global-config
title: Global Configuration
sidebar_label: Global Configuration
---

The Gradle root project and its subprojects can be organized into a hierarchy, and the AWS plugins take full advantage of this to provide a layered configuration system.

Configuration starts at the root project as demonstrated in the [tutorial](cf-tutorial). You can create an `aws` block to set a default region and credential profile. AWS subprojects will inherit this configuration, and you can selectively override config properties per subproject, and even per task.

Unless otherwise specified, properties use the `name = value` syntax as shown in the examples below.

## The AWS Config Block

The `aws` block provides settings that apply to any service used by the plugins.

```groovy
aws {
    profile = "..." // Set a specific credential profile
    region = "..." // Set a specific region
}
```

**Reference:**

| Property | Default Value | Description
| ---- | ---- | ----
| `profile` | System default | Set the credentials profile to use for AWS API calls
| `region` | System default | Set the region to use for AWS API calls
| `tags` | None | Map of resource tags to be applied on resources which support them

### Authentication

Under the hood, the AWS Java SDK v2 is used to make service calls. If you do nothing, then the [default credential provider](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/DefaultCredentialsProvider.html) is used. This is typically the most ideal fit especially if you use the plugins in a CI/CD workflow.

Optionally, you can configure authentication using a named profile on your system. Named profiles for AWS are set up by default in `$HOME/.aws/config`.

Learn more about [named profiles](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-profiles.html) in the AWS documentation.

### Using MFA

MFA challenges are supported if the named profile you select has the `mfa_serial` and `role_arn` properties configured.

:::note
Only devices that generate a six-digit code that can be consumed by the AWS SDK are supported. Read more about [MFA setup](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_mfa.html) in the AWS documentation.
:::

The plugin will prompt you for the token code, and assume the role directly through the STS service. The session credentials are written to a temporary file on disk and used for subsequent task runs until they expire.

**Example of a named profile configured for MFA:**

```ini
[profile sample_named_profile]
duration_seconds = 7200
role_arn = arn:aws:iam::****:role/iam-role-to-assume
role_session_name = ****
mfa_serial = arn:aws:iam::****:mfa/your-mfa-device
source_profile = default
region = us-west-2
output = json
```

### Setting Resource Tags

The AWS block supports providing resource tags. Set the `tags` property to a Map, and provide as many key-value pairs as you want within the limits supported by the specific resource you are working with.

```groovy
aws {
    tags = [
        "tag-key": "tag-value",
        "another-key": "another-value"
    ]
}
```

### Tag Merging

Most properties are overriden at the deepest point where they are assigned. Tags are one of the exceptions where the final tags assigned to a resource are the result of merging together all of the tags found in the project hierarchy.

You might look at it this way: root tags + subproject(s) tags + task tags = tags applied to the resource.

Overwrites will only happen if a tag deeper in the hierarchy has the same key name as a tag found earlier. For example, if you define a tag named `department` in a subproject, but the task also defines a `department` tag, the value assigned to the task will win.

If no tags are found, then any existing custom tags are removed from the resources on the next deployment.