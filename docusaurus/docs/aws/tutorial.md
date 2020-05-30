---
id: aws-tutorial
title: Tutorial
sidebar_label: Tutorial
---

import useBaseUrl from '@docusaurus/useBaseUrl';

So, you followed the steps in [Getting Started](aws-getting-started), and an empty project is ready to go. Now what?

The AWS plugin works by looking for specific directories, iterating through the files within, and generating runnable tasks according to what it finds. To demonstrate how this works, we will create a small VPC composed of resources to enable three availability zones with both public and private networking in the `us-west-2` region.

As you proceed through the steps, at times you will see a reference to the *project root*. This refers to the directory that contains your entire project, or for absence of doubt where `settings.gradle` lives.

### Step 1 - Create Coudformation Directory

Create a `cloudformation` directory. This will serve as the root folder for all templates. You can have as many subdirectories as you want to organize your templates. Each subdirectory where at least one template is found is made into a Gradle subproject.

### Step 2 - Configure Global Defaults

In the project root, open `build.gradle`. You will just see 3 lines which import the project plugin.

After the plugins block, add an `aws` config block, and here is where you should set a default region and, _optionally_, a credentials profile. Consider these sensible defaults to get started. You can selectively override these settings per project and even per task.

Credentials are resolved by the AWS SDK. If you do not specify a named profile, then a [series of checks is performed by the SDK](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/DefaultCredentialsProvider.html) to find credentials.

If you do use a [named profile](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/ProfileCredentialsProvider.html), then the SDK works with your local `$HOME/.aws/config` and `$HOME/.aws/credentials` files.

```groovy
aws {
    profile = "named_profile_goes_here"
    region = "us-west-2"
}
```

### Step 3 - Create Network Directory

Within the `cloudformation` directory, create another directory named `network`.

### Step 4 - Create vpc.yml Template

Within the `network` directory, created a template file named `vpc.yml`.

:::info Tip
YAML is highly recommended over JSON for designing CloudFormation templates. The wins are too numerous to list here.
:::

To help this tutorial flow, you can copy and paste the template content from the samples provided.

```yaml title="Sample vpc.yml"
Description: VPC master configuration

Resources:

  #
  # The VPC
  #
  Vpc:
    Type: AWS::EC2::VPC
    Properties:
      CidrBlock: 10.255.0.0/16
      EnableDnsSupport: true
      EnableDnsHostnames: true

  #
  # DHCP options
  #
  DhcpOptions:
    Type: AWS::EC2::DHCPOptions
    Properties:
      DomainName: us-west-2.compute.internal
      DomainNameServers:
        - AmazonProvidedDNS

  DhcpOptionsAssociation:
    Type: AWS::EC2::VPCDHCPOptionsAssociation
    Properties: 
      DhcpOptionsId: !Ref DhcpOptions
      VpcId: !Ref Vpc

  #
  # Assignable elastic IP for network egress
  #
  EipRoot:
    DependsOn: Vpc
    Type: AWS::EC2::EIP
    Properties:
      Domain: vpc

  #
  # Internet Gateway
  #
  InternetGateway:
    Type: AWS::EC2::InternetGateway

  InternetGatewayAttachment:
    Type: AWS::EC2::VPCGatewayAttachment
    Properties:
      VpcId: !Ref Vpc
      InternetGatewayId: !Ref InternetGateway

#
# Stack outputs
#
Outputs:
  VpcId:
    Description: The VPC ID
    Value: !Ref Vpc
    Export:
      Name: VpcId

  EipRootAllocationId:
    Description: Allocation ID for the root elastic IP
    Value: !GetAtt EipRoot.AllocationId
    Export:
      Name: VpcEipRootAllocationId

  InternetGatewayId:
    Description: Internet gateway ID for public subnets
    Value: !Ref InternetGateway
    Export:
      Name: VpcInternetGateway
```

### Step 5 - First Gradle Review

:::note "Gradle" vs "Gradlew"
If you are using the Gradle wrapper such as via the starter template, then all Gradle commands can be run by using the `./gradlew` or `.\gradlew.bat` scripts.
:::

Once `vpc.yml` is created and has some content, this is a good point to check out what the Gradle plugins will do. From the project root run `gradle projects`.

<img
    alt="Screenshot of Gradle projects"
    src={useBaseUrl('/img/screenshots/aws-tutorial-p1-gradle-projects.png')}
    style={{ width: '80%' }} />

Note now that `vpc.yml` is detected, its containing directory `network` becomes a subproject. As a convenience, the plugin also generated an empty Gradle build file with the same name. You should see `network.gradle` with the template.

Now, let's look at the tasks. Run `gradle tasks`.

<img
    alt="Screenshot of Gradle tasks"
    src={useBaseUrl('/img/screenshots/aws-tutorial-p1-gradle-tasks.png')}
    style={{ width: '80%' }} />

The plugin automatically creates a `lintVpc` task using the filename to run the `cfn-lint` tool to check for template errors and overall correctness. A `deployVpc` task is also created which will do the actual work of creating the CloudFormation stack and changeset. The deploy task has the linting task set as a dependency so that every deploy will first lint the template to check for errors.

### Step 6 - Add Availability Zone Templates

TODO.