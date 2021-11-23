import {
  DeviceEventEmitter,
  NativeEventEmitter,
  NativeModules,
  Platform,
} from 'react-native';

export const BatchEventEmitter = Platform.select({
  ios: new NativeEventEmitter(NativeModules.RNBatch),
  android: DeviceEventEmitter, // don't use NativeEventEmitter on android to support RN < 0.65
});

export interface EmitterSubscription {
  remove: () => void;
}
