# react-native-batch-push

> React Native integration of Batch.com push notifications SDK

## Getting started

```
$ npm install @bam.tech/react-native-batch --save

# OR

$ yarn add @bam.tech/react-native-batch
```

### Mostly automatic installation

```bash
react-native link @bam.tech/react-native-batch
```

#### iOS specific

If you don't have a Podfile or are unsure on how to proceed, see the [CocoaPods](http://guides.cocoapods.org/using/using-cocoapods.html) usage guide.

In your `Podfile`, add:

```
pod 'Batch', '~> 1.10'
```

Then:

```bash
cd ios
pod repo update # optional and can be very long
pod install
```

### Configuration

#### Android

Go to the Batch dashboard, create an Android app and setup your FCM configuration.
Make sure to have added Firebase Messaging as stated in the [Batch documentation](https://batch.com/doc/android/sdk-integration.html#_adding-push-notifications-support).
Then, in `android/app/build.gradle`, provide in your config:

```
defaultConfig {
    ...
    resValue "string", "BATCH_API_KEY", "%YOUR_BATCH_API_KEY%"
}
```

Note that you can also customize the keys depending on your product flavor or build type.

#### Small push notification icon

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

##### Mobile landings and in-app messaging

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

#### iOS

Go to the Batch dashboard, create an iOS app and upload your iOS push certificate.
Then, in `Info.plist`, provide:

```xml
<key>BatchAPIKey</key>
<string>%YOUR_BATCH_API_KEY%</string>
```

## Usage

### Enabling push notifications

```js
import Batch from '@bam.tech/react-native-batch';

// when you want to ask the user if he's willing to receive push notifications (required on iOS):
Batch.registerForRemoteNotifications();
```
