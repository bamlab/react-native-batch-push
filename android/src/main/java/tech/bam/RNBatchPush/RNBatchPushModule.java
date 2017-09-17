package tech.bam.RNBatchPush;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

public class RNBatchPushModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNBatchPushModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @ReactMethod
  public void registerForRemoteNotifications() {
    // not needed on Android
    return;
  }

  @ReactMethod
  public void setCustomUserID(String userID) {
    com.batch.android.Batch.User.editor()
      .setIdentifier(userID)
      .save();
  }

  @Override
  public String getName() {
    return "RNBatchPush";
  }
}
