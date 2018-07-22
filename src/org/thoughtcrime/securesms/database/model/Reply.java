package org.thoughtcrime.securesms.database.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.database.Address;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.whispersystems.jobqueue.util.Base64;
import org.whispersystems.libsignal.util.guava.Optional;

public class Reply {
  private final static String TAG = Reply.class.getSimpleName();

  public static final Integer REPLY_TYPE_TEXT = 1;
  public static final Integer REPLY_TYPE_IMAGE = 2;
  public static final Integer REPLY_TYPE_VIDEO = 3;
  public static final Integer REPLY_TYPE_AUDIO = 4;
  public static final Integer REPLY_TYPE_FILE = 5;

  private String number;
  private String text;
  private int type;
  private String thumbnail;

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

  public static Reply parseReplyBody(String replyBody) {
    if (!TextUtils.isEmpty(replyBody)) {
      try {
        return new Gson().fromJson(replyBody, Reply.class);
      } catch (Exception e) {
        Log.w(TAG, "Cannot deserialize: " + e.getMessage());
      }
    }
    return null;
  }

  public void setReplyRecipient(final Context context, final TextView textView) {
    new AsyncTask<Void, Void, Optional<Recipient>>() {
      @Override
      protected @NonNull
      Optional<Recipient> doInBackground(Void... params) {
        return Optional.of(Recipient.from(context, Address.fromExternal(context, number), false));
      }

      @Override
      protected void onPostExecute(@NonNull Optional<Recipient> recipient) {
        if(recipient.isPresent()) {
          textView.setText(recipient.get().toShortString());
        } else {
          textView.setText(number);
        }
      }
    }.execute();
  }

  public void setReplyThumbnail(final ImageView imageView) {
    imageView.setVisibility(View.GONE);
    if (type > REPLY_TYPE_TEXT) {
      if (type == REPLY_TYPE_IMAGE) {
        new AsyncTask<Void, Void, Bitmap>() {
          @Override
          protected @Nullable
          Bitmap doInBackground(Void... params) {
            try {
              byte[] decodedByte = Base64.decode(thumbnail, Base64.DEFAULT);
              return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            } catch (Exception e) {
              Log.w(TAG, e);
              return null;
            }
          }

          @Override
          protected void onPostExecute(@Nullable Bitmap bitmap) {
            if (bitmap != null) {
              imageView.setImageBitmap(bitmap);
            } else {
              imageView.setImageResource(R.drawable.ic_image_light);
            }
          }
        }.execute();

      } else if (type == REPLY_TYPE_VIDEO) {
        imageView.setImageResource(R.drawable.ic_video_light);
      } else if (type == REPLY_TYPE_AUDIO) {
        imageView.setImageResource(R.drawable.ic_audio_light);
      } else if (type == REPLY_TYPE_FILE) {
        imageView.setImageResource(R.drawable.ic_file_black_36dp);
      }
      imageView.setVisibility(View.VISIBLE);
    }
  }

  public void clear() {
    this.number     = null;
    this.text       = null;
    this.type       = 0;
    this.thumbnail  = null;
  }
}