import { NativeModules } from 'react-native';
const RNBatch = NativeModules.RNBatch;

export const BatchMessaging = {
  /**
   * - Shows the currently enqueued message, if any.
   * - Used in conjonction with `Batch.start(true)` to display pending messages
   */
  showPendingMessage: (): void => RNBatch.messaging_showPendingMessage(),

  /**
   * Define if incoming messages have to be enqueued or displayed directly
   *
   * @param active
   */
  setNotDisturbed: (active: boolean): void =>
    RNBatch.messaging_setNotDisturbed(active),
};
