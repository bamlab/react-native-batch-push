package tech.bam.RNBatchPush;

import android.support.annotation.Nullable;
import android.util.Log;

import com.batch.android.BatchEventData;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;

public class RNUtils {
    public static WritableMap convertMapToWritableMap(Map<String, Object> input) {
        WritableMap output = new WritableNativeMap();

         for(Map.Entry<String, Object> entry: input.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                output.putMap(key, convertMapToWritableMap((Map<String, Object>) value));
            } else if (value instanceof JSONArray) {
                output.putArray(key, convertArrayToWritableArray((Object[])value));
            } else if (value instanceof  Boolean) {
                output.putBoolean(key, (Boolean) value);
            } else if (value instanceof  Integer) {
                output.putInt(key, (Integer) value);
            } else if (value instanceof  Double) {
                output.putDouble(key, (Double) value);
            } else if (value instanceof String)  {
                output.putString(key, (String) value);
            } else {
                output.putString(key, value.toString());
            }
        }
        return output;
    }

    public static WritableArray convertArrayToWritableArray(Object[] input) {
        WritableArray output = new WritableNativeArray();

        for (int i = 0; i < input.length; i++) {
            Object value = input[i];
            if (value instanceof Map) {
                output.pushMap(convertMapToWritableMap((Map<String, Object>) value));
            } else if (value instanceof  JSONArray) {
                output.pushArray(convertArrayToWritableArray((Object[]) value));
            } else if (value instanceof  Boolean) {
                output.pushBoolean((Boolean) value);
            } else if (value instanceof  Integer) {
                output.pushInt((Integer) value);
            } else if (value instanceof  Double) {
                output.pushDouble((Double) value);
            } else if (value instanceof String)  {
                output.pushString((String) value);
            } else {
                output.pushString(value.toString());
            }
        }
        return output;
    }

    @Nullable
    public static BatchEventData convertSerializedEventDataToEventData(@Nullable ReadableMap serializedEventData) {
        if (serializedEventData == null) {
            return null;
        }

        BatchEventData batchEventData = new BatchEventData();
        ReadableArray tags = serializedEventData.getArray("tags");

        for (int i = 0; i < tags.size(); i++) {
            batchEventData.addTag(tags.getString(i));
        }

        ReadableMap attributes = serializedEventData.getMap("attributes");
        ReadableMapKeySetIterator iterator = attributes.keySetIterator();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableMap valueMap = attributes.getMap(key);

            String type = valueMap.getString("type");
            if ("string".equals(type)) {
                batchEventData.put(key, valueMap.getString("value"));
            } else if ("boolean".equals(type)) {
                batchEventData.put(key, valueMap.getBoolean("value"));
            } else if ("integer".equals(type)) {
                batchEventData.put(key, valueMap.getDouble("value"));
            } else if ("float".equals(type)) {
                batchEventData.put(key, valueMap.getDouble("value"));
            } else {
                Log.e("RNBatchPush", "Invalid parameter : Unknown event_data.attributes type (" + type + ")");
            }
        }

        return batchEventData;
    }
}
