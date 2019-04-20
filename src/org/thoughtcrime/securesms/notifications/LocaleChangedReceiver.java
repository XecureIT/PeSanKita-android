package org.thoughtcrime.securesms.notifications;

/**
 * Created by rendra on 05/11/18.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationChannels.create(context);
    }
}
