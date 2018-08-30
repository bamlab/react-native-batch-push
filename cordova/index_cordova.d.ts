declare namespace BatchSDK {
  /**
   * Batch Configuration object
   */
  interface Config {
    /**
     * Your Android API Key
     */
    androidAPIKey?: string | null;

    /**
     * Your iOS API Key
     */
    iOSAPIKey?: string | null;

    /**
     * Sets whether the SDK can use the advertising identifier or not (default: true)
     *
     * The advertising identifier is also called "IDFA" on iOS.
     */
    canUseAdvertisingIdentifier?: boolean;
  }

  type BatchEventCallback = (eventName: string, parameters: any) => void;

  /**
   * Represents a locations, using lat/lng coordinates
   */
  interface Location {
    /**
     * Latitude
     */
    latitude: number;

    /**
     * Longitude
     */
    longitude: number;

    /**
     * Date of the tracked location
     */
    date?: Date;

    /**
     * Precision radius in meters
     */
    precision?: number;
  }

  /**
   * Batch Cordova Module
   * @version 1.11.0
   * @exports batch
   */
  interface Batch {
    /**
     * Registers a listener for a given event. Multiple listeners can be set on an event.
     *
     * @param event The event name to listen to.
     * @param listener Function with two arguments : event name and parameters, called when an event occurs
     */
    on(event: string, listener: BatchEventCallback): void;

    /**
     * Unregisters all listeners for a given event, or all events.
     *
     * @param event The event name you wish to remove the listener for. If nothing is passed, all events are removed.
     */
    off(event?: string): void;

    /**
     * Set Batch's config. You're required to call this before start.
     *
     * If you don't want to specify one of the configuration options, simply omit the key.
     *
     * @param config The config to set
     */
    setConfig(config: Config): void;

    /**
     * Push module
     */
    push: PushModule;

    /**
     * User module
     */
    user: UserModule;

    /**
     * Messaging module
     */
    messaging: MessagingModule;

    /**
     * Inbox module
     */
    inbox: InboxModule;
  }

  /**
   * Batch's user module
   */
  interface UserModule {
    /**
     * Get the user data editor. Don't forget to call save when you're done.
     * @return Batch user data editor
     */
    getEditor(): BatchUserDataEditor;

    /**
     * Print the currently known attributes and tags for a user to the logs.
     */
    printDebugInformation(): void;

    /**
     * Track an event. Batch must be started at some point, or events won't be sent to the server.
     * @param name The event name. Must be a string.
     * @param label The event label (optional). Must be a string.
     * @param data The event data (optional). Must be an object.
     */
    trackEvent(
      name: string,
      label?: string,
      data?: { [key: string]: any }
    ): void;

    /**
     * Track a transaction. Batch must be started at some point, or events won't be sent to the server.
     * @param amount Transaction's amount.
     * @param data The transaction data (optional). Must be an object.
     */
    trackTransaction(amount: number, data?: { [key: string]: any }): void;

    /**
     * Track a geolocation update
     * You can call this method from any thread. Batch must be started at some point, or location updates won't be sent to the server.
     * @param location User location object
     */
    trackLocation(location: Location): void;
  }

  /**
   * Batch's push module
   */
  interface PushModule {
    iOSNotificationTypes: typeof iOSNotificationTypes;

    /**
     * Ask iOS users if they want to accept push notifications. Required to be able to push users.
     * No effect on Android.
     */
    registerForRemoteNotifications(): void;

    /**
     * Change the used remote notification types on iOS. (Ex: sound, vibrate, alert)
     * Example : setiOSNotificationTypes(batch.push.iOSNotificationTypes.ALERT | batch.push.iOSNotificationTypes.SOUND)
     * @param notifTypes Any combined value of the AndroidNotificationTypes enum.
     */
    setiOSNotificationTypes(
      notifTypes: PushModule['iOSNotificationTypes']
    ): void;
  }

  /**
   * Batch's messaging module
   */
  interface MessagingModule {}

  /**
   * Batch's inbox module
   */
  interface InboxModule {}

  /**
   * User data editor
   */
  interface BatchUserDataEditor {
    /**
     * Set the application language. Overrides Batch's automatically detected language.
     * Send null to let Batch autodetect it again.
     * @param language Language code. 2 chars minimum, or null
     */
    setLanguage(language: string | null): BatchUserDataEditor;

    /**
     * Set the application region. Overrides Batch's automatically detected region.
     * Send "null" to let Batch autodetect it again.
     * @param region Region code. 2 chars minimum, or null
     */
    setRegion(region: string | null): BatchUserDataEditor;

    /**
     * Set a custom user identifier to Batch, you should use this method if you have your own login system.
     * Be careful: Do not use it if you don't know what you are doing, giving a bad custom user ID can result
     * in failure of targeted push notifications delivery or offer delivery and restore.
     * @param identifier Your custom identifier.
     */
    setIdentifier(identifier: string | null): BatchUserDataEditor;

    /**
     * Set an attribute for a key
     * @param key Attribute key. Cannot be null, empty or undefined. It should be made of letters, numbers or underscores ([a-z0-9_]) and can't be longer than 30 characters.
     * @param value Attribute value. Accepted types are numbers, booleans, Date objects and strings. Strings must not be empty or longer than 64 characters.
     */
    setAttribute(
      key: string,
      value: string | number | boolean | Date
    ): BatchUserDataEditor;

    /**
     * Remove an attribute
     * @param key The key of the attribute to remove
     */
    removeAttribute(key: string): BatchUserDataEditor;

    /**
     * Remove all attributes
     */
    clearAttributes(): BatchUserDataEditor;

    /**
     * Add a tag to a collection. If the collection doesn't exist it will be created.
     * @param collection The tag collection name. Cannot be null or undefined. Must be a string of letters, numbers or underscores ([a-z0-9_]) and can't be longer than 30 characters.
     * @param tag The tag to add. Cannot be null, undefined or empty. Must be a string no longer than 64 characters.
     */
    addTag(collection: string, tag: string): BatchUserDataEditor;

    /**
     * Remove a tag
     * @param collection The tag collection name. Cannot be null or undefined. Must be a string of letters, numbers or underscores ([a-z0-9_]) and can't be longer than 30 characters.
     * @param tag The tag name. Cannot be null, empty or undefined. If the tag doesn't exist, this method will do nothing.
     */
    removeTag(collection: string, tag: string): BatchUserDataEditor;

    /**
     * Removes all tags
     */
    clearTags(): BatchUserDataEditor;

    /**
     * Removes all tags from a collection
     * @param collection The tag collection name. Cannot be null or undefined. Must be a string of letters, numbers or underscores ([a-z0-9_]) and can't be longer than 30 characters.
     */
    clearTagCollection(collection: string): BatchUserDataEditor;

    /**
     * Save all of the pending changes made in that editor. This action cannot be undone.
     */
    save(): BatchUserDataEditor;
  }

  /**
   * Notification model from the Inbox module
   */
  interface InboxNotification {}

  /**
   * Android Notification Types enum.
   * This enum's implementation is available on batch.push.AndroidNotificationTypes.
   */
  enum AndroidNotificationTypes {}

  /**
   * iOS Notification Types enum.
   * This enum's implementation is available on batch.push.iOSNotificationTypes.
   */
  enum iOSNotificationTypes {
    NONE = 0,
    BADGE = 1 << 0,
    SOUND = 1 << 1,
    ALERT = 1 << 2,
  }

  /**
   * Inbox Notification Source enum.
   * A notification source represents how the push was sent from Batch: via the Transactional API, or using a Push Campaign
   *
   * To be used with batch.inbox fetched notifications. This enum's implementation is available on batch.inbox.NotificationSource.
   */
  enum InboxNotificationSource {
    UNKNOWN = 0,
    CAMPAIGN = 1,
    TRANSACTIONAL = 2,
  }
}

// Cordova extensions
interface Window {
  batch: BatchSDK.Batch;
}

interface CordovaPlugins {
  batch: BatchSDK.Batch;
}

declare var batch: BatchSDK.Batch;
