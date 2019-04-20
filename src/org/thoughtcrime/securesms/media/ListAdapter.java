package org.thoughtcrime.securesms.media;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.util.MediaUtil;
import org.thoughtcrime.securesms.util.Util;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.viewHolder> {
  private Context context;
  private ArrayList<Media> list;
  private ClickListener clickListener;

  ArrayList<Media> getList() {
    return list;
  }

  public void setListData(ArrayList<Media> list) {
    this.list = list;
  }

  public ListAdapter(Context context) {
    this.context = context;
  }

  @Override
  public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_list_item, parent, false);
    return new viewHolder(itemRow);
  }

  @Override
  public void onBindViewHolder(viewHolder holder, int position) {
    int img;
    Media media = getList().get(position);
    holder.filename.setText(media.getFileName());
    holder.description.setText(Util.getPrettyFileSize(media.getSize()));
    if (media.getContentType() != null) {
      if (MediaUtil.isAudioType(media.getContentType())) {
        img = R.drawable.ic_audio;
      } else if (media.getContentType().startsWith("application/pdf")) {
        img = R.drawable.ic_application_pdf;
      } else if (media.getContentType().startsWith("application/zip")) {
        img = R.drawable.ic_application_zip;
      } else {
        img = R.drawable.ic_unknown_filetype;
      }
    } else {
      img = R.drawable.ic_unknown_filetype;
    }

    GlideApp.with(context)
            .load(img)
            //.crossFade()
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_unknown_filetype)
            .into(holder.image);
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    ImageView image;
    TextView filename;
    TextView description;

    public viewHolder(View itemView) {
      super(itemView);
      image = (ImageView) itemView.findViewById(R.id.tab_media_thumbnail);
      filename = (TextView) itemView.findViewById(R.id.tab_media_filename);
      description = (TextView) itemView.findViewById(R.id.tab_media_description);
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      clickListener.onItemClick(getAdapterPosition(), v);
    }
  }

  public void setOnItemClickListener(ClickListener clickListener) {
    this.clickListener = clickListener;
  }

  public interface ClickListener {
    void onItemClick(int position, View v);
  }
}