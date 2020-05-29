#!/usr/bin/env bash

# Clone the template repo to the target directory
git clone git@github.com:cloudmation-llc/cloudmation-gradle-aws-starter.git $1

# Change into the target directory
cd $1

# Drop the .git directory to disassociate from template repo
rm -Rf .git

# Create a new empty Git repo as a replacement
git init

# Make an initial commit to start the new history
git add --all
git commit -m 'Created new repository'