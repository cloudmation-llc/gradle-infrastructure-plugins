---
id: cf-cookbook
title: Cookbook
sidebar_label: Cookbook
---

import useBaseUrl from '@docusaurus/useBaseUrl';

The following is a selection of interesting things that you can do using the CloudFormation plugin to organize and deploy your stacks.

## Parameterized Deployments

[**Example in Github**](https://github.com/cloudmation-llc/gradle-infrastructure-plugins/tree/master/examples/src/cloudformation/cookbook-params-and-dependencies)

This has been touched on in the [configuration](cf-config) page, but I wanted to reiterate the value gained from parameterizing templates. Using parameters effectively opens an opportunity to use _one template_ for multiple stack deployments. Doing so cuts down on the number of template files that need to be maintained.

The default design of the plugin is to read your template files, and come up with a sensible assumption about how to deploy them. In the situation of wanting to use single template, and deploy it multiple times with varying parameter values, the plugin cannot guess what needs to be done, and instead you need to provide the config.

Take a look at the [parameterization example](https://github.com/cloudmation-llc/gradle-infrastructure-plugins/tree/master/examples/src/cloudformation/cookbook-params-and-dependencies). In the build config, you can exclude tasks that were created automatically by the plugin, and instead define the specific stacks that you want deployed and their parameters. Note how the same template file is used repeatedly.

## Using Dependencies for Complex Deployments

[**Example in Github**](https://github.com/cloudmation-llc/gradle-infrastructure-plugins/tree/master/examples/src/cloudformation/cookbook-params-and-dependencies)

Gradle provides really nice tools to organize the task graph, and arrange the order of how tasks should be deployed. Since the plugin creates a task for each stack deployment, you can take advantage of task ordering to define complex deployments.

Use an `afterEvaluate` block to configure task dependencies. In the [provided example](https://github.com/cloudmation-llc/gradle-infrastructure-plugins/tree/master/examples/src/cloudformation/cookbook-params-and-dependencies), note how the subnet stacks are not deployed until the VPC stack is first created successfully.

Use Gradle properties such as `dependsOn`, `mustRunBefore`, and `mustRunAfter` to chain together tasks.

```groovy
afterEvaluate {
    deployNetworkZoneA.dependsOn deployVpc
    deployNetworkZoneB.dependsOn deployVpc
    deployNetworkZoneC.dependsOn deployVpc
    deployNetworkZoneD.dependsOn deployVpc
}
```

## Task Groups

[**Example in Github**](https://github.com/cloudmation-llc/gradle-infrastructure-plugins/tree/master/examples/src/cloudformation/cookbook-params-and-dependencies)

Setting task dependencies is nice, but what if you want to run them all in one shot? You can create custom tasks in Gradle that simply call other tasks according to the define dependencies.

```groovy
afterEvaluate {
    // Define a task to deploy all of the others
    task "deployNetwork" {
        group = "AWS Network"
        description = "Deploy all of the network resources"
        dependsOn deployNetworkZoneA, deployNetworkZoneB, deployNetworkZoneC, deployNetworkZoneD
    }
}
```