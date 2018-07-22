package org.thoughtcrime.securesms.applications;

import android.view.View;

/**
 * Created by winardiaris on 15/02/18.
 */

public class ApplicationItemOnClickListener implements View.OnClickListener {
  private int position;
  private OnItemClickCallback onItemClickCallback;
  public ApplicationItemOnClickListener(int position, OnItemClickCallback onItemClickCallback) {
    this.position = position;
    this.onItemClickCallback = onItemClickCallback;
  }

  @Override
  public void onClick(View v) {
    onItemClickCallback.onItemClicked(v, position);
  }
  public interface OnItemClickCallback {
    void onItemClicked(View view, int position);
  }
}