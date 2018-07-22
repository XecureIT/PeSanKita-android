package org.thoughtcrime.securesms.media;

import android.net.Uri;

import org.thoughtcrime.securesms.attachments.AttachmentId;

import java.util.Date;

public class Media {
  private AttachmentId id;
  private String fileName;
  private Uri uri;
  private String contentType;
  private long size;
  private Date date;

  public AttachmentId getId() {
    return id;
  }

  public void setId(AttachmentId id) {
    this.id = id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Uri getUri() {
    return uri;
  }

  public void setUri(Uri uri) {
    this.uri = uri;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}