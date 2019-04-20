package org.thoughtcrime.securesms;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.CursorRecyclerViewAdapter;
import org.thoughtcrime.securesms.database.loaders.ThreadMediaLoader;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.util.DynamicLanguage;
import org.thoughtcrime.securesms.util.DynamicTheme;

/**
 * Activity for displaying media attachments in-app
 */
public class AllMediaOverviewActivity extends PassphraseRequiredActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {
  private final static String TAG = AllMediaOverviewActivity.class.getSimpleName();

  public static final String TYPE_ID_EXTRA = "type_id";

  private final DynamicTheme    dynamicTheme    = new DynamicTheme();
  private final DynamicLanguage dynamicLanguage = new DynamicLanguage();

  private MasterSecret masterSecret;

  public  int               typeId;
  private RecyclerView      gridView;
  private GridLayoutManager gridManager;
  private TextView          noImages;

  @Override
  protected void onPreCreate() {
    dynamicTheme.onCreate(this);
    dynamicLanguage.onCreate(this);
  }

  @Override
  protected void onCreate(Bundle bundle, @NonNull MasterSecret masterSecret) {
    this.masterSecret = masterSecret;

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    setContentView(R.layout.all_media_overview_activity);

    initializeResources();
    initializeActionBar();
    getSupportLoaderManager().initLoader(0, null, AllMediaOverviewActivity.this);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (gridManager != null) gridManager.setSpanCount(getResources().getInteger(R.integer.media_overview_cols));
  }

  @Override
  public void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
    dynamicLanguage.onResume(this);
  }

  private void initializeActionBar() {
    getSupportActionBar().setTitle(getString(R.string.AndroidManifest__all_media));
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  private void initializeResources() {
    typeId   = getIntent().getIntExtra(TYPE_ID_EXTRA, -1);

    noImages = (TextView    ) findViewById(R.id.no_images );
    gridView = (RecyclerView) findViewById(R.id.media_grid);
    gridManager = new GridLayoutManager(this, getResources().getInteger(R.integer.media_overview_cols));
    gridView.setLayoutManager(gridManager);
    gridView.setHasFixedSize(true);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    menu.clear();

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);

    switch (item.getItemId()) {
      case android.R.id.home: finish();     return true;
    }

    return false;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return new ThreadMediaLoader(this, masterSecret, null, true, typeId);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    Log.w(TAG, "onLoadFinished()");
    gridView.setAdapter(new AllMediaAdapter(this, masterSecret, GlideApp.with(this), cursor));
    noImages.setVisibility(gridView.getAdapter().getItemCount() > 0 ? View.GONE : View.VISIBLE);
    invalidateOptionsMenu();
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader) {
    ((CursorRecyclerViewAdapter)gridView.getAdapter()).changeCursor(null);
  }

}