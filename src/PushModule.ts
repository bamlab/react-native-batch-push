import { NativeModules } from 'react-native';
const RNBatch = NativeModules.RNBatch;

export interface IAndroidNotificationTypes {
  NONE: number;
  SOUND: number;
  VIBRATE: number;
  LIGHTS: number;
  ALERT: number;
}

export const AndroidNotificationTypes: IAndroidNotificationTypes =
  RNBatch.NOTIFICATION_TYPES;

/**
 * Batch's push module
 */
export const PushModule = {
  AndroidNotificationTypes,

  /**
   * Ask iOS users if they want to accept push notifications. Required to be able to push users.
   *
   * Android *️⃣ , iOS ✅
   *
   * No effect on Android.
   */
  registerForRemoteNotifications: (): void =>
    RNBatch.push_registerForRemoteNotifications(),

  /**
   * Change the used remote notification types on Android. (Ex: sound, vibrate, alert)
   *
   * Android ✅ , iOS ⚠️
   *
   * Example : setAndroidNotificationTypes(batch.push.AndroidNotificationTypes.ALERT | batch.push.AndroidNotificationTypes.SOUND)
   */
  setAndroidNotificationTypes: (notificationTypes: number[]): void => {
    const notificationType = notificationTypes.reduce(
      (sum, value) => sum + value,
      0
    );
    RNBatch.push_setNotificationTypes(notificationType);
  },

  /**
   * Clear the app badge on iOS. No effect on Android.
   *
   * Android *️⃣ , iOS ✅
   *
   */
  clearBadge: (): void => RNBatch.push_clearBadge(),

  /**
   * Dismiss the app's shown notifications on iOS. Should be called on startup.
   *
   * Android *️⃣ , iOS ✅
   *
   * No effect on Android.
   */
  dismissNotifications: (): void => RNBatch.push_dismissNotifications(),

  /**
   * Gets the last known push token.
   * Batch MUST be started in order to use this method.
   *
   * Android ✅ , iOS ✅
   *
   * The returned token might be outdated and invalid if this method is called
   * too early in your application lifecycle.
   *
   * On iOS, your application should still register for remote notifications
   * once per launch, in order to keep this value valid.
   */
  getLastKnownPushToken: (): Promise<string> =>
    RNBatch.push_getLastKnownPushToken(),
};
