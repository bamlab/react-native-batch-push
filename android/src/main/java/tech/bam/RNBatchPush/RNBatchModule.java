package tech.bam.RNBatchPush;

import android.app.Activity;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.batch.android.Batch;
import com.batch.android.PushNotificationType;
import com.batch.android.BatchInboxFetcher;
import com.batch.android.BatchInboxNotificationContent;
import com.batch.android.BatchMessage;
import com.batch.android.BatchUserDataEditor;
import com.batch.android.Config;
import com.batch.android.json.JSONObject;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RNBatchModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String NAME = "RNBatch";

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

            Batch.setConfig(new Config(batchAPIKey));
        } catch (Exception e) {
            Log.e("RNBatchPush", e.getMessage());
        }
    }

    // BASE MODULE

    @ReactMethod
    public void start(final boolean doNotDisturb) {
        Activity activity = getCurrentActivity();
        if (activity == null)
            return;

        if (BATCH_STARTED == true) {
            return;
        }

        if (doNotDisturb) {
            Batch.Messaging.setDoNotDisturbEnabled(true);
        }

        Batch.onStart(activity);
        BATCH_STARTED = true;
    }

    public void start() {
        this.start(false);
    }

    @ReactMethod
    public void optIn() {
        Batch.optIn(reactContext);
    }

    @ReactMethod
    public void optOut() {
        Batch.optOut(reactContext);
    }

    @ReactMethod
    public void optOutAndWipeData() {
        Batch.optOutAndWipeData(reactContext);
    }

    // PUSH MODULE

    @ReactMethod
    public void push_registerForRemoteNotifications() { /* No effect on android */ }

    @ReactMethod
    public void push_setNotificationTypes(Integer notifType) {
        EnumSet<PushNotificationType> pushTypes =  PushNotificationType.fromValue(notifType);
        Batch.Push.setNotificationsType(pushTypes);
    }

    @ReactMethod
    public void push_clearBadge() { /* No effect on android */ }

    @ReactMethod
    public void push_dismissNotifications() { /* No effect on android */ }

    @ReactMethod
    public void push_getLastKnownPushToken(Promise promise) {
        String pushToken = Batch.Push.getLastKnownPushToken();
        promise.resolve(pushToken);
    }

    // MESSAGING MODULE

    @ReactMethod
    public void messaging_showPendingMessages() {
        Boolean test = Batch.Messaging.isDoNotDisturbEnabled();
        BatchMessage msg = Batch.Messaging.popPendingMessage();
        if (msg != null) {
            Batch.Messaging.show(getCurrentActivity(), msg);
        }
    }

    // INBOX MODULE

    private static final int NOTIFICATIONS_COUNT = 100;

    @ReactMethod
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
                promise.reject("InboxFetchError", error);
            }
        });
    }


    @ReactMethod
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
                promise.reject("InboxFetchError", error);
            }
        });
    }

    // USER DATA EDITOR MODULE

    @ReactMethod
    public void userData_getInstallationId(Promise promise) {
        String userId = Batch.User.getInstallationID();
        promise.resolve(userId);
    }

    @ReactMethod
    public void userData_save(ReadableArray actions) {
        BatchUserDataEditor editor = Batch.User.editor();
        for(int i = 0; i < actions.size(); i++) {
            ReadableMap action = actions.getMap(i);
            String type = action.getString("type");

            if(type.equals("setAttribute")) {
                String key = action.getString("key");
                ReadableType valueType = action.getType("value");
                switch (valueType){
                    case Null:
                        editor.removeAttribute(key);
                        break;
                    case Boolean:
                        editor.setAttribute(key, action.getBoolean("value"));
                        break;
                    case Number:
                        editor.setAttribute(key, action.getDouble("value"));
                        break;
                    case String:
                        editor.setAttribute(key, action.getString("value"));
                        break;
                }
            } else if (type.equals("setDateAttribute")) {
                String key = action.getString("key");
                long timestamp = action.getInt("value");
                Date date = new Date(timestamp);
                editor.setAttribute(key, date);
            } else if (type.equals("removeAttribute")) {
                String key = action.getString("key");
                editor.removeAttribute(key);
            } else if (type.equals("clearAttributes")) {
                editor.clearAttributes();
            } else if (type.equals("setIdentifier")) {
                ReadableType valueType = action.getType("value");
                if (valueType.equals(ReadableType.Null)) {
                    editor.setIdentifier(null);
                } else {
                    String value = action.getString("value");
                    editor.setIdentifier(value);
                }
            } else if (type.equals("setLanguage")) {
                ReadableType valueType = action.getType("value");
                if (valueType.equals(ReadableType.Null)) {
                    editor.setLanguage(null);
                } else {
                    String value = action.getString("value");
                    editor.setLanguage(value);
                }
            } else if (type.equals("setRegion")) {
                ReadableType valueType = action.getType("value");
                if (valueType.equals(ReadableType.Null)) {
                    editor.setRegion(null);
                } else {
                    String value = action.getString("value");
                    editor.setRegion(value);
                }
            } else if (type.equals("addTag")) {
                String collection = action.getString("collection");
                String tag = action.getString("tag");
                editor.addTag(collection, tag);
            } else if (type.equals("removeTag")) {
                String collection = action.getString("collection");
                String tag = action.getString("tag");
                editor.removeTag(collection, tag);
            } else if (type.equals("clearTagCollection")) {
                String collection = action.getString("collection");
                editor.clearTagCollection(collection);
            } else if (type.equals("clearTags")) {
                editor.clearTags();
            }
        }
        editor.save();
    }

    @ReactMethod
    public void userData_trackEvent(String name, String label, ReadableMap serializedEventData) {
        Batch.User.trackEvent(name, label, RNUtils.convertSerializedEventDataToEventData(serializedEventData));
    }

    @ReactMethod
    public void userData_trackTransaction(double amount, ReadableMap data) {
        Batch.User.trackTransaction(amount, new JSONObject(data.toHashMap()));
    }

    @ReactMethod
    public void userData_trackLocation(ReadableMap serializedLocation) {
        Location nativeLocation = new Location("tech.bam.RNBatchPush");
        nativeLocation.setLatitude(serializedLocation.getDouble("latitude"));
        nativeLocation.setLongitude(serializedLocation.getDouble("longitude"));

        if (serializedLocation.hasKey("precision")) {
            nativeLocation.setAccuracy((float) serializedLocation.getDouble("precision"));
        }

        if (serializedLocation.hasKey("date")) {
            nativeLocation.setTime((long) serializedLocation.getDouble("date"));
        }

        Batch.User.trackLocation(nativeLocation);
    }

    // EVENT LISTENERS

    @Override
    public void onHostResume() { start(); }

    @Override
    public void onHostPause() { Batch.onStop(getCurrentActivity()); }

    @Override
    public void onHostDestroy() { Batch.onDestroy(getCurrentActivity()); }
}
