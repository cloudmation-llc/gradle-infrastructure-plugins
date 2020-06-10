---
id: cf-config
title: CloudFormation Configuration
sidebar_label: Configuration
---

import useBaseUrl from '@docusaurus/useBaseUrl';

Building on the services provided by the [global configuration](../global-config), the CloudFormation plugin can be further customized by adding a `cloudformation` block to an `aws` block as demonstrated below.

```groovy
aws {
    cloudformation {
        capabilities = [""] // Set capabilities to be used for the stack deployment
        failOnEmptyChangeset = false
        roleArn = "" // IAM role that CloudFormation will assume for deployment
    }
}
```

## Task Properties

| Property | Default Value | Description
| ---- | ---- | ----
| `capabilities` | None | For certain cases, you will need to set IAM capabilities. See the [create stack API reference](https://docs.aws.amazon.com/AWSCloudFormation/latest/APIReference/API_CreateStack.html).
| `failOnEmptyChangeset` | `false` | If `true`, a changeset that is created for a stack that results in no resource changes will cause the build to fail. `false` ignores this situation, and allows the build to succeed.
| `roleArn` | None | Set an IAM role that CloudFormation will assume for the stack deployment. [See AWS documentation](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-iam-servicerole.html).

### Parameter Overrides

```groovy
aws {
    cloudformation {
        /* Call parameterOverride(...) as many times as needed */
        parameterOverride "param1", "value1"
        parameterOverride "param2", "value2"

        /* Another option is to provide a Map of parameters */
        parameterOverrides [
            "param1": "value1",
            "param2": "value2"
        ]
    }
}
```

**Note:** Parameter overrides are treatly similiarly as resource tags, and all parameter overrides found through the project config will be merged together.

## Customize Stack Naming

By default, a stack deployment task uses the filename of its template, and the name of its project to derive the stack name. For example, a template named `vpc.yml` in a subproject named `network` will have the stack name `network-vpc`.

There are ways to override this behavior:

**Override Stack Prefix**

Instead of using the project name to prefix the stack name, you can specify a custom prefix that all of the deploy tasks in the project will use.

```groovy
aws {
    cloudformation {
        stackPrefix = "super-awesome-network"
    }
}
```

Using the example above, if you have templates named `vpc.yml`, and `vpc-zone-a.yml`, then the respective stack names at deployment will be `super-awesome-network-vpc` and `super-awesome-network-vpc-zone-a`.

You can also set an empty string for stack prefix which effectively disables this behavior.

**Override Stack Name Per Task**

The configuration for automatically generated tasks can be changed even after they are created. For example, if you have a subproject named `network`, then there will be a `network.gradle` in the project directory. Open that file, and add an `afterEvaluate` block. You can use the task reconfiguration DSL to change the properties for any generated task.

```groovy
afterEvaluate {
    deployVpc.configure {
        stackName = "super-awesome-vpc"
    }
}
```

## Custom Stack Definition

The idea of using convention-over-configuration can be very convenient, but the default behavior may also generate tasks that you do not want, and you would like finer control over how a template is going to be deployed. Custom stacks skip the conventions, and let you define the specific stacks to be deployed.

Create a `stack` block for custom stack within the `cloudformation` block. Below is a more complex example that sets a custom task group name, and uses a single template with parameterization to generate deployment tasks.

Note that name in quotes which defines each custom stack is used as part of the task name generation. See the _Task Naming_ section above for more detail on naming.

```groovy
aws {
    taskGeneration {
        group = "AWS Network"
    }

    cloudformation {
        stack "network-zone-a", {
            stackName = "vpc-zone-a"
            templateFile = file("vpc-zone-network.yml")

            /*
             * Methods such as parameterOverride(...) for the 
             * CloudFormation config DSL can be called on custom 
             * stacks
             */
            parameterOverride "PrivateSubnetRange", "10.255.1.0/24" 
            parameterOverride "PublicSubnetRange", "10.255.251.0/24"
            parameterOverride "RegionAzIndex", "0"
            parameterOverride "ExportSuffix", "ZoneA"
        }

        stack "network-zone-b", {
            stackName = "vpc-zone-b"
            templateFile = file("vpc-zone-network.yml")
            parameterOverride "PrivateSubnetRange", "10.255.2.0/24"
            parameterOverride "PublicSubnetRange", "10.255.252.0/24"
            parameterOverride "RegionAzIndex", "1"
            parameterOverride "ExportSuffix", "ZoneB"
        }

        stack "network-zone-c", {
            stackName = "vpc-zone-c"
            templateFile = file("vpc-zone-network.yml")
            parameterOverride "PrivateSubnetRange", "10.255.3.0/24"
            parameterOverride "PublicSubnetRange", "10.255.253.0/24"
            parameterOverride "RegionAzIndex", "2"
            parameterOverride "ExportSuffix", "ZoneC"
        }

        stack "network-zone-d", {
            stackName = "vpc-zone-d"
            templateFile = file("vpc-zone-network.yml")
            parameterOverride "PrivateSubnetRange", "10.255.4.0/24"
            parameterOverride "PublicSubnetRange", "10.255.254.0/24"
            parameterOverride "RegionAzIndex", "3"
            parameterOverride "ExportSuffix", "ZoneD"
        }
    }
}
```

Running `gradle tasks` will display the tasks that are generated as a result.

<img
    alt="Screenshot of Custom Stacks"
    src={useBaseUrl('/img/screenshots/aws-cf-config-custom-tasks.png')} />