import { NativeModules } from 'react-native';
import { BatchInboxFetcher } from './BatchInboxFetcher';
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

export interface BatchInboxFetcherOptions {
  fetchLimit?: number;
  maxPageSize?: number;
  user?: { identifier: string; authenticationKey: string };
}

/**
 * Batch's inbox module
 */
export const BatchInbox = {
  NotificationSource,

  /**
   * Gets a Batch inbox fetcher.
   */
  async getFetcher(
    options: BatchInboxFetcherOptions = {}
  ): Promise<BatchInboxFetcher> {
    const fetcherIdentifier = await RNBatch.inbox_getFetcher(options);
    return new BatchInboxFetcher(fetcherIdentifier);
  },
};
