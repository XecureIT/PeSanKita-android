package org.thoughtcrime.securesms.jobs;

import android.content.Context;
import android.util.Log;

import org.thoughtcrime.securesms.dependencies.InjectableType;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.whispersystems.jobqueue.JobParameters;
import org.whispersystems.jobqueue.requirements.NetworkRequirement;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.messages.SignalServiceEnvelope;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class PushNotificationReceiveJob extends PushReceivedJob implements InjectableType {

  private static final String TAG = PushNotificationReceiveJob.class.getSimpleName();

  @Inject transient SignalServiceMessageReceiver receiver;

  public PushNotificationReceiveJob(Context context) {
    super(context, JobParameters.newBuilder()
                                .withRequirement(new NetworkRequirement(context))
                                .withGroupId("__notification_received")
                                .withWakeLock(true, 30, TimeUnit.SECONDS).create());
  }

  @Override
  public void onAdded() {}

  @Override
  public void onRun() throws IOException {
    receiver.retrieveMessages(new SignalServiceMessageReceiver.MessageReceivedCallback() {
      @Override
      public void onMessage(SignalServiceEnvelope envelope) {
        handle(envelope);
      }
    });
  }

  public void pullAndProcessMessages(SignalServiceMessageReceiver receiver, String tag, long startTime) throws IOException {
    synchronized (PushReceivedJob.RECEIVE_LOCK) {
      receiver.retrieveMessages(envelope -> {
        Log.i(tag, "Retrieved an envelope." + timeSuffix(startTime));
        processEnvelope(envelope);
        Log.i(tag, "Successfully processed an envelope." + timeSuffix(startTime));
      });
      TextSecurePreferences.setNeedsMessagePull(context, false);
    }
  }

  @Override
  public boolean onShouldRetry(Exception e) {
    Log.w(TAG, e);
    return e instanceof PushNetworkException;
  }

  @Override
  public void onCanceled() {
    Log.w(TAG, "***** Failed to download pending message!");
//    MessageNotifier.notifyMessagesPending(getContext());
  }

  private static String timeSuffix(long startTime) {
    return " (" + (System.currentTimeMillis() - startTime) + " ms elapsed)";
  }
}
