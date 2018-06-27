package tech.bam.RNBatchPush;

import android.app.Activity;
import android.util.Log;

import com.batch.android.BatchInboxNotificationContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class RNBatchInbox {
    final private static String TAG = "BatchRNPluginInbox";

    protected static JSONObject getSuccessResponse(List<BatchInboxNotificationContent> notifications)
    {
        try {
            final JSONArray jsonNotifications = new JSONArray();
            for (BatchInboxNotificationContent notification : notifications) {
                try {
                    jsonNotifications.put(getJSONNotification(notification));
                } catch (JSONException e1) {
                    Log.d(TAG, "Could not convert notification", e1);
                }
            }

            final JSONObject json = new JSONObject();

            json.put("notifications", jsonNotifications);

            return json;
        } catch (JSONException e) {
            Log.d(TAG, "Could not convert notifications", e);
            return getErrorResponse("Internal native error (-201)");
        }
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

    private static JSONObject getJSONNotification(BatchInboxNotificationContent notification) throws JSONException
    {
        final JSONObject json = new JSONObject();

        json.put("identifier", notification.getNotificationIdentifier());
        json.put("body", notification.getBody());
        json.put("is_unread", notification.isUnread());
        json.put("date", notification.getDate().getTime());

        int source = 0; // UNKNOWN
        switch (notification.getSource()) {
            case CAMPAIGN:
                source = 1;
                break;
            case TRANSACTIONAL:
                source = 2;
                break;
        }
        json.put("source", source);

        final String title = notification.getTitle();
        if (title != null) {
            json.put("title", title);
        }

        json.put("payload", pushPayloadToJSON(notification.getRawPayload()));

        return json;
    }

    private static JSONObject pushPayloadToJSON(Map<String, String> payload)
    {
        try {
            final JSONObject jsonPayload = new JSONObject();

            for (String key : payload.keySet()) {
                Object value = payload.get(key);
                if (value == null) {
                    continue;
                }

                jsonPayload.put(key, value);
            }

            return jsonPayload;
        } catch (JSONException e) {
            return new JSONObject();
        }
    }
}
