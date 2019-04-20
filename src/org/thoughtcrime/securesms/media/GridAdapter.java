package org.thoughtcrime.securesms.media;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.util.MediaUtil;

import java.io.File;
import java.util.ArrayList;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.viewHolder> {
  private Context context;
  private ArrayList<Media> list;
  private ClickListener clickListener;

  ArrayList<Media> getList() {
    return list;
  }

  public void setListData(ArrayList<Media> list) {
    this.list = list;
  }

  public GridAdapter(Context context) {
    this.context = context;
  }

  @Override
  public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_grid_item, parent, false);
    return new viewHolder(itemRow);
  }

  @Override
  public void onBindViewHolder(viewHolder holder, int position) {
    String img = null;
    Media media = getList().get(position);
    if(media.getContentType() != null) {
      if(MediaUtil.isImageType(media.getContentType())) {
        img = String.valueOf(media.getUri());
        holder.playIcon.setVisibility(View.GONE);
      } else if (MediaUtil.isVideoType(media.getContentType())) {
        img = String.valueOf(Uri.fromFile(new File(String.valueOf(media.getUri()))));
        holder.playIcon.setVisibility(View.VISIBLE);
      }
    } else {
      img = String.valueOf(R.drawable.ic_attach_grey600_24dp);
      holder.playIcon.setVisibility(View.GONE);
    }

    GlideApp.with(context)
            .load(img)
            //.crossFade()
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(holder.image);
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    ImageView image;
    ImageView playIcon;

    public viewHolder(View itemView) {
      super(itemView);
      image     = (ImageView) itemView.findViewById(R.id.tab_media_thumbnail);
      playIcon  = (ImageView) itemView.findViewById(R.id.play_icon);
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