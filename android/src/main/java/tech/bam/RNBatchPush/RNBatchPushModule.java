package tech.bam.RNBatchPush;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.batch.android.Batch;
import com.batch.android.BatchInboxFetcher;
import com.batch.android.BatchInboxNotificationContent;
import com.batch.android.Config;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;
import java.util.Map;

public class RNBatchPushModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

  private final ReactApplicationContext reactContext;
  private String batchAPIKey;

  public RNBatchPushModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.reactContext.addLifecycleEventListener(this);

    try {
      Resources resources = reactContext.getResources();
      String packageName = reactContext.getApplicationContext().getPackageName();
      this.batchAPIKey = resources.getString(resources.getIdentifier("BATCH_API_KEY", "string", packageName));

      Batch.Push.setGCMSenderId(resources.getString(resources.getIdentifier("GCM_SENDER_ID", "string", packageName)));
      Batch.setConfig(new Config(this.batchAPIKey));

      startBatch();
    } catch (Exception e) {
      Log.e("RNBatchPush", e.getMessage());
    }
  }

  private void startBatch() {
    Activity activity = getCurrentActivity();
    if (activity == null)
      return;

    Batch.onStart(activity);
  }

  @ReactMethod
  public void registerForRemoteNotifications() {
    // not needed on Android
    return;
  }

  @ReactMethod
  public void loginUser(String userID) {
    Batch.User.editor()
      .setIdentifier(userID)
      .save();
  }

  @ReactMethod
  public void logoutUser() {
    Batch.User.editor()
      .setIdentifier(null)
      .save();
  }

  @ReactMethod
  public void fetchNewNotifications(String userID, String authKey, final Promise promise) {
    try {
      BatchInboxFetcher inboxFetcher = Batch.Inbox.getFetcher(userID, authKey);
      inboxFetcher.fetchNewNotifications(new BatchInboxFetcher.OnNewNotificationsFetchedListener() {
        public void onFetchSuccess(@NonNull List<BatchInboxNotificationContent> notifications, boolean foundNewNotifications, boolean endReached) {
          WritableArray jsNotifications = Arguments.createArray();

          for (BatchInboxNotificationContent notification : notifications) {
            Bundle payloadBundle = new Bundle();
            for (Map.Entry<String, String> entry : notification.getRawPayload().entrySet()) {
              payloadBundle.putString(entry.getKey(), entry.getValue());
            }
            WritableMap jsNotification = Arguments.createMap();
            jsNotification.putMap("payload", Arguments.fromBundle(payloadBundle));
            jsNotification.putString("title", notification.getTitle());
            jsNotification.putString("body", notification.getBody());
            jsNotification.putDouble("timestamp", notification.getDate().getTime());
            jsNotifications.pushMap(jsNotification);
          }

          promise.resolve(jsNotifications);
        }

        public void onFetchFailure(@NonNull String error) {
          promise.reject("BATCH_ERROR", "Error fetching new notifications: " + error);
        }
      });
    } catch (Exception exception) {
      Log.e("RNBatchPush", "Unknown exception: " + exception.getMessage());
    }
  }

  @Override
  public void onHostResume()
  {
    startBatch();
  }

  @Override
  public void onHostPause()
  {
    Batch.onStop(getCurrentActivity());
  }

  @Override
  public void onHostDestroy()
  {
    Batch.onDestroy(getCurrentActivity());
  }

  @Override
  public String getName() {
    return "RNBatchPush";
  }
}
