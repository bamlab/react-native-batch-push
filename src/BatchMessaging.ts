import { NativeModules, Platform } from 'react-native';
import { BatchEventEmitter, EmitterSubscription } from './BatchEventEmitter';
const RNBatch = NativeModules.RNBatch;

export interface BatchMessagingEventPayload {
  isPositiveAction: boolean;
  trackingId?: string | null;
  webViewAnalyticsIdentifier?: string | null;
  deeplink?: string | null;
}

export const BatchMessaging = {
  /**
   * Shows the currently enqueued message, if any.
   */
  showPendingMessage: (): Promise<void> =>
    RNBatch.messaging_showPendingMessage(),

  /**
   * Define if incoming messages have to be enqueued or displayed directly
   *
   * @param active
   */
  setNotDisturbed: (active: boolean): Promise<void> =>
    RNBatch.messaging_setNotDisturbed(active),

  /**
   * Disables do not disturb mode and shows the currently enqueued message, if any.
   */
  disableDoNotDisturbAndShowPendingMessage: (): Promise<void> =>
    RNBatch.messaging_disableDoNotDisturbAndShowPendingMessage(),

  /**
   * Override the font used in message views. Not applicable for standard alerts.
   * [iOS](https://doc.batch.com/ios-api-reference/Classes/BatchMessaging.html#/c:objc(cs)BatchMessaging(cm)setFontOverride:boldFont:italicFont:boldItalicFont:)
   * [Android](https://doc.batch.com/android-api-reference/com/batch/android/Batch.Messaging.html#setTypefaceOverride-Typeface-Typeface-)
   *
   * @param normalFontName
   * @param boldFontName
   * @param italicFontName (iOS only)
   * @param italicBoldFontName (iOS only)
   */
  setFontOverride: (
    normalFontName?: string | null,
    boldFontName?: string | null,
    italicFontName?: string | null,
    italicBoldFontName?: string | null
  ): Promise<void> => {
    if (Platform.OS === 'android') {
      return RNBatch.messaging_setTypefaceOverride(
        normalFontName,
        boldFontName
      );
    }

    return RNBatch.messaging_setFontOverride(
      normalFontName,
      boldFontName,
      italicFontName,
      italicBoldFontName
    );
  },

  /**
   * Listen for messaging events
   */
  addListener(
    eventType:
      | 'show'
      | 'close'
      | 'close_error'
      | 'auto_close'
      | 'click'
      | 'webview_click',
    callback: (payload: BatchMessagingEventPayload) => void
  ): EmitterSubscription {
    const subscription = BatchEventEmitter.addListener(
      `messaging_${eventType}`,
      callback
    );
    return {
      remove: () => subscription.remove(),
    };
  },
};
