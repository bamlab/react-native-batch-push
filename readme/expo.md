**Warning: The iOS part of this plugin may not be compatible with a native Firebase module cohabitation, or other third-party libraries that may swizzle your application delegate file (ios/AppDelegate.m). To understand more or if you want to extend this plugin to cover the "manual integration" of Batch Push, read [this documentation](https://doc.batch.com/ios/advanced/manual-integration)**

**Currently only supporting React Native >= 0.60.0. You should also use Expo SDK >= 42**

# Installation

## Common steps (Android & iOS)

1. Install using `yarn add @bam.tech/react-native-batch` or `npm i @bam.tech/react-native-batch`
2. In the app.json/app.config.js/app.config.ts file add the plugin:

```json
{
 "plugins": [
      [
        "@bam.tech/react-native-batch",
        {
          "androidApiKey": <YOUR_ANDROID_BATCH_API_KEY>,
          "iosApiKey": <YOUR_IOS_BATCH_API_KEY>
        }
      ]
    ]
}
```

## Additional Android steps

1. Copy your google-services.json file at the root of your project (get it from the Firebase Console) and link it in your app.json/app.config.js/app.config.ts under the key `googleServicesFile` of the android section.

2. Create a react-native.config.js file at the root of your project and/or add the following lines:

```js
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

## Additional iOS steps

Add the following in your app code to enable push notifications, ideally the first view a user sees when opening the app:

```js
import { BatchPush } from '@bam.tech/react-native-batch';

...

// Ask for the permission to display notifications
// The push token will automatically be fetched by the SDK
BatchPush.requestNotificationAuthorization();

// Alternatively, you can call requestNotificationAuthorization later
// But, you should always refresh your token on each application start
// This will make sure that even if your user's token changes, you still get notifications
// BatchPush.refreshToken();
```

# Build and run locally

1. Prepare your custom Expo client: `expo prebuild --clean`. This can be useful to debug and verify the plugin has executed correctly (compare with bare React Native configuration from the Batch doc)

2. Build your custom Expo client for Android: `expo run:android` for development; or for iOS: `expo run:ios`. To force starting on physical device instead of a simulator, add the `-d` option.

# Build with EAS

When you are ready to go to production or to provide a new develoment client (for internal testing) containing your newly added custom native code: [build your app with custom native code with EAS](https://docs.expo.dev/workflow/customizing/#releasing-apps-with-custom-native-code-to)

You will have to register every iOS device you plan on testing on with `eas device:create` (it has to be done before the build)
