---
id: about
title: About
sidebar_label: About
---

## What is this?

A set of plugins for [Gradle](https://gradle.org/) that provide an opinionated set of conventions for setting up and managing infrastructure-as-code projects.

Deploying and managing cloud infrastructure using configuration-as-code in this author's view is vital for long term success. When you consider that cloud providers provide official tooling for their services, add into the mix community projects for linting, static analysis, image building, host automation, and more . . . the amount tools that you end up using manage your cloud environment can be significant.

Those who develop software and libraries have long understood this problem, and the community has built excellent tools to solve it. This is where Gradle comes in. Gradle is not just well suited for software development projects, it can also be great for "developing" your infrastructure, too.

## Why Infrastructure as Code?

For many organizations and projects, excellent infrastructure is key. Some businesses would cease to exist without a well run and maintained cloud environment.

_Infrastructure-as-code_ brings some of the time honored practices of software development such change control and rapid continuous integration into the delivery of compute services. It allows you describe your operating environment is in a way that is documented, teachible, reproducible, and auditable.

In contrast, were you to interact with your cloud provider exclusively through a web GUI, an enormous amount of operational knowledge is lost in the mouse clicks. Sure, you could reconstruct some of it through an effective audit strategy that captures console and API actions. However, whatever you may cobble together will never be as complete as having the environment completely down in writing, with comments, and a change history. Morever, having a written copy of your environment is the first step to using automation effectively for long-term maintenance.

### Gradle Crash Course

Paraphrasing from the page [What is Gradle](https://docs.gradle.org/current/userguide/what_is_gradle.html#what_is_gradle), projects are made easy to build by implementing conventions. You can show up and not need to think deeply about how your infrastructure templates/code/pipelines/plans/playbooks should be organized. Gradle can automate that for you. The beauty of Gradle is that while there are recommended conventions that work well out of the box, it is very easy to override and customize to suit specific needs.

#### The "Root" Project

Any directory with a `build.gradle` file becomes the root of a project. Subprojects can be defined in subdirectories with additional `.gradle` files, thus forming a hierarchy.

#### Subprojects

Gradle subprojects can have their own configuration independent of other projects, but can also derive and override configuration from the root project, too.

If we were to visualize this using a simple nested list, it might look like the following:

- Root
  - Project A
    - Project AA
    - Project AB
    - Project AC
  - Project B
    - Project BA
  - Project C

Effective use of subprojects are one of the highlights when you use this plugin family for your cloud projects.

#### Tasks

Gradle projects define tasks that go and perform actual work. Tasks can be created at the root for the entire project including children subprojects, or a subproject may define its own tasks for specific uses.