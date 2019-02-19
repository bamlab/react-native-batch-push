import { NativeModules } from 'react-native';
const RNBatch = NativeModules.RNBatch;

export const BatchMessaging = {
  /**
   * - Shows the currently enqueued message, if any.
   * - Used in conjonction with `Batch.start(true)` to display pending messages
   *
   * Android ✅ , iOS ✅
   */
  showPendingMessage: (): void => RNBatch.messaging_showPendingMessage(),
};
