# Install plugin for development

## 1. Setup a new Project

- Create a new app using `react-native init BatchTest`
- Go into the folder `cd BatchTest`
- Initialize the git repo and commit everything

## 2. Install the React Native Batch plugin

- Create a `mkdir local-modules`
- Go into the folder `cd local-modules`
- Create a `mkdir @bam.tech`
- Go into the folder `cd @bam.tech`
- Add `local-modules` to your _.gitignore_
- Clone the react-native-batch-push repository `git clone git@github.com:bamlab/react-native-batch-push.git`
- Rename `react-native-batch-push` to `react-native-batch`
- Checkout the required branch or a new branch
- Run `yarn` to install dependencies

## 3. Run build for development

- Open `local-modules/@bam.tech/react-native-batch` within VSCode
- Run the `Task run build task >> tsc: watch`

## 4. Install the plugin on Android

### a. Link the library

```groovy
// android/settings.gradle

include ':@bam.tech_react-native-batch'
project(':@bam.tech_react-native-batch').projectDir = new File(rootProject.projectDir, '../local-modules/@bam.tech/react-native-batch/android')
```

```groovy
// android/app/build.gradle

dependencies {
    implementation project(':@bam.tech_react-native-batch')
    ...
}
```

```java
// android/app/src/main/java/com/<AppName>/MainApplication.java

import tech.bam.RNBatchPush.RNBatchPackage;

public class MainApplication extends Application implements ReactApplication {
  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new RNBatchPackage()
      );
    }
  }
}
```

### b. Install Batch dependencies

```groovy
// android/build.gradle

buildscript {
    ...
    dependencies {
        ...
        classpath 'com.google.gms:google-services:4.3.4'
    }
}
```

```groovy
// android/app/build.gradle

dependencies {
    implementation platform('com.google.firebase:firebase-bom:25.12.0')
    implementation "com.google.firebase:firebase-messaging"
    ...
}

apply plugin: 'com.google.gms.google-services'
```

### c. Add your Batch key

```groovy
// android/app/build.gradle

defaultConfig {
    ...
    resValue "string", "BATCH_API_KEY", "%YOUR_BATCH_API_KEY%"
}
```

### d. Add your Firebase config

- Add the _google-services.json_ file to `/android/app`

### d. Run the App

- Open the `/android` folder with _Android studio_
- Connect your phone to your computer
- Run `adb reverse tcp:8081 tcp:8081`
- Run `yarn start`
- Run the project in debug mode

## 5. Install the plugin on iOS

### a. Setup the Batch dependencies

- Go to `/ios`
- Run `pod init`
- Format your _Podfile_ following this template

```
# ios/Podfile
# Uncomment the next line to define a global platform for your project
platform :ios, '10.0'

target '<YourAppName>' do
  # See http://facebook.github.io/react-native/docs/integration-with-existing-apps.html#configuring-cocoapods-dependencies
  pod 'React', :path => '../node_modules/react-native', :subspecs => [
    'Core',
    'CxxBridge', # Include this for RN >= 0.47
    'DevSupport', # Include this to enable In-App Devmenu if RN >= 0.43
    'RCTText',
    'RCTNetwork',
    'RCTWebSocket', # Needed for debugging
    'RCTAnimation', # Needed for FlatList and animations running on native UI thread
    # Add any other subspecs you want to use in your project
  ]

  # Explicitly include Yoga if you are using RN >= 0.42.0
  pod 'yoga', :path => '../node_modules/react-native/ReactCommon/yoga'
  pod 'Folly', :podspec => '../node_modules/react-native/third-party-podspecs/Folly.podspec'

  # Third party deps podspec link
  pod 'Batch', '~>1.13'
  pod 'RNBatchPush', path: '../local-modules/@bam.tech/react-native-batch'
end

post_install do |installer|
  installer.pods_project.targets.each do |target|
    targets_to_ignore = %w(React yoga)

    if targets_to_ignore.include? target.name
      target.remove_from_project
    end
  end
end
```

- Run `pod install`

### b. Enable Push Capabilities

- In the project window
- Go to _Capabilities_
- Toggle _Push Notifications_

### c. Configure your Batch key

Go to the Batch dashboard, create an iOS app and upload your iOS push certificate.

Then, in `Info.plist`, provide:

```xml
<key>BatchAPIKey</key>
<string>%YOUR_BATCH_API_KEY%</string>
```

### d. Run the App

- Run the project **on a real device** from XCode

## 6. Use the plugin in your app

```typescript
// App.js

import { Batch, BatchPush } from '@bam.tech/react-native-batch';

Batch.start();
// On iOS, to display the push authorization modal
BatchPush.registerForRemoteNotifications();
```

## 7. Extra

### a. Using the type definition with a JS project

```json
// jsconfig.json

{
  "compilerOptions": {
    "baseUrl": "./",
    "paths": {
      "@bam.tech/react-native-batch": [
        "./local-modules/@bam.tech/react-native-batch"
      ]
    }
  },
  "exclude": ["node_modules"]
}
```

### b. Check that the setup was succesful

- Add a Custom User Identifier

```typescript
import { BatchUser } from '@bam.tech/react-native-batch';

BatchUser.editor().setIdentifier('<custom-id>').save();
```

- Go to your project on Batch.com
- Go to the debug view
- Select _CUSTOM USER ID_ and write in your _<custom_id>_
- Press debug to retrieve your information
