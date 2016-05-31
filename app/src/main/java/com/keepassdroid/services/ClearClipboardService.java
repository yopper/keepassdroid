/*
 * Copyright 2009 Brian Pellin.
 *     
 * This file is part of KeePassDroid.
 *
 *  KeePassDroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.keepassdroid.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.android.keepass.R;
import com.keepassdroid.UIToastTask;
import com.keepassdroid.intents.Intents;
import com.keepassdroid.utils.Util;

public class ClearClipboardService extends IntentService {
	private static final String TAG = "KeePassDroid Clipboard";

    public ClearClipboardService() {
        super(TAG);
    }

    @Override
	protected void onHandleIntent(Intent intent) {
        if ( Intents.CLEAR_CLIPBOARD.equals(intent.getAction()) ) {
            if (!clearClipboard(this, intent.getStringExtra(Intent.EXTRA_TEXT))) {
                stopSelf();
            }
        }
	}

	private boolean clearClipboard(Context ctx, String toClear) {
        if (toClear == null) {
            return false;
        }
		Log.d(TAG, "Timeout(Clipboard)");
		try {
			String s  = Util.getClipboard(ctx);
			if (s == null || s.length() == 0) {
				return false;
			}
			if (toClear == null || TextUtils.equals(toClear, s)) {
                Util.clearClipboard(ctx);
				Log.d(TAG, "Clipboard cleared");
                Looper l = Looper.getMainLooper();
                if (l != null) {
                    new Handler(l).post(new UIToastTask(ctx, R.string.ClearClipboard){
                        @Override
                        public void run() {
                            super.run();
                            stopSelf();
                        }
                    });
                    return true;
                }
            }
		} catch (Exception e) {
			Log.e(TAG, "Clear clip failed." + e);
		}
        return false;
	}

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "ClearClipboard service started");
    }

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(TAG, "ClearClipboard service stopped");
	}
}
