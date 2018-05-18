# react-native-batch-push

> React Native integration of Batch.com push notifications SDK

## Getting started

`$ npm install react-native-batch-push --save`

### Mostly automatic installation

```bash
react-native link react-native-batch-push
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
import BatchPush from 'react-native-batch-push';

// when you want to ask the user if he's willing to receive push notifications (required on iOS):
BatchPush.registerForRemoteNotifications();

// if you want to give a custom identifier to the user
BatchPush.loginUser('theUserId'); // add Platform.OS if you want to target a specific platform on your backend
BatchPush.logoutUser(); // when the user logs out
```

### Custom User Attribute

```js
import BatchPush from 'react-native-batch-push';

// if you want to set a user attribute, use setAttribute (takes two string arguments)
BatchPush.setAttribute('age', '23');
```

### Track User Location

```js
import BatchPush from 'react-native-batch-push';

// if you want to track the user's location
BatchPush.trackLocation({ latitude: 48, longitude: 2.3 });
```

### Inbox

```js
import BatchPush from 'react-native-batch-push';

BatchPush.fetchNewNotifications('theUserId', 'authKey')
  .then(notifications => {
    // notifications is Array<{ title: string, body: string, timestamp: number, payload: Object }>
  })
  .catch(e => console.warn('BatchPush error', e));
```
