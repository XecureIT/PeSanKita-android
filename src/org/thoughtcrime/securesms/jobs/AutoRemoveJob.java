package org.thoughtcrime.securesms.jobs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.NoExternalStorageException;
import org.thoughtcrime.securesms.util.StorageUtil;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.whispersystems.jobqueue.JobParameters;
import org.whispersystems.jobqueue.requirements.NetworkRequirement;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class AutoRemoveJob extends ContextJob {

  private static final String TAG = AutoRemoveJob.class.getSimpleName();
  @Nullable
  private transient MasterSecret masterSecret;

  public AutoRemoveJob(@NonNull Context context) {
    this(context, null);
  }

  public AutoRemoveJob(@NonNull Context context,
                       @Nullable MasterSecret masterSecret)
  {
    super(context, JobParameters.newBuilder()
        .withGroupId(AutoRemoveJob.class.getSimpleName())
        .withRequirement(new NetworkRequirement(context))
        .create());

    this.masterSecret = masterSecret;
  }

  @Override
  public void onAdded() {}

  @Override
  public void onRun() {
    try {
      walkAndRemove(StorageUtil.getAudioDir());
      walkAndRemove(StorageUtil.getDownloadDir());
      walkAndRemove(StorageUtil.getImageDir());
      walkAndRemove(StorageUtil.getVideoDir());
    } catch (NoExternalStorageException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean onShouldRetry(Exception e) {
    return false;
  }

  @Override
  public void onCanceled() {}

  public void walkAndRemove(File directory) {
    File files[] = directory.listFiles();

    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          walkAndRemove(files[i]);
        } else {
          File file = files[i].getAbsoluteFile();
          long age  = TimeUnit.MINUTES.toMillis(TextSecurePreferences.getSavedMediaAge(context));

          if ( System.currentTimeMillis() - file.lastModified() >= age && file.canWrite()) {
            file.delete();
          }
        }
      }
    }
  }
}