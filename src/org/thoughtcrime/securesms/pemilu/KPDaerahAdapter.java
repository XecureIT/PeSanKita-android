package org.thoughtcrime.securesms.pemilu;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.pemilu.model.KPDaerah;

import java.util.List;

public class KPDaerahAdapter extends BaseAdapter {

    List<KPDaerah.ItemDaerah> items;
    Context context;
    LayoutInflater inflater;

    public KPDaerahAdapter(List<KPDaerah.ItemDaerah> items, Context context) {
        this.items = items;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.kp_item_daerah, null);
        }

        CheckedTextView name = convertView.findViewById(R.id.name);
        name.setText(items.get(position).getNama());

        return convertView;
    }
}
