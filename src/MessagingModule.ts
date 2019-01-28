import { NativeModules } from 'react-native';
const RNBatch = NativeModules.RNBatch;

export const MessagingModule = {
  /**
   * Toogles whether Batch should enter its "do not disturb" (DnD) mode or exit it.
   * While in DnD, Batch will not display landings, not matter if they've been triggered by notifications or an In-App Campaign, even in automatic mode.
   *
   * This mode is useful for times where you don't want Batch to interrupt your user, such as during a splashscreen, a video or an interstitial ad.
   *
   * If a message should have been displayed during DnD, Batch will enqueue it, overwriting any previously enqueued message.
   * When exiting DnD, Batch will not display the message automatically: you'll have to call the queue management methods to display the message, if you want to.
   *
   * Use batch.messaging.showPendingMessage() to show a pending message, if any.
   *
   * Android ✅ , iOS ⚠️
   * 
   * @param enabled Whether to enable, or disable "Do Not Disturb" mode
   */
  setDoNotDisturbEnabled: (enabled: boolean): void =>
    RNBatch.messaging_setNotDisturbed(enabled),

  /**
   * Shows the currently enqueued message, if any.
   * 
   * Android ✅ , iOS ⚠️
   */
  showPendingMessage: (): void => RNBatch.messaging_showPendingMessages(),
};
