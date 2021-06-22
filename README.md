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
- Add `pod 'Batch', '~>1.17'` to your _Podfile_
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

| react-native version | link the plugin                                                                     |
| -------------------- | ----------------------------------------------------------------------------------- |
| `>= 0.60.0`          | - auto-linking is supported                                                         |
| `< 0.60.0`           | - From the root folder <br/> - Run `react-native link @bam.tech/react-native-batch` |

### 4. Configure do not disturb mode for mobile landings/in app messages

Mobile landings/in app messages are modals that open in your app after being triggered by a push notification or an event.
By default, they appear as soon as they are triggered, meaning that such a message can appear when your app is not yet ready. (JS code not loaded, or your navigation is not yet mounted, or any other logic that you implemented)
This can raise problems, for example if one of the buttons in the in app message should redirect somewhere in your app, if the navigation is not yet ready, it won't work.
Batch has a "do not disturb" mode that can delay those messages until you indicate that your app is ready.
Do not disturb mode has no impact on push notifications, only on mobile landings/in app messages.

If you have a typical React Native app (eg. no brownfield), you should enable do not disturb mode.
In the future, we might enable do not disturb mode by default, but currently we don't, in order to have the same default behavior as full native apps.

You can enable do not disturb mode with the following steps:

* On Android, add in your resources:

```groovy
// android/app/build.gradle

defaultConfig {
    ...
    resValue "bool", "BATCH_DO_NOT_DISTURB_INITIAL_STATE", "true"
}
```

* On iOS, add in your `Info.plist`:

```xml
<key>BatchDoNotDisturbInitialState</key>
<true/>
```

* Show pending in app message or mobile landing

When your app is ready to be interacted with (for example, after showing the splashscreen or preparing your navigation), disable do not disturb and show the pending in app message or mobile landing:

```js
import { BatchMessaging } from '@bam.tech/react-native-batch';

await BatchMessaging.disableDoNotDisturbAndShowPendingMessage();
```

### 5. Extra steps on Android

#### a. Install Batch dependencies

```groovy
// android/build.gradle

buildscript {
    ...
    ext {
        batchSdkVersion = '1.17+'
    }
    dependencies {
        ...
        classpath 'com.google.gms:google-services:4.3.4'
    }
}
```

```groovy
// android/app/build.gradle

dependencies {
    implementation platform('com.google.firebase:firebase-bom:25.12.0') // needed if you don't have @react-native-firebase/app
    implementation "com.google.firebase:firebase-messaging" // needed if you don't have @react-native-firebase/messaging
    implementation "com.batch.android:batch-sdk:${rootProject.ext.batchSdkVersion}"
    ...
}

apply plugin: 'com.google.gms.google-services'
```

#### b. Configure auto-linking

Create or adapt the `react-native.config.js` file at the root of your project:

```js
// react-native.config.js

module.exports = {
  dependencies: {
    '@bam.tech/react-native-batch': {
      platforms: {
        android: {
          packageInstance: 'new RNBatchPackage(this.getApplication())',
        },
      },
    },
  },
};
```

#### c. Add your Batch key

```groovy
// android/app/build.gradle

defaultConfig {
    ...
    resValue "string", "BATCH_API_KEY", "%YOUR_BATCH_API_KEY%"
}
```

#### d. Add your Firebase config

- Add the _google-services.json_ file to `/android/app`

#### e. Configure `onNewIntent`

Add `Batch.onNewIntent(this, intent);` in your `MainActivity.java`:

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

### 6. Extra steps on iOS

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
    [RNBatch start];
    ...
    return YES;
}
```

#### d. Setup your UNUserNotificationCenterDelegate

* If you use Firebase or another framework swizzling your AppDelegate, follow the [manual integration](https://doc.batch.com/ios/advanced/manual-integration) guide
* Otherwise, add `[BatchUNUserNotificationCenterDelegate registerAsDelegate];` after the `[RNBatch start];` call in your `AppDelegate.m`

If you want to show foreground notifications, add the [relevant configuration](https://doc.batch.com/ios/advanced/customizing-notifications#showing-foreground-notifications-ios-10-only): `[BatchUNUserNotificationCenterDelegate sharedInstance].showForegroundNotifications = true;` after registering the delegate .

<hr>

## Usage

### Enable push notifications on iOS

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

### GDPR compliance flow

#### 1. Opt out by default

##### Android

Add the following metadata in your `AndroidManifest.xml` as described in the [Android SDK documentation](https://doc.batch.com/android/advanced/opt-out#disabling-the-sdk-by-default):

```xml
<meta-data android:name="batch_opted_out_by_default" android:value="true" />
```

##### iOS

Add `BATCH_OPTED_OUT_BY_DEFAULT=true` in your `Info.plist` as described in the [iOS SDK documentation](https://doc.batch.com/ios/advanced/opt-out#disabling-the-sdk-by-default):

```xml
<key>BATCH_OPTED_OUT_BY_DEFAULT</key>
<true/>
```

#### 2. Opt in when the user agrees

```js
import { Batch } from '@bam.tech/react-native-batch';

await Batch.optIn();
```

#### 3. Delete the user's data when the user wants to delete its account

As per the [SDK documentation](https://doc.batch.com/android/advanced/opt-out#opting-out) :
> This will wipe the data locally and request a remote data removal for the matching advertising ID/Custom User ID. Batch will blacklist the advertising and the Custom User ID for one month following the data removal. Batch will also discard the data sent from the Custom Data API for that specific Custom User ID. The Inbox feature will no longer work if you were relying on the Custom User ID.

```js
import { Batch } from '@bam.tech/react-native-batch';

await Batch.optOutAndWipeData();
```

### Handling push notification initial deeplink

On iOS, `Linking.getInitialURL` might return null even when the app was started as a result of a Batch push notification with a deeplink.
This is because the iOS Batch SDK opens the deep-link related to your push notification, after the app has already started.

In order to workaround this, you can use the following:

```ts
import { BatchPush } from '@bam.tech/react-native-batch';

BatchPush.getInitialURL().then((url: string | null) => {
  console.log('received initial url', url)
});
```

This is a replacement of `Linking.getInitialURL` that you can use on Android or iOS:
`Batch.getInitialURL` first checks if `Linking.getInitialURL` returns something, and then if it doesn't on iOS, it calls a custom native function of this module that gets the first deeplink it has ever seen.
Subsequent calls to this native function will return `null` to prevent a future JS reload to see an old initial URL.
Because of this, you have to make sure to call this method only once in the app lifetime.

Make sure to also listen for the Linking `url` event in case Batch opens your deep-link after you call getInitialURL.

<hr>

## Troubleshooting

- :warning: You will need a physical device to fully test **push notifications**
