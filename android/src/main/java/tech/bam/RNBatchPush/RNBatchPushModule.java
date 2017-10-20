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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class RNBatchPushModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

  private final ReactApplicationContext reactContext;
  private String batchAPIKey;
  private String batchInboxSecret;

  public RNBatchPushModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.reactContext.addLifecycleEventListener(this);

    try {
      Resources resources = reactContext.getResources();
      String packageName = reactContext.getApplicationContext().getPackageName();
      this.batchAPIKey = resources.getString(resources.getIdentifier("BATCH_API_KEY", "string", packageName));

      // Inbox is optional
      int batchInboxSecretIdentifier = resources.getIdentifier("BATCH_INBOX_SECRET", "string", packageName);
      if (batchInboxSecretIdentifier > 0) {
        this.batchInboxSecret = resources.getString(batchInboxSecretIdentifier);
      }

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
  public void fetchNewNotifications(String userID, final Promise promise) {
    try {
      SecretKeySpec signinKey = new SecretKeySpec(this.batchInboxSecret.getBytes(), "HmacSHA256");
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(signinKey);
      String hash = toHexString(mac.doFinal((this.batchAPIKey + userID).getBytes()));

      BatchInboxFetcher inboxFetcher = Batch.Inbox.getFetcher(userID, hash);
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
    } catch (NoSuchAlgorithmException exception) {
      Log.e("RNBatchPush", "HmacSHA256 is not available");
    } catch (InvalidKeyException exception) {
      Log.e("RNBatchPush", "Key is invalid");
    } catch (Exception exception) {
      Log.e("RNBatchPush", "Unknown exception: " + exception.getMessage());
    }
  }

  private static String toHexString(byte[] bytes) {
    Formatter formatter = new Formatter();

    for (byte b : bytes) {
      formatter.format("%02x", b);
    }

    return formatter.toString();
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
