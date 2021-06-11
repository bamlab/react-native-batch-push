import { NativeModules, Platform } from 'react-native';
export * from './BatchEventData';
export * from './BatchInbox';
export * from './BatchInboxFetcher';
export * from './BatchMessaging';
export * from './BatchPush';
export * from './BatchUser';

const RNBatch = NativeModules.RNBatch;

/**
 * Batch React-Native Module
 */
export const Batch = {
  /**
   * Start Batch.
   */
  start: (): void => {
    if (Platform.OS === 'ios') {
      // start should be made on native side in didFinishLaunchingWithOptions
      return;
    }

    RNBatch.start();
  },

  /**
   * Opt In to Batch SDK Usage.
   *
   * This method will be taken into account on next full application start (full process restart)
   *
   * Only useful if you called batch.optOut() or batch.optOutAndWipeData() or opted out by default in the manifest
   *
   * Some features might not be disabled until the next app start if you call this late into the application's life. It is strongly
   * advised to restart the application (or at least the current activity) after opting in.
   */
  optIn: (): void => RNBatch.optIn(),

  /**
   * Opt Out from Batch SDK Usage
   *
   * Note that calling the SDK when opted out is discouraged: Some modules might behave unexpectedly
   * when the SDK is opted-out from.
   *
   * Opting out will:
   * - Prevent batch.start()
   * - Disable any network capability from the SDK
   * - Disable all In-App campaigns
   * - Make the Inbox module return an error immediatly when used
   * - Make the SDK reject any editor calls
   * - Make the SDK reject calls to batch.user.trackEvent(), batch.user.trackTransaction(), batch.user.trackLocation() and any related methods
   *
   * Even if you opt in afterwards, data that has been generated while opted out WILL be lost.
   *
   * If you're also looking at deleting user data, please use batch.optOutAndWipeData()
   *
   * Note that calling this method will stop Batch.
   * Your app should be prepared to handle these cases.
   * Some features might not be disabled until the next app start.
   */
  optOut: (): void => RNBatch.optOut(),

  /**
   * Opt Out from Batch SDK Usage
   *
   * Same as batch.optOut(Context) but also wipes data.
   *
   * Note that calling this method will stop Batch.
   * Your app should be prepared to handle these cases.
   */
  optOutAndWipeData: (): void => RNBatch.optOutAndWipeData(),

  /**
   * Shows debug view
   *
   * Android: https://doc.batch.com/android/troubleshooting#implementing-batch-debugger
   * iOS: https://doc.batch.com/ios/troubleshooting#implementing-batch-debugger
   */
  showDebugView: (): void => {
    switch (Platform.OS) {
      case 'android':
        RNBatch.debug_startDebugActivity();
        break;
      default:
        RNBatch.presentDebugViewController();
        break;
    }
  },
};
