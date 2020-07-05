---
id: global-config
title: Global Configuration
sidebar_label: Global Configuration
---

The Gradle root project and its subprojects can be organized into a hierarchy, and the AWS plugins take full advantage of this to provide a layered configuration system.

Configuration starts at the root project as demonstrated in the [tutorial](cf-tutorial). You can create an `aws` block to set a default region and credential profile. AWS subprojects will inherit this configuration, and you can selectively override config properties per subproject, and even per task.

Unless otherwise specified, properties use the `name = value` syntax as shown in the examples below.

## Gradle Files

Projects are configured using Gradle files. The plugin accepts the default convention that each project directory has a `build.gradle` files.

For subprojects, you can also define a named Gradle file. For example, if the project subdirectory is named `network`, then you can create a build file named `network.gradle`, and the plugin will use the named file for configuration instead of the default.

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
| `profile` | System default | Set the named profile for configuring the AWS client. The profile plays an important part in configuring credentials, too.
| `region` | System default | Set a specific region to use. This will override the named profile, and other lookups built into the AWS SDK.
| `tags` | None | Map of resource tags to be applied on resources which support them

### Authentication

Under the hood, the AWS Java SDK v2 is used to make service calls. If you do nothing, then the [instance metadata provider](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/InstanceProfileCredentialsProvider.html) is configured, followed by the [default credential provider](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/DefaultCredentialsProvider.html) is used. This is typically the most ideal fit especially if you use the plugins in a CI/CD workflow.

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

The AWS block supports providing resource tags. Call the `tag` method for each key-value pair, and provide as many pairs as you want within the limits supported by the specific resource you are working with.

```groovy
aws {
    tag "tag-key", "tag-value"
    tag "another-key", "another-value"
}
```

### Dynamic Resource Tags

Tag values can also be set using closures. This means you run arbitrary Groovy code to determine the value for a tag. You can call other Java/Groovy APIs, or even launch external programs. Example:

```groovy
aws {
    tag "last-updated-by", {
        // Run an external executable
        def process = "whoami".execute()
        process.waitFor()
        return process.text.trim()
    }   
}
```

### Tag Merging

Most properties are overridden at the deepest point where they are assigned. Tags are one of the exceptions where the final tags assigned to a resource are the result of merging together all of the tags found in the project hierarchy.

You might look at it this way: root tags + subproject(s) tags + task tags = tags applied to the resource.

Overwrites will only happen if a tag deeper in the hierarchy has the same key name as a tag found earlier. For example, if you define a tag named `department` in a subproject, but the task also defines a `department` tag, the value assigned to the task will win.

If no tags are found, then any existing custom tags are removed from the resources on the next deployment.

## Customize Task Generation

If you want to customize the tasks that are automatically generated by the plugin, add a `taskGeneration` config block to the `aws` block. **This is only supported at the subproject level.**

### Task Naming

The plugin follows the Gradle convention of using camel casing for task names. The characters `-` `.` `_` and ` ` (space) are declared as delimiters which are fed into the camel case routine. Dashes (`-`) are very readable and encouraged for naming files and custom stacks.

### Task Prefix

Optionally, you can tweak the task names that are generated by adding a prefix.

Take for example if you had a template which would by default generate a task name of `deployNetwork`. Using the prefix configuration below, that task name will now be generated as `deployEgressNetwork`.

```groovy
aws {
    taskGeneration {
        taskPrefix = "Egress"
    }
}
```

### Task Grouping

Gradle tasks support a `group` property to logically group tasks together under categories. Using grouping provides for a neater appearance in tools that display lists of tasks such as when you run `gradle tasks` on the command line. This is recommended.

By default, the group will be set to `aws`, but you can change this to anything. For example you might consider grouping IAM tasks and network tasks separately.

```groovy
aws {
    taskGeneration {
        group = "Some other group"    
    }
}
```

### Including and Excluding Tasks

There could be use cases when you want to alter which tasks are generated by the plugin. For example, you choose to use the custom stacks feature _(see below)_ with a specific template file, and do not want tasks generated automatically for that template. You can create a rule that uses either a [regular expression match](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html) on the task name, or you can provide a closure to use your own logic.

Include rules are evaluated first, and then exclude rules are evaluated after. You can call `include` or `exclude` as many times as you want. Rules are evaluated in creation order. The final task name is used for the evaluation which means any adjustments discussed earlier such as a custom task prefix are taken into consideration.

#### Exclude by Pattern

```groovy
aws {
    taskGeneration {
        exclude ".*VpcSubnet.*"   
    }
}
```

#### Exclude by Closure

```groovy
aws {
    taskGeneration {
        exclude { taskName -> taskName == "someTaskYouWantToAvoid" }
    }
}
```

#### Include by Pattern

```groovy
aws {
    taskGeneration {
        include "lint.*"   
    }
}
```

#### Include by Closure

```groovy
aws {
    taskGeneration {
        include { taskName -> taskName == "someTaskYouWantToEnsureIsCreated" }
    }
}
```