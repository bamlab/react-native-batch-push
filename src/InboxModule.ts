import { NativeModules } from 'react-native';
const RNBatch = NativeModules.RNBatch;

export enum NotificationSource {
  UNKNOWN = 0,
  CAMPAIGN = 1,
  TRANSACTIONAL = 2,
}

export interface IInboxNotification {
  /**
   * Unique notification identifier. Do not make assumptions about its format: it can change at any time.
   */
  identifier: string;

  /**
   * Notification title (if present)
   */
  title?: string;

  /**
   * Notification alert body
   */
  body: string;

  /**
   * URL of the rich notification attachment (image/audio/video) - iOS Only
   */
  iOSAttachmentURL?: string;

  /**
   * Raw notification user data (also called payload)
   */
  payload: any;

  /**
   * Date at which the push notification has been sent to the device
   */
  date: Date;

  /**
   * Flag indicating whether this notification is unread or not
   */
  isUnread: boolean;

  /**
   * The push notification's source, indicating what made Batch send it. It can come from a push campaign via the API or the dashboard, or from the transactional API, for example.
   */
  source: NotificationSource;
}

/**
 * Batch's inbox module
 */
export const InboxModule = {
  NotificationSource,

  /**
   * Fetch notifications for the current installation.
   * Only the 100 latest notifications will be fetched.
   */
  fetchNotifications: (): Promise<IInboxNotification[]> =>
    RNBatch.inbox_fetchNotifications().then(parseNotifications),

  /**
   * Fetch notifications for the specified user identifier.
   * Only the 100 latest notifications will be fetched.
   *
   * @param identifier User identifier for which you want the notifications
   * @param authKey Secret authentication key: it should be computed your backend and given to this method. Information on how to compute it can be found in our online documentation.
   */
  fetchNotificationsForUserIdentifier: (
    userIdentifier: string,
    authenticationKey: string
  ): Promise<IInboxNotification[]> =>
    RNBatch.inbox_fetchNotificationsForUserIdentifier(
      userIdentifier,
      authenticationKey
    ).then(parseNotifications),
};

const parseNotifications = (
  notifications: IInboxNotification[]
): IInboxNotification[] => {
  return notifications.map(notification => {
    if (!notification.payload) return notification;

    const batchPayload = notification.payload['com.batch'];

    // Try parsing the raw batch payload
    try {
      return {
        ...notification,
        payload: {
          ...notification.payload,
          'com.batch': JSON.parse(batchPayload),
        },
      };
    } catch (error) {
      return notification;
    }
  });
};
