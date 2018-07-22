package org.thoughtcrime.securesms;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.media.GridAdapter;
import org.thoughtcrime.securesms.media.ListAdapter;
import org.thoughtcrime.securesms.media.Media;
import org.thoughtcrime.securesms.media.MediaData;
import org.thoughtcrime.securesms.util.DynamicLanguage;
import org.thoughtcrime.securesms.util.DynamicTheme;
import org.thoughtcrime.securesms.util.ViewUtil;

import java.util.ArrayList;

public class AllMediaActivity extends PassphraseRequiredActionBarActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String TAG = AllMediaActivity.class.getSimpleName();
  public final static String REFRESHABLE      = "refreshable";
  public static final String TYPE_EXTRA       = "type";
  public static final String TYPE_IMAGE_EXTRA = "image";
  public static final String TYPE_VIDEO_EXTRA = "video";
  public static final String TYPE_AUDIO_EXTRA = "audio";
  public static final String TYPE_FILE_EXTRA  = "file";

  private MasterSecret        masterSecret;
  private Context             context;
  private SwipeRefreshLayout  swipeRefresh;
  private TextView            title;
  private TextView            counter;
  private RecyclerView        recyclerView;
  private GridLayoutManager   gridLayoutManager;
  private LinearLayoutManager linearLayoutManager;
  private String              type;

  private final DynamicTheme dynamicTheme       = new DynamicTheme   ();
  private final DynamicLanguage dynamicLanguage = new DynamicLanguage();

  @Override
  protected void onPreCreate() {
    dynamicTheme.onCreate(this);
    dynamicLanguage.onCreate(this);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState, @NonNull MasterSecret masterSecret) {
    super.onCreate(savedInstanceState, masterSecret);
    setContentView(R.layout.activity_all_media);

    this.context = getBaseContext();
    this.masterSecret = masterSecret;

    swipeRefresh  = ViewUtil.findById(this, R.id.swipe_refresh);
    title         = ViewUtil.findById(this, R.id.title_view_all);
    counter       = ViewUtil.findById(this, R.id.count_all);
    recyclerView  = ViewUtil.findById(this, R.id.recycler_view_all);
    recyclerView.setNestedScrollingEnabled(false);

    type = getIntent().getStringExtra(TYPE_EXTRA);

    if (type.equals(TYPE_IMAGE_EXTRA))
      title.setText(R.string.tab_media_fragment__image);
    else if (type.equals(TYPE_VIDEO_EXTRA))
      title.setText(R.string.tab_media_fragment__video);
    else if (type.equals(TYPE_AUDIO_EXTRA))
      title.setText(R.string.tab_media_fragment__audio);
    else
      title.setText(R.string.tab_media_fragment__file);

    new LoadData().execute();

    swipeRefresh.setEnabled(this.getIntent().getBooleanExtra(REFRESHABLE, true) &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
    swipeRefresh.setOnRefreshListener(
        new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            new LoadData().execute();
          }
        }
    );
  }

  private void loadGridLayout(ArrayList data) {
    final ArrayList<Media> list;

    gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.media_overview_cols));
    recyclerView.setLayoutManager(gridLayoutManager);
    recyclerView.setHasFixedSize(true);

    list = data;
    GridAdapter adapter = new GridAdapter(this);
    adapter.setListData(list);

    recyclerView.setAdapter(adapter);
    adapter.notifyDataSetChanged();

    adapter.setOnItemClickListener(new GridAdapter.ClickListener() {
      @Override
      public void onItemClick(int position, View v) {
        MediaFragment.performClick(context, list, position);
      }
    });
  }

  private void loadLinearLayout(ArrayList data) {
    final ArrayList<Media> list;

    linearLayoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);

    list = data;
    ListAdapter adapter =  new ListAdapter(this);
    adapter.setListData(list);

    recyclerView.setAdapter(adapter);
    adapter.notifyDataSetChanged();

    adapter.setOnItemClickListener(new ListAdapter.ClickListener() {
      @Override
      public void onItemClick(int position, View v) {
        MediaFragment.performClick(context, list, position);
      }
    });
  }

  public Media getItem(ArrayList<Media> list, int position){
    return list.get(position);
  }

  class LoadData extends AsyncTask<Void, Void, ArrayList> {
    @Override
    protected ArrayList doInBackground(Void... voids) {
      MediaData data = new MediaData(getBaseContext(), masterSecret);

      switch (type) {
        case TYPE_IMAGE_EXTRA :
          return data.getListImage();

        case TYPE_VIDEO_EXTRA :
          return data.getListVideo();

        case TYPE_AUDIO_EXTRA :
          return data.getListAudio();

        default:
          return data.getListFile();
      }
    }

    @Override
    protected void onPostExecute(ArrayList media) {
      if (type.equals(TYPE_IMAGE_EXTRA) || type.equals(TYPE_VIDEO_EXTRA)) {
        loadGridLayout(media);
      } else {
        loadLinearLayout(media);
      }

      counter.setText(String.valueOf(media.size()));
      swipeRefresh.setRefreshing(false);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      swipeRefresh.setRefreshing(true);
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return null;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {

  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    new LoadData().execute();
    if (gridLayoutManager != null) gridLayoutManager.setSpanCount(getResources().getInteger(R.integer.media_overview_cols));
  }
}