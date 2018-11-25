#!/bin/bash

# Timeout prevents this job from lingering beyond development testing
timeout 60m mvn package exec:java
