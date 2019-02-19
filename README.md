# React Native Batch Push

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

- Go to `/ios`
- If you don't have a Podfile yet run `pod init`
- Add `pod 'Batch', '~>1.13'` to your _Podfile_
- Run `pod install`

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

It is recommended to provide a small notification icon in your `MainActivity.java`:

```java
// push_icon.png in your res/drawable-{dpi} folder
import com.batch.android.Batch;
import android.os.Bundle;
import android.graphics.Color;

...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Batch.Push.setSmallIconResourceId(R.drawable.push_icon);
        Batch.Push.setNotificationsColor(Color.parseColor(getResources().getString(R.color.pushIconBackground)));
    }
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
