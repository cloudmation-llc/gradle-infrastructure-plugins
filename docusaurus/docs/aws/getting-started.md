---
id: aws-getting-started
title: Getting Started
sidebar_label: Getting Started
---

The AWS family of plugins focuses on using [CloudFormation](https://aws.amazon.com/cloudformation) to manage your cloud environment. CloudFormation is an excellent service offering from AWS that provides ingredients and a domain specific language (using either YAML or JSON) to design much of your cloud environment in written form as templates, and then deploy those templates as "stacks" of live resources.

## Features

* Automatic generation of deployment tasks for each CloudFormation template.
* Incorporates [cfn-lint](https://github.com/aws-cloudformation/cfn-python-lint) to ensure high template quality and best practices before deployment.
* Declarative configuration that starts with the root project, and can be selectively overidden at the subproject, or even _per task_.
  * It is simple and straight forward to organize a complex environment across multiple regions, and with different IAM role requirements. This is like a powerful hybrid of both [nested stacks](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-nested-stacks.html) and [stack sets](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/what-is-cfnstacksets.html).
* Enhanced CLI experience including live, colored output of stack events during deployment.

## Requirements

- Java 8 JDK or greater _(this project is regularly tested with the [AWS Coretto](https://aws.amazon.com/corretto/) JVM)_
- [cfn-lint](https://github.com/aws-cloudformation/cfn-python-lint) installed for template linting prior to deployment