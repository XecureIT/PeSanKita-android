package org.thoughtcrime.securesms.service;

import android.content.Context;
import android.content.Intent;

import org.thoughtcrime.securesms.ApplicationContext;
import org.thoughtcrime.securesms.jobs.AutoRemoveJob;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

import java.util.concurrent.TimeUnit;

public class AutoRemoveListener extends PersistentAlarmManagerListener {

  private static long INTERVAL = 0;

  @Override
  protected long getNextScheduledExecutionTime(Context context) {
    return TextSecurePreferences.getAutoRemoveTime(context);
  }

  @Override
  protected long onAlarm(Context context, long scheduledTime) {
    if (TextSecurePreferences.isAutoRemoveEnabled(context)) {
      ApplicationContext.getInstance(context)
          .getJobManager()
          .add(new AutoRemoveJob(context));
    }

    long newTime = System.currentTimeMillis() + INTERVAL;
    TextSecurePreferences.setAutoRemoveTime(context, newTime);

    return newTime;
  }

  public static void schedule(Context context) {
    INTERVAL = TimeUnit.MINUTES.toMillis(TextSecurePreferences.getAutoRemoveTimeoutInterval(context));
    new AutoRemoveListener().onReceive(context, new Intent());
  }
}