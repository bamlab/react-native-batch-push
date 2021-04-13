#!/bin/sh

NEW_VERSION="$1"

sed -i '' "s/\"version\": \"[0-9].[0-9].[0-9]\"/\"version\": \"$NEW_VERSION\"/" package.json
sed -i '' "s/ReactNative\/[0-9].[0-9].[0-9]/\ReactNative\/$NEW_VERSION/" ./ios/RNBatch.h
sed -i '' "s/ReactNative\/[0-9].[0-9].[0-9]/\ReactNative\/$NEW_VERSION/" ./android/src/main/java/tech/bam/RNBatchPush/RNBatchModule.java
