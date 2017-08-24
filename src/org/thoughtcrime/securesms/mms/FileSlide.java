package org.thoughtcrime.securesms.mms;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.util.ResUtil;

import ws.com.google.android.mms.ContentType;

public class FileSlide extends Slide {

    public FileSlide(Context context, Attachment attachment) {
        super(context, attachment);
    }

    public FileSlide(Context context, Uri uri, long size) {
        super(context, constructAttachmentFromUri(context, uri, ContentType.MULTIPART_RELATED, size, false));
    }

    @Override
    public boolean hasPlaceholder() {
        return true;
    }

    @Override
    public @DrawableRes int getPlaceholderRes(Resources.Theme theme) {
        return ResUtil.getDrawableRes(theme, R.attr.conversation_icon_attach_file);
    }

    public boolean hasImage() {
        return true;
    }

    @NonNull
    @Override
    public String getContentDescription() {
        return "File";
    }
}
