package tech.bam.RNBatchPush;

import android.app.Activity;
import android.util.Log;

import com.batch.android.BatchInboxNotificationContent;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class RNBatchInbox {
    final private static String TAG = "BatchRNPluginInbox";

    protected static WritableArray getSuccessResponse(List<BatchInboxNotificationContent> notifications)
    {
        final WritableArray rnNotifications = new WritableNativeArray();
        for (BatchInboxNotificationContent notification : notifications) {
            rnNotifications.pushMap(getWritableMapNotification(notification));
        }

        return rnNotifications;
    }

    protected static JSONObject getErrorResponse(String reason)
    {
        try {
            final JSONObject json = new JSONObject();
            json.put("error", reason);
            return json;
        } catch (JSONException e) {
            Log.d(TAG, "Could not convert error", e);
            try {
                return new JSONObject().put("error", "Internal native error (-200)");
            } catch (JSONException error) {
                // Used to prevent above message to require JSONException handling
                return new JSONObject();
            }
        }
    }

    private static WritableMap getWritableMapNotification(BatchInboxNotificationContent notification)
    {
        final WritableMap output = new WritableNativeMap();

        output.putString("identifier", notification.getNotificationIdentifier());
        output.putString("body", notification.getBody());
        output.putBoolean("isUnread", notification.isUnread());
        output.putDouble("date", notification.getDate().getTime());

        int source = 0; // UNKNOWN
        switch (notification.getSource()) {
            case CAMPAIGN:
                source = 1;
                break;
            case TRANSACTIONAL:
                source = 2;
                break;
        }
        output.putInt("source", source);

        final String title = notification.getTitle();
        if (title != null) {
            output.putString("title", title);
        }

        output.putMap("payload", RNUtils.convertMapToWritableMap((Map) notification.getRawPayload()));

        return output;
    }

    private static WritableMap pushPayloadToWritableMap(Map<String, Object> payload)
    {
        return RNUtils.convertMapToWritableMap(payload);
    }
}
