---
id: cf-getting-started
title: Getting Started
sidebar_label: Getting Started
---

There are a number of plugins for Gradle that provide various levels of integration with AWS. This family of plugins focuses on using [CloudFormation](https://aws.amazon.com/cloudformation) as the **principal** tool to manage your AWS environment. The objective is not merely to lift AWS SDK calls to the surface as Gradle tasks, but to actually model an _opinionated_ set of conventions that help you effectively organize and manage AWS resources.

CloudFormation is an excellent service offering from AWS that provides ingredients and a domain specific language (using either YAML or JSON) to design most of your AWS environment in written form as templates. The templates in turn are used to create and update "stacks" of live resources.

## Features

* Automatic generation of deployment tasks for each CloudFormation template.
* Incorporates [cfn-lint](https://github.com/aws-cloudformation/cfn-python-lint) to ensure high template quality and best practices before deployment.
* Declarative configuration that starts with the root project, and can be selectively overidden at the subproject, or even _per task_.
  * It is simple and straight forward to organize a complex environment across multiple regions, and with different IAM role requirements. You might think of this as a more powerful take on [stack sets](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/what-is-cfnstacksets.html).
* Enhanced CLI experience including live, colored output of stack events during deployment.
* Uses [AWS SDK for Java V2](https://github.com/aws/aws-sdk-java-v2) under the hood

## Requirements

- At least Java 8 JDK or greater _(this project is regularly tested with the [AWS Coretto](https://aws.amazon.com/corretto/) JVM)_
- [cfn-lint](https://github.com/aws-cloudformation/cfn-python-lint) installed for template linting prior to deployment

## Installation

Regardless of which option you choose below, the steps below will cover getting a blank project created and set up. The [tutorial](cf-tutorial) walks through a basic AWS configuration in greater detail including working with templates, and the auto-generated tasks.

### Starter Template

One option to get going quickly to clone the AWS starter template into a new project. The template project includes a preconfigured Gradle wrapper so that you do not need to install Gradle yourself.

A helper script is provided that will clone the template repo, and then replace the Git configuration with a new empty repo ready for your work.

Copy and paste the line below to download and run the `create-aws-project.sh` script straight from GitHub. Replace `YOUR_DIRECTORY` with a destination name of your choosing.

```bash
curl -L "https://raw.githubusercontent.com/cloudmation-llc/gradle-aws-starter/master/create-project.sh" | bash -s YOUR_DIRECTORY
```

### Manual Installation

Alternatively, you can set up a new project yourself by going through a few manual steps. You will need a recent version of Gradle already installed on your workstation. Verify by first running `gradle --version`.

1. Create a new working directory for your project.
   
2. In your directory, create three empty files:

* `gradle.properties`
* `settings.gradle`
* `build.gradle`

3. It is recommended to externalize the plugin version as a Gradle property. Copy and paste the following into `gradle.properties`.

Check the Gradle plugins portal for the latest version string: https://plugins.gradle.org/plugin/com.cloudmation.aws

```properties
cloudmationInfraPluginsVersion = VERSION_HERE
```

1. Add the AWS project settings plugin to `settings.gradle`.

```groovy
plugins {
    id "com.cloudmation.aws-settings-cloudformation" version "$cloudmationInfraPluginsVersion"
}
```

5. Add the AWS project config and CloudFormation plugins to `build.gradle`.

```groovy
plugins {
    id "com.cloudmation.aws"
    id "com.cloudmation.aws-cloudformation"
}
```

6. Test that the configuration works by running `gradle tasks`. Gradle should list the available tasks and no errors or failures.

7. _Optionally_, create a Gradle wrapper by running `gradle wrapper`. This is highly recommended especially if you plan to develop a CI/CD pipeline for pushing infrastructure changes, or when multiple individuals will be working from this repo.

8. If this is your first time using the plugins, check out the [tutorial](cf-tutorial) next.