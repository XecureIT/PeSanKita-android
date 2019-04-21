package org.thoughtcrime.securesms.pemilu;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.pemilu.database.KPDaerahDatabase;
import org.thoughtcrime.securesms.pemilu.model.KPLaporan;

import java.util.List;

public class KPLaporanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static OnItemClickListener itemClickListener;
    private final int VIEW_ITEM = 1;
    private final int VIEW_SECTION = 0;
    private List<KPLaporan> items;
    private Context ctx;
    private KPDaerahDatabase daerahDatabase;

    KPLaporanAdapter(Context context, List<KPLaporan> items) {
        this.items = items;
        this.ctx = context;
        this.daerahDatabase = KPDatabaseHelper.getDaerahDatabase(context);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;

        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.kp_item_laporan, parent, false);
            vh = new ItemHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.kp_item_laporan_section, parent, false);
            vh = new SectionHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        KPLaporan p = items.get(position);
        if (holder instanceof ItemHolder) {
            ItemHolder view = (ItemHolder) holder;

            if (p.getType() == KPLaporanBaruActivity.RETRY) {
                view.status.setImageResource(R.drawable.ic_error_white_18dp);
                view.status.setColorFilter(ContextCompat.getColor(ctx, R.color.kawalpilpres_primary), android.graphics.PorterDuff.Mode.MULTIPLY);
            } else {
                view.status.setImageResource(R.drawable.ic_check_circle_white_18dp);
                view.status.setColorFilter(ContextCompat.getColor(ctx, R.color.blue), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
            view.tps.setText(p.getTps());
            view.count1.setText(String.valueOf(p.getCount1()));
            view.count2.setText(String.valueOf(p.getCount2()));
            view.s1.setText(String.valueOf(p.getS1()));
            view.n1.setText(String.valueOf(p.getN1()));
            view.parent.setOnClickListener(view1 -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(view1, items.get(position), position);
                }
            });
        } else {
            SectionHolder view = (SectionHolder) holder;
            view.daerah.setText(daerahDatabase.getSectionString(p.getKelurahan()));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return this.items.get(position).getType() == 99 ? VIEW_SECTION : VIEW_ITEM;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        KPLaporanAdapter.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, KPLaporan obj, int position);
    }

    public void setItems(List<KPLaporan> items) {
        this.items = items;
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        public ImageView status;
        public TextView tps, count1, count2, s1, n1;
        public View parent;

        public ItemHolder(View v) {
            super(v);
            status = v.findViewById(R.id.status);
            tps = v.findViewById(R.id.tps);
            count1 = v.findViewById(R.id.count1);
            count2 = v.findViewById(R.id.count2);
            s1 = v.findViewById(R.id.s1);
            n1 = v.findViewById(R.id.n1);
            parent = v.findViewById(R.id.itemLayout);
        }
    }

    public class SectionHolder extends RecyclerView.ViewHolder {
        public TextView daerah;

        public SectionHolder(View v) {
            super(v);
            daerah = v.findViewById(R.id.daerah);
        }
    }
}
