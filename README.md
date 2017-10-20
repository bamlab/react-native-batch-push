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

Go to the Batch dashboard, create an Android app and setup your GCM configuration.
Then, in `android/app/build.gradle`, provide in your config:

```
defaultConfig {
    ...
    resValue "string", "GCM_SENDER_ID", "%YOUR_GCM_SENDER_ID%"
    resValue "string", "BATCH_API_KEY", "%YOUR_BATCH_API_KEY%"
    resValue "string", "BATCH_INBOX_SECRET", "%YOUR_BATCH_INBOX_SECRET%" // if you want to use Batch's optional Inbox feature
}
```

Note that you can also customize the keys depending on your product flavor or build type.

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

### Inbox
Only on Android at the moment. See the [Batch's docs](https://batch.com/doc/ios/inbox.html) if you want to do a PR!

```js
import BatchPush from 'react-native-batch-push';

Batch.fetchNewNotifications('theUserId')
    .then(notifications => {
      // notifications is { title: string, body: string, timestamp: number, payload: Object }
    })
    .catch(e => console.warn('BatchPush error', e));
```
