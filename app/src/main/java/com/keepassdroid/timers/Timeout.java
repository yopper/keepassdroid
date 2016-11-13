package com.keepassdroid.timers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.keepass.R;
import com.keepassdroid.intents.Intents;
import com.keepassdroid.services.TimeoutService;
import com.keepassdroid.utils.EmptyUtils;
import com.keepassdroid.utils.Util;

public class Timeout {
	private static final int REQUEST_ID = 0;
	private static final long DEFAULT_TIMEOUT = 5 * 60 * 1000;  // 5 minutes
	private static String TAG = "KeePass Timeout";

	private static PendingIntent buildIntent(Context ctx) {
		Intent intent = new Intent(Intents.TIMEOUT);
		PendingIntent sender = PendingIntent.getBroadcast(ctx, REQUEST_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		return sender;
	}

    public static void start(Context ctx) {
        scheduleClearClipboard(ctx);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String sTimeout = prefs.getString(ctx.getString(R.string.app_timeout_key), ctx.getString(R.string.clipboard_timeout_default));

        long timeout;
        try {
            timeout = Long.parseLong(sTimeout);
        } catch (NumberFormatException e) {
            timeout = DEFAULT_TIMEOUT;
        }

        if ( timeout == -1 ) {
            // No timeout don't start timeout service
            return;
        }
        start(ctx, buildIntent(ctx), timeout);
    }

    private static boolean scheduleClearClipboard(Context ctx) {
        String last = Util.getLastClipboard();
        if (EmptyUtils.isNullOrEmpty(last)) {
            return false;
        }
        String cur = Util.getClipboard(ctx);
        if (!TextUtils.equals(cur, last)) {
            return false;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String sClipClear = prefs.getString(ctx.getString(R.string.clipboard_timeout_key), ctx.getString(R.string.clipboard_timeout_default));
        long clipClearTime = Long.parseLong(sClipClear);
        if (clipClearTime <= 0) {
            return false;
        }
        Intent intent = new Intent(Intents.CLEAR_CLIPBOARD);
        intent.putExtra(Intent.EXTRA_TEXT, cur);
        PendingIntent sender = PendingIntent.getService(ctx, REQUEST_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);

        start(ctx, sender, clipClearTime);
        return true;
    }
    private static void start(Context ctx, PendingIntent sender, long timeout) {

		ctx.startService(new Intent(ctx, TimeoutService.class));

		long triggerTime = System.currentTimeMillis() + timeout;
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		
		Log.d(TAG, "Timeout start");
		am.set(AlarmManager.RTC, triggerTime, sender);
	}

	public static void cancel(Context ctx) {
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		
		Log.d(TAG, "Timeout cancel");
		am.cancel(buildIntent(ctx));
		
		ctx.stopService(new Intent(ctx, TimeoutService.class));

	}

}
