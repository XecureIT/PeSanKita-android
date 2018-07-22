package org.thoughtcrime.securesms.media;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.MediaFragment;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.mms.DecryptableStreamUriLoader;

import java.util.ArrayList;


public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.viewHolder> {

  private final static String TAG = ThumbnailAdapter.class.getSimpleName();
  private Context context;
  private MasterSecret masterSecret;
  private ArrayList list;
  private ClickListener clickListener;
  private MediaFragment fragment = new MediaFragment();

  ArrayList getList() {
    return list;
  }

  public void setListData(ArrayList list) {
    this.list = list;
  }

  public ThumbnailAdapter(Context context, MasterSecret masterSecret) {
    this.context = context;
    this.masterSecret = masterSecret;
  }

  @Override
  public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_grid_thumbnail_item, parent, false);
    return new viewHolder(itemRow);
  }

  @Override
  public void onBindViewHolder(viewHolder holder, int position) {
    ImageView imageView = null;
    ArrayList item = (ArrayList) getList().get(position);

    if (item.size() > 0) {
      for (int i = 0; i < item.size(); i++) {
        RequestManager glide  = Glide.with(context);
        final Media media     = fragment.getItem(item, i);

        if (i == 0) {
          imageView = holder.thumbnail_1;
        } else if (i == 1) {
          imageView = holder.thumbnail_2;
        } else if (i == 2) {
          imageView = holder.thumbnail_3;
        } else if (i == 3) {
          imageView = holder.thumbnail_4;
        }

        if (media.getUri() != null) {
          glide.load(new DecryptableStreamUriLoader.DecryptableUri(masterSecret, media.getUri()))
              .crossFade()
              .centerCrop()
              .diskCacheStrategy(DiskCacheStrategy.NONE)
              .skipMemoryCache(true)
              .into(imageView);
        } else {
          glide.load(R.drawable.ic_video_light)
              .crossFade()
              .centerCrop()
              .diskCacheStrategy(DiskCacheStrategy.NONE)
              .skipMemoryCache(true)
              .into(imageView);
        }
      }
    } else {
      holder.linearLayout.setVisibility(View.GONE);
    }

    if ( position == 0 )
      holder.title.setText(R.string.tab_media_fragment__image);
    else
      holder.title.setText(R.string.tab_media_fragment__video);
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    ImageView thumbnail_1;
    ImageView thumbnail_2;
    ImageView thumbnail_3;
    ImageView thumbnail_4;
    TextView  title;
    LinearLayout linearLayout;

    public viewHolder(View itemView) {
      super(itemView);
      thumbnail_1     = (ImageView) itemView.findViewById(R.id.thumbnail_item_1);
      thumbnail_2     = (ImageView) itemView.findViewById(R.id.thumbnail_item_2);
      thumbnail_3     = (ImageView) itemView.findViewById(R.id.thumbnail_item_3);
      thumbnail_4     = (ImageView) itemView.findViewById(R.id.thumbnail_item_4);
      title           = (TextView) itemView.findViewById(R.id.thumbnail_item_title);
      linearLayout    = (LinearLayout) itemView.findViewById(R.id.linear_layout_grid_thumbnail);
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