#!/bin/bash

# Deploy no Maven Central
mvn clean javadoc:jar source:jar  deploy
