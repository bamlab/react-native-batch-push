# react-native-batch-push
> React Native integration of Batch.com push notifications SDK

## Getting started

`$ npm install react-native-batch-push --save`

### Mostly automatic installation

`$ react-native link react-native-batch-push`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-batch-push` and add `RNBatchPush.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNBatchPush.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import tech.bam.RNBatchPush.RNBatchPushPackage;` to the imports at the top of the file
  - Add `new RNBatchPushPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-batch-push'
  	project(':react-native-batch-push').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-batch-push/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-batch-push')
  	```


## Usage
```javascript
import RNBatchPush from 'react-native-batch-push';

// TODO: What to do with the module?
RNBatchPush;
```
  