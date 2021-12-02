**Warning: This plugin is available only for Android for the moment. iOS version in progress...**

# Installation

1. Install using `yarn add @bam.tech/react-native-batch` or `npm i @bam.tech/react-native-batch`
2. Copy your google-services.json file at the root of your project (get it from the Firebase Console)
3. In the app.json file add the plugin:

```
{
 "plugins": [
      [
        "react-native-batch",
        {
          "apiKey": <YOUR_BATCH_API_KEY>
        }
      ]
    ]
}
```

4. Create a react-native.config.js file at the root of your project and/or add the following lines:

```
module.exports = {
  dependencies: {
      "@bam.tech/react-native-batch": {
        platforms: {
          android: {
            packageInstance: "new RNBatchPackage(this.getApplication())",
          },
        },
      },
  },
};

```

5. Prepare your custom Expo client: `expo prebuild --clean`
6. Build your custom Expo client for Android: `expo run:android` for development

When you are ready to go to production : [build your app with custom native code with EAS](https://docs.expo.dev/workflow/customizing/#releasing-apps-with-custom-native-code-to)
