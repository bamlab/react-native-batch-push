import { NativeModules, Platform } from 'react-native';
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
  ): void => {
    if (Platform.OS === 'android') {
      RNBatch.messaging_setTypefaceOverride(normalFontName, boldFontName);
    } else {
      RNBatch.messaging_setFontOverride(
        normalFontName,
        boldFontName,
        italicFontName,
        italicBoldFontName
      );
    }
  },
};
