package org.thoughtcrime.securesms;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.mms.PartAuthority;
import org.thoughtcrime.securesms.media.ListAdapter;
import org.thoughtcrime.securesms.media.Media;
import org.thoughtcrime.securesms.media.MediaData;
import org.thoughtcrime.securesms.media.ThumbnailAdapter;
import org.thoughtcrime.securesms.util.ViewUtil;

import java.util.ArrayList;

public class MediaFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String TAG = MediaFragment.class.getSimpleName();
  public final static String REFRESHABLE  = "refreshable";

  protected View              view;
  private Context             context;
  private MasterSecret        masterSecret;
  private SwipeRefreshLayout  swipeRefresh;
  private LinearLayout        noMedia;
  private LinearLayout        parent;
  private TextView            countAudio;
  private TextView            countFile;
  private RelativeLayout      showMoreAudio;
  private RelativeLayout      showMoreFile;
  private RecyclerView        recyclerView;
  private GridLayoutManager   thumbnailLayoutManager;
  private LinearLayoutManager linearLayoutManager;

  public MediaFragment() {}

  public void onActivityCreated(Bundle bundle) {
    super.onActivityCreated(bundle);
    super.onCreate(bundle);
    this.context = getContext();
    this.masterSecret = getArguments().getParcelable("master_secret");
    new LoadData().execute();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    view          = inflater.inflate(R.layout.fragment_media, container, false);
    swipeRefresh  = ViewUtil.findById(view, R.id.swipe_refresh);
    noMedia       = ViewUtil.findById(view, R.id.no_media);
    showMoreAudio = ViewUtil.findById(view, R.id.show_more_audio);
    showMoreFile  = ViewUtil.findById(view, R.id.show_more_file);
    countAudio    = ViewUtil.findById(view, R.id.count_audio);
    countFile     = ViewUtil.findById(view, R.id.count_file);

    swipeRefresh.setEnabled(getActivity().getIntent().getBooleanExtra(REFRESHABLE, true) &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);

    swipeRefresh.setOnRefreshListener(
        new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            new LoadData().execute();
          }
        }
    );

    showMoreAudio.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        viewAll(AllMediaActivity.TYPE_AUDIO_EXTRA);
      }
    });

    showMoreFile.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        viewAll(AllMediaActivity.TYPE_FILE_EXTRA);
      }
    });

    return view;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return null;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {}

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {

  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  public static Media getItem(ArrayList<Media> list, int position){
    return list.get(position);
  }

  class LoadData extends  AsyncTask<Void, Void, MediaData> {
    @Override
    protected MediaData doInBackground(Void... voids) {
      MediaData data = new MediaData(getContext(), masterSecret);
      return data;
    }

    @Override
    protected void onPostExecute(MediaData data) {
      loadGridLayoutThumbnail(data.getListThumbnail());
      loadLinearLayout(data.getListAudio(4), R.id.layout_audio, R.id.recycler_view_audio);
      loadLinearLayout(data.getListFile(4), R.id.layout_file, R.id.recycler_view_file);

      noMedia.setVisibility(data.getListData().size() > 0 ? View.GONE : View.VISIBLE);
      showMoreAudio.setVisibility(data.getListAudio().size() > 4 ? View.VISIBLE : View.GONE);
      showMoreFile.setVisibility(data.getListFile().size() > 4 ? View.VISIBLE : View.GONE);
      countAudio.setText(String.valueOf(data.getListAudio().size()));
      countFile.setText(String.valueOf(data.getListFile().size()));
      swipeRefresh.setRefreshing(false);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      swipeRefresh.setRefreshing(true);
    }
  }

  private void loadGridLayoutThumbnail(ArrayList data) {
    final ArrayList<Media> list;
    list = data;

    if (getCountThumbnail(data) == 0) {
      parentHide(R.id.layout_thumbnail);
    } else {
      recyclerView = ViewUtil.findById(view, R.id.recycler_view_thumbnail);
      recyclerView.setNestedScrollingEnabled(false);

      thumbnailLayoutManager = new GridLayoutManager(getActivity(), 2);
      recyclerView.setLayoutManager(thumbnailLayoutManager);
      recyclerView.setHasFixedSize(true);

      ThumbnailAdapter adapter = new ThumbnailAdapter(getContext(), masterSecret);
      adapter.setListData(list);

      recyclerView.setAdapter(adapter);
      adapter.notifyDataSetChanged();

      adapter.setOnItemClickListener(new ThumbnailAdapter.ClickListener() {
        @Override
        public void onItemClick(int position, View v) {
          Intent intent = new Intent(getContext(), MediaOverviewActivity.class);
          intent.putExtra(MediaOverviewActivity.TYPE_ID_EXTRA, position);
          startActivity(intent);
        }
      });
    }
  }

  private int getCountThumbnail(ArrayList data){
    ArrayList i = (ArrayList) data.get(0);
    ArrayList j = (ArrayList) data.get(1);

    return i.size() + j.size();
  }

  private void loadLinearLayout(ArrayList data, int parentResId, int recyclerResId) {
    final ArrayList<Media> list;

    if (data.size() == 0) {
      parentHide(parentResId);
    } else {
      recyclerView = ViewUtil.findById(view, recyclerResId);
      recyclerView.setNestedScrollingEnabled(false);

      linearLayoutManager = new LinearLayoutManager(getActivity());
      recyclerView.setLayoutManager(linearLayoutManager);
      recyclerView.setHasFixedSize(true);

      list = data;
      ListAdapter adapter =  new ListAdapter(getContext());
      adapter.setListData(list);

      recyclerView.setAdapter(adapter);
      adapter.notifyDataSetChanged();

      adapter.setOnItemClickListener(new ListAdapter.ClickListener() {
        @Override
        public void onItemClick(int position, View v) {
          performClick(context, list, position);
        }
      });
    }
  }

  private void parentHide(int parentResId) {
    parent = ViewUtil.findById(view, parentResId);
    parent.setVisibility(View.GONE);
  }

  public static void performClick(Context context, ArrayList<Media> list, int position) {
    Media item = getItem(list, position);
    Uri uri = item.getUri();

    if (MediaPreviewActivity.isContentTypeSupported(item.getContentType()) && item.getUri() != null) {
      Intent intent = new Intent(context, MediaPreviewActivity.class);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      intent.setDataAndType(uri, item.getContentType());

      intent.putExtra(MediaPreviewActivity.DATE_EXTRA, item.getDate());
      intent.putExtra(MediaPreviewActivity.SIZE_EXTRA, item.getSize());
      intent.putExtra(MediaPreviewActivity.THREAD_ID_EXTRA,0);

      context.startActivity(intent);
    } else {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setDataAndType(PartAuthority.getAttachmentPublicUri(uri), item.getContentType());

      try {
        context.startActivity(intent);
      } catch (ActivityNotFoundException anfe) {
        Log.w(TAG, "No activity existed to view the media.");
        Toast.makeText(context, R.string.ConversationItem_unable_to_open_media, Toast.LENGTH_LONG).show();
      } catch (NullPointerException e) {
        e.printStackTrace();
        Toast.makeText(context, R.string.ConversationItem_unable_to_open_media, Toast.LENGTH_LONG).show();
      }
    }
  }

  private void viewAll(String type) {
    Intent intent = new Intent(context, AllMediaActivity.class);
    intent.putExtra(AllMediaActivity.TYPE_EXTRA, type);

    context.startActivity(intent);
  }
}