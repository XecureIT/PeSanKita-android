package org.thoughtcrime.securesms.media;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.thoughtcrime.securesms.attachments.DatabaseAttachment;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.AttachmentDatabase;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.MediaDatabase;
import org.thoughtcrime.securesms.util.MediaUtil;

import java.util.ArrayList;
import java.util.Date;

public class MediaData {
  private static final String TAG = Media.class.getSimpleName();
  private Context context;
  private MasterSecret masterSecret;
  private Media media = null;
  private ArrayList<Media> list = new ArrayList<>();
  private ArrayList             listThumbnail = new ArrayList<>();
  private ArrayList<Media> listImage = new ArrayList<>();
  private ArrayList<Media> listVideo = new ArrayList<>();
  private ArrayList<Media> listAudio = new ArrayList<>();
  private ArrayList<Media> listFile = new ArrayList<>();

  public MediaData(Context context, MasterSecret masterSecret) {
    this.context = context;
    this.masterSecret = masterSecret;
    loadDataFromDatabase();
  }

  public ArrayList<Media> getListData() {
    return this.list;
  }

  public ArrayList getListThumbnail() {
    int limit = 4;
    ArrayList<Media> tmpImg = new ArrayList<>();
    ArrayList<Media> tmpVid = new ArrayList<>();

    if (listImage.size() < limit)
      limit = listImage.size();

    for (int i=0; i < limit ; i++) {
      tmpImg.add(listImage.get(i));
    }

    if (listVideo.size() < limit)
      limit = listVideo.size();

    for (int i=0; i < limit ; i++) {
      tmpVid.add(listVideo.get(i));
    }

    listThumbnail.add(tmpImg);
    listThumbnail.add(tmpVid);

    return listThumbnail;
  }

  public ArrayList<Media> getListImage() {
    return this.listImage;
  }

  public ArrayList<Media> getListVideo() {
    return this.listVideo;
  }

  public ArrayList<Media> getListAudio() {
    return this.listAudio;
  }

  public ArrayList<Media> getListAudio(int limit) {
    int l = limit;
    ArrayList<Media> tmp = new ArrayList<>();

    if (listAudio.size() < l)
      l = listAudio.size();

    for (int i=0; i < l ; i++) {
      tmp.add(listAudio.get(i));
    }

    return tmp;
  }

  public ArrayList<Media> getListFile() {
    return this.listFile;
  }

  public ArrayList<Media> getListFile(int limit) {
    int l = limit;
    ArrayList<Media> tmp = new ArrayList<>();

    if (listFile.size() < l)
      l = listFile.size();

    for (int i=0; i < l ; i++) {
      tmp.add(listFile.get(i));
    }

    return tmp;
  }

  public void loadDataFromDatabase() {
    Cursor cursor = DatabaseFactory.getMediaDatabase(context).getAllMedia();

    while (cursor != null && cursor.moveToNext()) {
      media = new Media();
      final MediaDatabase.MediaRecord mediaRecord = MediaDatabase.MediaRecord.from(context, masterSecret, cursor);

      DatabaseAttachment databaseAttachment = (DatabaseAttachment) mediaRecord.getAttachment();

      media.setId(databaseAttachment.getAttachmentId());

      if (mediaRecord.getAttachment().getFileName() == null &&
          cursor.getString(cursor.getColumnIndexOrThrow(AttachmentDatabase.FN)) == null) {
        media.setFileName("");
      } else if (mediaRecord.getAttachment().getFileName() == null){
        media.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(AttachmentDatabase.FN)));
      } else {
        media.setFileName(mediaRecord.getAttachment().getFileName());
      }

      media.setContentType(mediaRecord.getContentType());
      media.setSize(mediaRecord.getAttachment().getSize());
      media.setDate(new Date(mediaRecord.getDate()));

      list.add(media);

      if (MediaUtil.isVideoType(mediaRecord.getContentType())) {
        media.setUri(mediaRecord.getAttachment().getThumbnailUri());
        listVideo.add(media);
      } else {
        media.setUri(mediaRecord.getAttachment().getDataUri());

        if (MediaUtil.isImageType(mediaRecord.getContentType())) {
          listImage.add(media);
        }  else if (MediaUtil.isAudioType(mediaRecord.getContentType())) {
          listAudio.add(media);
        } else {
          listFile.add(media);
        }
      }
    }
    cursor.close();
  }
}