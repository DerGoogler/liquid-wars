#!/bin/sh
ant clean &&
ndk-build &&
ant debug
