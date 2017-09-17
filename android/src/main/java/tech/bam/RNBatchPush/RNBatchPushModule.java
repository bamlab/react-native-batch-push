package tech.bam.RNBatchPush;

import android.app.Activity;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.batch.android.Batch;
import com.batch.android.Config;

public class RNBatchPushModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

  private final ReactApplicationContext reactContext;

  public RNBatchPushModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.reactContext.addLifecycleEventListener(this);

    Batch.Push.setGCMSenderId(BuildConfig.GCM_SERVER_API_KEY);
    Batch.setConfig(new Config(BuildConfig.BATCH_API_KEY_ANDROID));

    startBatch();
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
