package tech.bam.RNBatchPush;

import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.Log;

import com.batch.android.Batch;
import com.batch.android.PushNotificationType;
import com.batch.android.BatchInboxFetcher;
import com.batch.android.BatchInboxNotificationContent;
import com.batch.android.BatchMessage;
import com.batch.android.BatchUserDataEditor;
import com.batch.android.Config;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RNBatchModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String NAME = "RNBatch";
    public static final String ACTION_FOREGROUND_PUSH = "com.batch.android.cordova.foreground_push_received";
    public static final String ACTION_DISPLAY_LANDING_BANNER = "com.batch.android.cordova.display_landing_banner";

    /**
     * Key used to add extra to an intent to prevent it to be used more than once to compute opens
     */
    private static final String INTENT_EXTRA_CONSUMED_PUSH = "BatchCordovaPushConsumed";

    /**
     * Variable that keeps track of whether the JS called "batch.start()" already.
     * Used for automatic restarting of Batch
     */
    private static Boolean BATCH_STARTED = false;

    private final ReactApplicationContext reactContext;

    // REACT NATIVE PLUGIN SETUP

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("ACTION_FOREGROUND_PUSH", ACTION_FOREGROUND_PUSH);
        constants.put("ACTION_DISPLAY_LANDING_BANNER", ACTION_DISPLAY_LANDING_BANNER);

        // Add push notification types
        final Map<String, Object> notificationTypes = new HashMap<>();
        for (PushNotificationType type: PushNotificationType.values()) {
            notificationTypes.put(type.name(), type.ordinal());
        }
        constants.put("NOTIFICATION_TYPES", notificationTypes);

        return constants;
    }

    public RNBatchModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.reactContext.addLifecycleEventListener(this);

        try {
            Resources resources = reactContext.getResources();
            String packageName = reactContext.getApplicationContext().getPackageName();
            String batchAPIKey = resources.getString(resources.getIdentifier("BATCH_API_KEY", "string", packageName));

            Batch.Push.setGCMSenderId(resources.getString(resources.getIdentifier("GCM_SENDER_ID", "string", packageName)));
            Batch.setConfig(new Config(batchAPIKey));

            start();
        } catch (Exception e) {
            Log.e("RNBatchPush", e.getMessage());
        }
    }

    // BASE MODULE

    @ReactMethod // OK
    public void start() {
        Activity activity = getCurrentActivity();
        if (activity == null)
            return;

        if (BATCH_STARTED == true) {
            return;
        }

        Batch.onStart(activity);
        BATCH_STARTED = true;
    }

    @ReactMethod // OK
    public void optIn() {
        Batch.optIn(reactContext);
    }

    @ReactMethod // OK
    public void optOut() {
        Batch.optOut(reactContext);
    }

    @ReactMethod // OK
    public void optOutAndWipeData() {
        Batch.optOutAndWipeData(reactContext);
    }

    // PUSH MODULE

    @ReactMethod // OK
    public void push_registerForRemoteNotifications() { /* No effect on android */ }

    @ReactMethod // OK
    public void push_setNotificationTypes(Integer notifType) {
        EnumSet<PushNotificationType> pushTypes =  PushNotificationType.fromValue(notifType);
        Batch.Push.setNotificationsType(pushTypes);
    }

    @ReactMethod // OK
    public void push_clearBadge() { /* No effect on android */ }

    @ReactMethod // OK
    public void push_dismissNotifications() { /* No effect on android */ }

    @ReactMethod // OK
    public void push_getLastKnownPushToken(Promise promise) {
        String pushToken = Batch.Push.getLastKnownPushToken();
        promise.resolve(pushToken);
    }

    // MESSAGING MODULE

    @ReactMethod // OK
    public void messaging_setNotDisturbed(Boolean enabled) {
        Batch.Messaging.setDoNotDisturbEnabled(enabled);
    }

    @ReactMethod // OK
    public void messaging_showPendingMessages() {
        Boolean test = Batch.Messaging.isDoNotDisturbEnabled();
        BatchMessage msg = Batch.Messaging.popPendingMessage();
        if (msg != null) {
            Batch.Messaging.show(getCurrentActivity(), msg);
        }
    }

    // INBOX MODULE

    private static final int NOTIFICATIONS_COUNT = 100;

    @ReactMethod // OK - TODO: parse notification['com.batch'] in JS
    public void inbox_fetchNotifications(final Promise promise) {
        BatchInboxFetcher fetcher = Batch.Inbox.getFetcher(getCurrentActivity());
        fetcher.setFetchLimit(NOTIFICATIONS_COUNT);
        fetcher.setMaxPageSize(NOTIFICATIONS_COUNT);

        fetcher.fetchNewNotifications(new BatchInboxFetcher.OnNewNotificationsFetchedListener() {
            @Override
            public void onFetchSuccess(List<BatchInboxNotificationContent> notifications,
                                       boolean foundNewNotifications,
                                       boolean endReached)
            {
                WritableArray results = RNBatchInbox.getSuccessResponse(notifications);
                promise.resolve(results);
            }

            @Override
            public void onFetchFailure(String error)
            {
                promise.resolve(RNBatchInbox.getErrorResponse(error));
            }
        });
    }


    @ReactMethod // OK - TODO: parse notification['com.batch'] in JS
    public void inbox_fetchNotificationsForUserIdentifier(String userIdentifier, String authenticationKey, final Promise promise) {
        BatchInboxFetcher fetcher = Batch.Inbox.getFetcher(getCurrentActivity(), userIdentifier, authenticationKey);
        fetcher.setFetchLimit(NOTIFICATIONS_COUNT);
        fetcher.setMaxPageSize(NOTIFICATIONS_COUNT);

        fetcher.fetchNewNotifications(new BatchInboxFetcher.OnNewNotificationsFetchedListener() {
            @Override
            public void onFetchSuccess(List<BatchInboxNotificationContent> notifications,
                                       boolean foundNewNotifications,
                                       boolean endReached)
            {
                promise.resolve(RNBatchInbox.getSuccessResponse(notifications));
            }

            @Override
            public void onFetchFailure(String error)
            {
                promise.reject("InboxFetchError", "");
            }
        });
    }

    // USER DATA EDITOR MODULE

    @ReactMethod
    public void userData_getInstallationId(Promise promise) {
        String userId = Batch.User.getInstallationID();
        promise.resolve(userId);
    }

    @ReactMethod // OK
    public void userData_setLanguage(@Nullable String language) {
        BatchUserDataEditor editor = Batch.User.editor();
        if (editor != null)
        {
            editor.setLanguage(language);
            editor.save();
        }
    }

    @ReactMethod // OK
    public void userData_setRegion(@Nullable String region) {
        BatchUserDataEditor editor = Batch.User.editor();
        if (editor != null)
        {
            editor.setRegion(region);
            editor.save();
        }
    }

    @ReactMethod // OK
    public void userData_setIdentifier(@Nullable String identifier) {
        BatchUserDataEditor editor = Batch.User.editor();
        if (editor != null)
        {
            editor.setIdentifier(identifier);
            editor.save();
        }
    }

    @ReactMethod // OK
    public void userData_setAttributes(ReadableMap values) {
        BatchUserDataEditor editor = Batch.User.editor();
        if (editor != null)
        {
            ReadableMapKeySetIterator iterator = values.keySetIterator();
            while(iterator.hasNextKey()) {
                String valueKey = iterator.nextKey();
                ReadableType valueType = values.getType(valueKey);
                switch (valueType){
                    case Null:
                        editor.removeAttribute(valueKey);
                        break;
                    case Boolean:
                        editor.setAttribute(valueKey, values.getBoolean(valueKey));
                        break;
                    case Number:
                        editor.setAttribute(valueKey, values.getDouble(valueKey));
                        break;
                    case String:
                        editor.setAttribute(valueKey, values.getString(valueKey));
                        break;
                }
            }
            editor.save();
        }
    }

    @ReactMethod // OK
    public void userData_clearAttributes() {
        BatchUserDataEditor editor = Batch.User.editor();
        if (editor != null) {
            editor.clearAttributes();
            editor.save();
        }
    }

    @ReactMethod // OK
    public void userData_addTag(String collection, String tag) {
        BatchUserDataEditor editor = Batch.User.editor();
        if (editor != null) {
            editor.addTag(collection, tag);
            editor.save();
        }
    }

    @ReactMethod // OK
    public void userData_removeTag(String collection, String tag) {
        BatchUserDataEditor editor = Batch.User.editor();
        if (editor != null) {
            editor.removeTag(collection, tag);
            editor.save();
        }
    }

    @ReactMethod // OK
    public void userData_clearTags() {
        BatchUserDataEditor editor = Batch.User.editor();
        if (editor != null) {
            editor.clearTags();
            editor.save();
        }
    }

    @ReactMethod // TODO: Only lowercase
    public void userData_clearTagCollection(String collection) {
        BatchUserDataEditor editor = Batch.User.editor();
        if (editor != null) {
            editor.clearTagCollection(collection);
            editor.save();
        }
    }

    // EVENT LISTENERS

    @Override // OK
    public void onHostResume() { start(); }

    @Override // OK
    public void onHostPause() { Batch.onStop(getCurrentActivity()); }

    @Override // OK
    public void onHostDestroy() { Batch.onDestroy(getCurrentActivity()); }
}
