#!/bin/bash

rm -rf /opt/griffon/*

gradlew clean installBinary
