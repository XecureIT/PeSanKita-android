package org.thoughtcrime.securesms.applications;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.thoughtcrime.securesms.R;

import java.util.ArrayList;

/**
 * Created by winardiaris on 14/02/18.
 */

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.GridViewHolder> {
  private Context context;
  private ArrayList<Application> list;

  public void setList(ArrayList<Application> list) {
    this.list = list;
  }

  public ArrayList<Application> getList() {
    return list;
  }

  public ApplicationAdapter(Context context) {
    this.context = context;
  }

  @Override
  public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.applications_item_grid, parent, false);
    return new GridViewHolder(view);
  }

  @Override
  public void onBindViewHolder(GridViewHolder holder, int position) {
    holder.applicationId.setText(getList().get(position).getId());
    holder.applicationClass.setText(getList().get(position).getApplicationClass());

    holder.applicationName.setText(context.getResources().getIdentifier(
        getList().get(position).getApplicationName(), "string",
        context.getPackageName()
    ));

    Glide.with(context)
        .load(context.getResources()
            .getIdentifier(
                getList().get(position).getApplicationIcon(), "drawable",
                context.getPackageName()
            ))
        .override(100,100)
        .crossFade()
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .placeholder(R.drawable.ic_image_light)
        .into(holder.applicationIcon);

    holder.applicationContainer.setOnClickListener(
        new ApplicationItemOnClickListener(position,
            new ApplicationItemOnClickListener.OnItemClickCallback() {
      @Override
      public void onItemClicked(View view, int position) {
        Intent intent = new Intent();
        intent.setClassName("id.kita.pesan.secure",
            "org.thoughtcrime.securesms.applications." + getList().get(position).getApplicationClass());
        context.startActivity(intent);
      }
    }));
  }

  @Override
  public int getItemCount() {
    return getList().size();
  }

  public class GridViewHolder extends RecyclerView.ViewHolder {
    LinearLayout  applicationContainer;
    TextView      applicationId;
    TextView      applicationClass;
    ImageView     applicationIcon;
    TextView      applicationName;

    public GridViewHolder(View itemView) {
      super(itemView);
      applicationContainer  = (LinearLayout) itemView.findViewById(R.id.application_container);
      applicationId         = (TextView) itemView.findViewById(R.id.application_id);
      applicationClass      = (TextView) itemView.findViewById(R.id.application_class);
      applicationIcon       = (ImageView) itemView.findViewById(R.id.application_icon);
      applicationName       = (TextView) itemView.findViewById(R.id.application_name);
    }
  }
}