package com.batch.cordova.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;

import com.batch.android.Batch;
import com.batch.android.BatchMessage;
import com.batch.android.BatchPushPayload;
import com.batch.android.LoggerDelegate;
import com.batch.cordova.interop.Action;
import com.batch.cordova.interop.Bridge;
import com.batch.cordova.interop.Callback;
import com.batch.cordova.interop.JSONHelper;
import com.batch.cordova.interop.SimplePromise;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchCordovaPlugin extends CordovaPlugin implements Callback, LoggerDelegate, Batch.Messaging.LifecycleListener
{
    static
    {
        System.setProperty(PLUGIN_VERSION_ENVIRONEMENT_VAR, PLUGIN_VERSION);
    }

    private String genericCallbackId;

    @Override
    protected void pluginInitialize()
    {
        super.pluginInitialize();
    }

    public void onReset()
    {
        super.onReset();
        genericCallbackId = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean execute(String action, String rawArgs, CallbackContext callbackContext) throws JSONException
    {
        if (action != null && action.startsWith("BA_"))
        {
            action = action.substring(3);
            SimplePromise<String> result = null;
            try
            {
                Map<String, Object> parametersMap = null;

                if ( rawArgs != null && !rawArgs.isEmpty() )
                {
                    try
                    {
                        final List<Object> parametersList = JSONHelper.toList(rawArgs);
                        if (parametersList != null && parametersList.size() > 0)
                        {
                            final Object firstItem = parametersList.get(0);

                            try
                            {
                                parametersMap = (Map<String, Object>) firstItem;
                            }
                            catch (ClassCastException e)
                            {
                                Log.e(TAG, "Error while sending action to Batch: invalid parameters.", e);
                                // Do nothing here, just ignore
                            }
                        }
                    }
                    catch (com.batch.android.json.JSONException e) {
                        throw new JSONException(e.getMessage());
                    }
                }

                if( parametersMap == null )
                {
                    parametersMap = new HashMap<String, Object>();
                }

                if( Action.START.getName().equals(action) )
                {
                    // Enable manual push to handle foreground notifications
                    Batch.Push.setManualDisplay(true);

                    // Set a messaging listener to forward events to cordova
                    Batch.Messaging.setLifecycleListener(this);

                    // Deliver the push that started the activity, if applicable
                    Intent intent = null;
                    try
                    {
                        intent = cordova.getActivity().getIntent();
                    }
                    catch (NullPointerException e)
                    {
                        Log.e(TAG, "Error while getting the activity from cordova.", e);
                        // Do nothing here, just ignore
                    }

                    if (intent != null)
                    {
                        sendPushFromIntent(intent, true, true);
                    }

                    // If Batch is already started, we don't need to forward that to the bridge
                    if (BATCH_STARTED)
                    {
                        return true;
                    }
                    BATCH_STARTED = true;
                }
                else if ( Action.SET_CONFIG.getName().equals(action) )
                {
                    parametersMap.put("logger", this);
                }

                result = Bridge.call(action, parametersMap, this, cordova.getActivity());
            }
            catch (org.json.JSONException e)
            {
                Log.e(TAG, "Error while deserializing JSON for Batch Bridge", e);
            }
            finally
            {
                if (result == null)
                {
                    result = SimplePromise.resolved("");
                }

                result.then(new SimplePromise.ThenRunnable<String>() {
                    @Override
                    public void run(String value)
                    {
                        // Don't use NO_RESULT, it is bugged
                        webView.sendPluginResult(new PluginResult(PluginResult.Status.OK, value), callbackContext.getCallbackId());
                    }
                });
            }

            return true;
        }
        else if ("_setupCallback".equals(action))
        {
            genericCallbackId = callbackContext.getCallbackId();
            //Log.d(TAG, "DEBUG: Setting up the generic callback " + callbackContext.getCallbackId());
            return true;
        }
        return false;
    }

    // Batch Bridge callback

    @Override
    public void callback(String s)
    {
        callback(s, new HashMap<String, Object>());
    }

    @Override
    public void callback(String s, Map<String, Object> map)
    {
        if (genericCallbackId == null)
        {
            Log.e(TAG, "Not sending callback to Batch, _setupCallback doesn't seem to have been called. Something bad happened.");
        }

        try
        {
            final Map<String, Object> resultArguments = new HashMap<String, Object>();
            resultArguments.put("action", s);
            resultArguments.put("result", map);
            final PluginResult result = new PluginResult(PluginResult.Status.OK, new org.json.JSONObject(JSONHelper.fromMap(resultArguments).toString()));
            result.setKeepCallback(true);
            webView.sendPluginResult(result, genericCallbackId);
        }
        catch (com.batch.android.json.JSONException e1)
        {
            Log.e(TAG, "Error while serializing callback parameters to JSON", e1);
        }
        catch (org.json.JSONException e2)
        {
            Log.e(TAG, "Error while serializing callback parameters to JSON", e2);
        }
    }

    public void log(String tag, String msg, Throwable throwable)
    {
        final String message = tag + " - " + msg + (throwable != null ? " - " + throwable.toString() : "");

        if (genericCallbackId == null)
        {
            Log.e(TAG, "Not sending log to Batch, _setupCallback doesn't seem to have been called.");
        }

        try
        {
            final Map<String, Object> resultArguments = new HashMap<String, Object>();
            resultArguments.put("action", "_log");
            resultArguments.put("message", message);
            final PluginResult result = new PluginResult(PluginResult.Status.OK, new org.json.JSONObject(JSONHelper.fromMap(resultArguments).toString()));
            result.setKeepCallback(true);
            webView.sendPluginResult(result, genericCallbackId);
        }
        catch (com.batch.android.json.JSONException e1)
        {
            Log.e(TAG, "Error while serializing callback parameters to JSON", e1);
        }
        catch (org.json.JSONException e2)
        {
            Log.e(TAG, "Error while serializing callback parameters to JSON", e2);
        }
    }

    /**
     * Sends the push received event to cordova if a push is found in the intent
     * @param intent Intent in which the push is
     * @param forceDelivery Force the push delivery even if Batch isn't started
     */
    public void sendPushFromIntent(Intent intent, boolean forceDelivery, boolean startedApp)
    {
        if (genericCallbackId == null || (!forceDelivery && !BATCH_STARTED))
        {
            return;
        }

        if (intent.hasExtra(INTENT_EXTRA_CONSUMED_PUSH))
        {
            return;
        }

        Bundle extras = intent.getExtras();
        if (extras != null && !extras.isEmpty())
        {
            Bundle payload = extras.getBundle(Batch.Push.PAYLOAD_KEY);
            if (payload != null && !payload.isEmpty())
            {
                final JSONObject jsonPayload = new JSONObject();

                for (String key : payload.keySet())
                {
                    Object value = payload.get(key);
                    if (value == null)
                    {
                        continue;
                    }

                    if (!(value instanceof String))
                    {
                        value = value.toString();
                    }

                    try
                    {
                        jsonPayload.put(key, value);
                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, "Error while parsing push payload.", e);
                        return;
                    }
                }

                Log.d(TAG, "JSON payload to cordova : " + jsonPayload.toString());

                intent.putExtra(INTENT_EXTRA_CONSUMED_PUSH, true);

                boolean hasLandingMessage = false;
                try
                {
                    BatchPushPayload parsedPayload = BatchPushPayload.payloadFromBundle(extras);
                    if (parsedPayload != null && parsedPayload.hasLandingMessage())
                    {
                        hasLandingMessage = true;
                    }
                }
                catch (BatchPushPayload.ParsingException e)
                {
                    Log.e(TAG, "Error while checking if the push contains a landing.", e);
                }

                try
                {
                    final JSONObject jsonResult = new JSONObject();
                    jsonResult.put("action", "_dispatchPush");
                    jsonResult.put("payload", jsonPayload);
                    jsonResult.put("hasLandingMessage", hasLandingMessage);
                    jsonResult.put("didStartApp", startedApp);

                    final PluginResult result = new PluginResult(PluginResult.Status.OK, jsonResult);
                    result.setKeepCallback(true);
                    webView.sendPluginResult(result, genericCallbackId);
                }
                catch (org.json.JSONException e)
                {
                    Log.e(TAG, "Error while sending push payload to cordova.", e);
                }
            }
        }
    }

    // Activity lifecycle methods

    @Override
    public void onStart()
    {
        super.onStart();
        if (BATCH_STARTED)
        {
            Batch.onStart(cordova.getActivity());
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_FOREGROUND_PUSH);
        intentFilter.addAction(ACTION_DISPLAY_LANDING_BANNER);
        LocalBroadcastManager.getInstance(cordova.getActivity()).registerReceiver(foregroundPushReceiver, intentFilter);
    }

    @Override
    public void onStop()
    {
        LocalBroadcastManager.getInstance(cordova.getActivity()).unregisterReceiver(foregroundPushReceiver);
        Batch.onStop(cordova.getActivity());
        super.onStop();
    }

    @Override
    public void onPause(boolean multitasking)
    {
        super.onPause(multitasking);
        resumeCount.decrementAndGet();
    }

    @Override
    public void onResume(boolean multitasking)
    {
        super.onResume(multitasking);
        resumeCount.incrementAndGet();
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        Batch.onNewIntent(cordova.getActivity(), intent);
        sendPushFromIntent(intent, false, false);
    }

    @Override
    public void onDestroy()
    {
        Batch.onDestroy(cordova.getActivity());
        super.onDestroy();
    }

    /****
     * Batch Logger delegate methods
     */

    @Override
    public void error(String tag, String message, Throwable throwable)
    {
        log(tag, message, throwable);
    }

    @Override
    public void warn(String tag, String message, Throwable throwable)
    {
        log(tag, message, throwable);
    }

    @Override
    public void debug(String tag, String message, Throwable throwable)
    {
        log(tag, message, throwable);
    }

    @Override
    public void info(String tag, String message, Throwable throwable)
    {
        log(tag, message, throwable);
    }

    @Override
    public void verbose(String tag, String message, Throwable throwable)
    {
        log(tag, message, throwable);
    }

    /****
     * Batch Messaging listener methods
     */
    @Override
    public void onBatchMessageShown(String messageIdentifier)
    {
        dispatchMessagingEvent("shown", messageIdentifier);
    }

    @Override
    public void onBatchMessageClosed(String messageIdentifier)
    {
        dispatchMessagingEvent("closed", messageIdentifier);
    }

    private void dispatchMessagingEvent(String lifecycleEvent, String messageIdentifier)
    {
        if (genericCallbackId == null)
        {
            return;
        }

        try
        {
            final JSONObject json = new JSONObject();
            json.put("action", "_dispatchMessagingEvent");
            json.put("lifecycleEvent", lifecycleEvent);

            if (messageIdentifier != null) {
                json.put("messageIdentifier", messageIdentifier);
            }

            final PluginResult result = new PluginResult(PluginResult.Status.OK, json);
            result.setKeepCallback(true);
            webView.sendPluginResult(result, genericCallbackId);
        }
        catch (org.json.JSONException e)
        {
            Log.e(TAG, "Error while sending push payload to cordova.", e);
        }
    }

    /****
     * Foreground push
     */
    public static boolean isApplicationInForeground()
    {
        int count = resumeCount.get();

        // Fix negative resume counts
        if( count < 0 )
        {
            Log.e(TAG, "BatchCordovaPlugin's Activity resume counter is < 0. Something went wrong at some point.");

            // Check that it is still in an invalid state, this may have changed while logging the issue.
            // Just like AtomicInteger's incrementAndGet() works, try until we are satisfied.
            for (;;)
            {
                int current = resumeCount.get();
                if( count < 0 )
                {
                    if( resumeCount.compareAndSet(count, 0) )
                    {
                        return false;
                    }
                }
                else
                {
                    // This was fixed by another thread, disregard and wish the developer well
                    return current != 0;
                }
            }
        }

        return count != 0;
    }

    private void handleForegroundPush(Intent intent) {
        sendPushFromIntent(intent, true, false);
    }

    private void displayLandingBanner(Bundle messageBundle) {
        try {
            BatchMessage message = BatchMessage.getMessageForBundle(messageBundle);
            BatchMessage.Format format = message.getFormat();
            if (format == BatchMessage.Format.BANNER) {
                Batch.Messaging.show(cordova.getActivity(), message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not display banner", e);
        }
    }

    private BroadcastReceiver foregroundPushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_FOREGROUND_PUSH.equalsIgnoreCase(intent.getAction())) {
                handleForegroundPush(intent);
            } else if (ACTION_DISPLAY_LANDING_BANNER.equalsIgnoreCase(intent.getAction())) {
                displayLandingBanner(intent.getExtras());
            }
        }
    };
}