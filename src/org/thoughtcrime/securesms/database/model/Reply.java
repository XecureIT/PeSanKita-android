package org.thoughtcrime.securesms.database.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import org.thoughtcrime.securesms.R;
import org.whispersystems.jobqueue.util.Base64;

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

  public static void setReplyThumbnail(Context context, Reply reply, ImageView imageView) {
    imageView.setVisibility(View.GONE);
    if (reply.getType() > REPLY_TYPE_TEXT) {
      if (reply.getType() == REPLY_TYPE_IMAGE) {
        Glide
            .with(context)
            .load(reply.getThumbnail() != null ? Base64.decode(reply.getThumbnail(), Base64.DEFAULT) : "")
            .asBitmap()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_image_light)
            .into(imageView);
      } else if (reply.getType() == REPLY_TYPE_VIDEO) {
        imageView.setImageResource(R.drawable.ic_video_light);
      } else if (reply.getType() == REPLY_TYPE_AUDIO) {
        imageView.setImageResource(R.drawable.ic_audio_light);
      } else if (reply.getType() == REPLY_TYPE_FILE) {
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