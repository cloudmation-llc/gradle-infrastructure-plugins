---
id: cf-tutorial-basics
title: Tutorial (The Basics)
sidebar_label: Tutorial (The Basics)
---

import useBaseUrl from '@docusaurus/useBaseUrl';

So, you followed the steps in [Getting Started](cf-getting-started), and an empty project is ready to go. Now what?

The CloudFormation plugin works by looking for specific directories, iterating through the files within, and generating runnable tasks according to what it finds. To demonstrate how this works, we will create a small VPC composed of resources to enable three availability zones with public networking in the `us-west-2` region.

As you proceed through the steps, at times you will see a reference to the *project root*. This refers to the directory that contains your entire project, or for absence of doubt where `settings.gradle` lives.

When the plugin is applied to the root project, it looks for `src/cloudformation`. If it does not exist, then the plugin will create it for you. This will serve as the root folder for all templates. You can have as many subdirectories as you want to organize your templates. Each subdirectory found is configured as a Gradle subproject.

### Step 1 - Configure Global Defaults

In the project root, open `build.gradle`. You will just see 3 lines which import the project plugin.

If you want to set a specific region and credentials profile, add a `aws` config block.

If you do not set your own values, the region and credentials are resolved by the AWS SDK. The series of checks performed by the SDK is best described in the [javadocs](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/awscore/client/builder/AwsClientBuilder.html#region-software.amazon.awssdk.regions.Region-).

If you do use a [named profile](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/ProfileCredentialsProvider.html), then the SDK works with your local `$HOME/.aws/config` and `$HOME/.aws/credentials` files.

```groovy
aws {
    profile = "named_profile_goes_here"
    region = "us-west-2"
}
```

### Step 2 - Create Network Directory

Within the `cloudformation` directory, create another directory named `network`.

### Step 3 - Create vpc.yml Template

Within the `network` directory, created a template file named `vpc.yml`.

:::info Tip
YAML is highly recommended over JSON for designing CloudFormation templates. [The wins are too numerous to list here](https://aws.amazon.com/blogs/mt/the-virtues-of-yaml-cloudformation-and-using-cloudformation-designer-to-convert-json-to-yaml).
:::

Template samples are provided below for the benefit of the tutorial, but certainly feel free to substitute your own templates. Check the `examples/simple` directory for the actual template files used.

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
      DomainName: !Sub "${AWS::Region}.compute.internal"
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

### Step 4 - First Gradle Review

:::note "Gradle" vs "Gradlew"
If you are using the Gradle wrapper such as via the starter template, then all Gradle commands can be run by using the `./gradlew` or `.\gradlew.bat` scripts.
:::

Once `vpc.yml` is created and has some content, this is a good point to check out what the Gradle plugins will do. From the project root run `gradle projects`.

<img
    alt="Screenshot of Gradle projects"
    src={useBaseUrl('/img/screenshots/aws-tutorial-p1-gradle-projects.png')}
    style={{ width: '80%' }} />

Note now that `vpc.yml` is detected, its containing directory `network` becomes a subproject.

Now, let's look at the tasks. Run `gradle tasks`.

<img
    alt="Screenshot of Gradle tasks"
    src={useBaseUrl('/img/screenshots/aws-tutorial-p1-gradle-tasks.png')}
    style={{ width: '80%' }} />

The plugin automatically creates a `lintVpc` task using the filename to run the `cfn-lint` tool to check for template errors and overall correctness. A `deployVpc` task is also created which will do the actual work of creating the CloudFormation stack and changeset. The deploy task has the linting task set as a dependency so that every deploy will first lint the template to check for errors.

### Step 5 - Create the VPC

Run the `deployVpc` task, and watch the VPC stack with its resources get created. After the build completes successfully, you will be able to browse around the AWS console and see the everything that was created.

### Step 6 - Add First Availability Zone

For simplicity, we will create the network resources for each availability zone in separate template files. There other more advanced methods to take advantage of template reuse and parameters discussed later in the documentation.

Create a file named `vpc-zone-a.yml` in the same directory as `vpc.yml`, and copy and paste the content below into it.

```yaml Sample vpc-zone-a.yml
Description: VPC availability zone A

Parameters:
  PublicRange:
    Type: String
    AllowedPattern: "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}\\/\\d+"
    Default: 10.255.251.0/24

  RegionAzIndex:
    Type: Number
    Default: 0

Resources:

  #
  # Public subnet
  #
  SubnetPublic:
    Type: AWS::EC2::Subnet
    Properties:
      AvailabilityZone: !Select 
        - !Ref RegionAzIndex
        - Fn::GetAZs: !Ref "AWS::Region"
      CidrBlock: !Ref PublicRange
      Tags:
        - Key: Type
          Value: public
      VpcId: !ImportValue VpcId
      
  #
  # Route table for public subnet
  #
  RouteTablePublic:
    Type: AWS::EC2::RouteTable
    Properties:
      VpcId: !ImportValue VpcId

  RouteTablePublicSubnetAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref SubnetPublic
      RouteTableId: !Ref RouteTablePublic

  RoutePublicRouteInternetGateway:
    Type: AWS::EC2::Route
    Properties:
      RouteTableId: !Ref RouteTablePublic
      DestinationCidrBlock: 0.0.0.0/0
      GatewayId: !ImportValue VpcInternetGateway
```

Run `gradle tasks` again, and you will see new tasks created to lint and deploy the zone A template.

Now run `gradle deployVpcZoneA` to create the resources.

### Step 7 - Create Additional Availability Zones

Copy `vpc-zone-a.yml` to new files `vpc-zone-b.yml` and `vpc-zone-c.yml`. Open each of the new zone files, and adjust the default values for the parameters accordingly so that the IP ranges are unique, and the correct availability zone identifier is set.

**Suggested Parameter Values:**

| File | PublicRange | RegionAzIndex
| ---- | ---- | ---- |
| vpc-zone-a.yml | `10.255.251.0/24` | `0` |
| vpc-zone-b.yml | `10.255.252.0/24` | `1` |
| vpc-zone-c.yml | `10.255.253.0/24` | `2` |

Once the templates have been adjusted, you can run multiple tasks in series and deploy resources for several stacks in one shot. Deploy zone B and zone C together by running `gradle deployVpcZoneB deployVpcZoneC`.

### Conclusion

Tutorial complete! At this, you now have an overview of how the Gradle plugins can help you set up an organized AWS project. Without any deep customization, the plugin applied simple conventions to your templates to create standard operaring procedures for making the deployment, and providing sensible defaults along the way such as how stacks get named.

Throughout the rest of this documentation, we will lift the hood on more advanced configuration so that you can tweak the conventions to suit your specific needs.