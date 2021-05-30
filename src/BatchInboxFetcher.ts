import { NativeModules } from 'react-native';
import { IInboxNotification } from './BatchInbox';
const RNBatch = NativeModules.RNBatch;

export class BatchInboxFetcher {
  private readonly identifier: string;

  constructor(identifier: string) {
    this.identifier = identifier;
  }

  /**
   * Destroys the fetcher.
   *
   * You'll usually want to use this when your component unmounts in order to free up memory.
   */
  destroy(): Promise<void> {
    return RNBatch.inbox_fetcher_destroy(this.identifier);
  }

  /**
   * Returns whether there is more notification to fetch.
   */
  hasMore(): Promise<boolean> {
    return RNBatch.inbox_fetcher_hasMore(this.identifier);
  }

  /**
   * Marks all notifications as read.
   */
  markAllNotificationsAsRead(): Promise<void> {
    return RNBatch.inbox_fetcher_markAllAsRead(this.identifier);
  }

  /**
   * Marks a notification as read.
   *
   * The notification must have been fetched by this fetcher before.
   *
   * @param notificationIdentifier The identifier of the notification to mark as read
   */
  markNotificationAsRead(notificationIdentifier: string): Promise<void> {
    return RNBatch.inbox_fetcher_markAsRead(
      this.identifier,
      notificationIdentifier
    );
  }

  /**
   * Marks a notification as deleted.
   *
   * The notification must have been fetched by this fetcher before.
   *
   * @param notificationIdentifier The identifier of the notification to mark as deleted
   */
  markNotificationAsDeleted(notificationIdentifier: string): Promise<void> {
    return RNBatch.inbox_fetcher_markAsDeleted(
      this.identifier,
      notificationIdentifier
    );
  }

  /**
   * Fetches new notifications (and resets pagination to 0).
   *
   * Usually used as an initial fetch and refresh method in an infinite list.
   */
  fetchNewNotifications(): Promise<{
    notifications: IInboxNotification[];
    endReached: boolean;
    foundNewNotifications: boolean;
  }> {
    return RNBatch.inbox_fetcher_fetchNewNotifications(this.identifier).then(
      result => ({
        ...result,
        notifications: parseNotifications(result.notifications),
      })
    );
  }

  /**
   * Fetches the next page of notifications.
   *
   * Usually used as a "fetchMore" method in an infinite list.
   */
  fetchNextPage(): Promise<{
    notifications: IInboxNotification[];
    endReached: boolean;
  }> {
    return RNBatch.inbox_fetcher_fetchNextPage(this.identifier).then(
      result => ({
        ...result,
        notifications: parseNotifications(result.notifications),
      })
    );
  }
}

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
