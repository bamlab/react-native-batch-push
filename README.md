# React Native Batch

The official React Native plugin for the Batch SDK. Made with ❤️ by BAM and Batch.

<hr>

## [Link to full documentation](https://bamlab.github.io/react-native-batch-push)

## [Development setup](readme/development.md)

<hr>

## Installation

### 1. Install the React Native Batch plugin

- Install using `yarn add @bam.tech/react-native-batch`
- Or `npm i @bam.tech/react-native-batch`

### 2. Setup iOS dependencies

#### Cocoapods (Recommended)

- Go to `/ios`
- If you don't have a Podfile yet run `pod init`
- Add `pod 'Batch', '~>1.13'` to your _Podfile_
- Run `pod install`

#### Manual frameworks (Not Recommended)

If you don't use CocoaPods, you can integrate Batch SDK manually.

- Download the [SDK](https://batch.com/doc/ios/advanced/general.html#_manual-sdk-integration)
- Unzip the SDK
- Here instead of following the readme inside the Batch.embeddedframework folder you downloaded follow the below steps.
- Create a Batch folder in `{your-project}/ios/Batch`
- Copy your Batch framework inside `{your-project}/ios/Batch`
- Open your project in XCode. Right click your ".xcodeproj" and click "Add Files to {yourProjectName}…". Find the "Batch" folder you created and select it. Before clicking "Add", to the left you'll see an "Options" button. Click it, and make sure "Create Groups" and "Add to targets" for your project are both selected.

### 3. Link the plugin

- From the root folder
- Run `react-native link @bam.tech/react-native-batch`

### 4. Extra steps on Android

#### a. Install Batch dependencies

```groovy
// android/build.gradle

buildscript {
    ...
    dependencies {
        ...
        classpath 'com.google.gms:google-services:4.2.0'
    }
}
```

```groovy
// android/app/build.gradle

dependencies {
    implementation "com.google.firebase:firebase-core:16.0.7"
    implementation "com.google.firebase:firebase-messaging:17.3.4"
    ...
}

apply plugin: 'com.google.gms.google-services'
```

#### b. Add your Batch key

```groovy
// android/app/build.gradle

defaultConfig {
    ...
    resValue "string", "BATCH_API_KEY", "%YOUR_BATCH_API_KEY%"
}
```

#### c. Add your Firebase config

- Add the _google-services.json_ file to `/android/app`

### 5. Extra steps on iOS

#### a. Enable Push Capabilities

- In the project window
- Go to _Capabilities_
- Toggle _Push Notifications_

#### b. Configure your Batch key

Go to the Batch dashboard, create an iOS app and upload your iOS push certificate.

Then, in `Info.plist`, provide:

```xml
<key>BatchAPIKey</key>
<string>%YOUR_BATCH_API_KEY%</string>
```

#### c. Start Batch in AppDelegate.m

In `AppDelegate.m`, start Batch:

```objective-c
#import "RNBatch.h"

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    ...
    [RNBatch start:false]; // or true if you want the do not disturb mode
    ...
    return YES;
}
```

<hr>

## Usage

### Start Batch

```js
import { Batch } from '@bam.tech/react-native-batch';

Batch.start();
```

### Enabling push notifications on iOS

```js
import { BatchPush } from '@bam.tech/react-native-batch';

BatchPush.registerForRemoteNotifications();
```

<hr>

## Other

### Small push notification icon

For better results on Android 5.0 and higher, it is recommended to add a Small Icon and Notification Color.
An icon can be generated using Android Studio's asset generator: as it will be tinted and masked by the system, only the alpha channel matters and will define the shape displayed. It should be of 24x24dp size.
If your notifications shows up in the system statusbar in a white shape, this is what you need to configure.

This can be configured in the manifest as metadata in the application tag:

```xml
<!-- Assuming there is a push_icon.png in your res/drawable-{dpi} folder -->

<manifest ...>
    <application ...>
        <meta-data
            android:name="com.batch.android.push.smallicon"
            android:resource="@drawable/push_icon" />

        <!-- Notification color. ARGB but the alpha value can only be FF -->
        <meta-data
            android:name="com.batch.android.push.color"
            android:value="#FF00FF00" />
```

### Mobile landings and in-app messaging

If you set a custom `launchMode` in your `AndroidManifest.xml`, add in your `MainActivity.java`:

```java
// import android.content.Intent;
// import com.batch.android.Batch;

@Override
public void onNewIntent(Intent intent)
{
    Batch.onNewIntent(this, intent);

    super.onNewIntent(intent);
}
```
