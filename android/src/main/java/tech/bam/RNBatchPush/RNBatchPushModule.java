package tech.bam.RNBatchPush;

import android.app.Activity;
import android.util.Log;
import android.content.res.Resources;
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

    try {
      Resources resources = reactContext.getResources();
      String packageName = reactContext.getApplicationContext().getPackageName();

      Batch.Push.setGCMSenderId(resources.getString(resources.getIdentifier("GCM_SENDER_ID", "string", packageName)));
      Batch.setConfig(new Config(resources.getString(resources.getIdentifier("BATCH_API_KEY", "string", packageName))));

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
