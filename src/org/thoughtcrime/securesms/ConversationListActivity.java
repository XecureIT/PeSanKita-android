/**
 * Copyright (C) 2014 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.Address;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.notifications.MessageNotifier;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.service.KeyCachingService;
import org.thoughtcrime.securesms.util.DirectoryHelper;
import org.thoughtcrime.securesms.util.DynamicLanguage;
import org.thoughtcrime.securesms.util.DynamicTheme;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.ViewUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class ConversationListActivity extends PassphraseRequiredActionBarActivity implements
        ConversationListFragment.ConversationSelectedListener,
        SwipeRefreshLayout.OnRefreshListener,
        ContactSelectionListFragment.OnContactSelectedListener,
        ViewPager.OnPageChangeListener
{

  private static final String TAG = ConversationListActivity.class.getSimpleName();

  private final DynamicTheme    dynamicTheme    = new DynamicTheme   ();
  private final DynamicLanguage dynamicLanguage = new DynamicLanguage();

  private ConversationListFragment chatsFragment;
  private ContactSelectionListFragment contactsFragment;
  private MediaFragment mediaFragment;

  private ContentObserver observer;
  private MasterSecret masterSecret;

  private ViewPager viewPager;
  private TabLayout tabLayout;

  @Override
  protected void onPreCreate() {
    dynamicTheme.onCreate(this);
    dynamicLanguage.onCreate(this);
  }

  @Override
  protected void onCreate(Bundle icicle, @NonNull MasterSecret masterSecret) {
    setContentView(R.layout.conversation_list_activity);
    this.masterSecret = masterSecret;

    if (!getIntent().hasExtra(ContactSelectionListFragment.DISPLAY_MODE)) {
      getIntent().putExtra(ContactSelectionListFragment.DISPLAY_MODE,
              ContactSelectionListFragment.DISPLAY_MODE_PUSH_ONLY);
    }

    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
    getSupportActionBar().setTitle(R.string.app_name);
    getSupportActionBar().setElevation(0);

    viewPager = ViewUtil.findById(this, R.id.view_pager);

    tabLayout = ViewUtil.findById(this, R.id.tab_layout);
    tabLayout.setupWithViewPager(viewPager);

    viewPager.addOnPageChangeListener(this);
    setupViewPager(viewPager);

    initializeContactUpdatesReceiver();
  }

  @Override
  public void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
    dynamicLanguage.onResume(this);
  }

  @Override
  public void onDestroy() {
    if (observer != null) getContentResolver().unregisterContentObserver(observer);
    super.onDestroy();
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuInflater inflater = this.getMenuInflater();
    menu.clear();

    inflater.inflate(R.menu.conversation_list_main, menu);

    menu.findItem(R.id.menu_clear_passphrase).setVisible(!TextSecurePreferences.isPasswordDisabled(this));
    menu.findItem(R.id.menu_mark_all_read).setVisible(viewPager.getCurrentItem() == 0);
    menu.findItem(R.id.menu_refresh).setVisible(viewPager.getCurrentItem() == 1);

    inflater.inflate(R.menu.conversation_list, menu);
    MenuItem menuItem = menu.findItem(R.id.menu_search);
    initializeSearch(menuItem);

    super.onPrepareOptionsMenu(menu);
    return true;
  }

  private void setupViewPager(ViewPager viewPager)
  {
    ConversationListFragmentAdapter adapter = new ConversationListFragmentAdapter(getSupportFragmentManager());

    chatsFragment = initFragment(new ConversationListFragment(), masterSecret, dynamicLanguage.getCurrentLocale(), null);
    contactsFragment = initFragment(new ContactSelectionListFragment(), masterSecret, dynamicLanguage.getCurrentLocale(), null);
    mediaFragment = initFragment(new MediaFragment(), masterSecret, dynamicLanguage.getCurrentLocale(), null);
    contactsFragment.setOnContactSelectedListener(this);
    contactsFragment.setOnRefreshListener(this);

    adapter.addFragment(chatsFragment, getString(R.string.ConversationListActivity_chats));
    adapter.addFragment(contactsFragment, getString(R.string.ConversationListActivity_contacts));
    adapter.addFragment(mediaFragment, getString(R.string.ConversationListActivity_media));
    viewPager.setAdapter(adapter);
  }

  private void initializeSearch(MenuItem searchViewItem) {
    SearchView searchView = (SearchView)MenuItemCompat.getActionView(searchViewItem);
    searchView.setQueryHint(getString(R.string.ConversationListActivity_search));
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        if (viewPager.getCurrentItem() == 0) {
          chatsFragment.setQueryFilter(query);
          return true;
        }

        if (viewPager.getCurrentItem() == 1) {
          contactsFragment.setQueryFilter(query);
          return true;
        }

        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        return onQueryTextSubmit(newText);
      }
    });

    if (viewPager.getCurrentItem() == 2) {
      searchViewItem.setEnabled(false);
      searchViewItem.setVisible(false);
    }

    MenuItemCompat.setOnActionExpandListener(searchViewItem, new MenuItemCompat.OnActionExpandListener() {
      @Override
      public boolean onMenuItemActionExpand(MenuItem menuItem) {
        return true;
      }

      @Override
      public boolean onMenuItemActionCollapse(MenuItem menuItem) {
        if (viewPager.getCurrentItem() == 0) {
          chatsFragment.resetQueryFilter();
        }

        if (viewPager.getCurrentItem() == 1) {
          contactsFragment.resetQueryFilter();
        }

        return true;
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);

    switch (item.getItemId()) {
      case R.id.menu_refresh:           handleManualRefresh();   return true;
      case R.id.menu_applications:      handleApplications();    return true;
      case R.id.menu_new_group:         createGroup();           return true;
      case R.id.menu_import_export:     handleImportExport();    return true;
      case R.id.menu_settings:          handleDisplaySettings(); return true;
      case R.id.menu_clear_passphrase:  handleClearPassphrase(); return true;
      case R.id.menu_mark_all_read:     handleMarkAllRead();     return true;
      case R.id.menu_invite:            handleInviteFriend();    return true;
    }

    return false;
  }

  @Override
  public void onCreateConversation(long threadId, Recipient recipient, int distributionType, long lastSeen) {
    Intent intent = new Intent(this, ConversationActivity.class);
    intent.putExtra(ConversationActivity.ADDRESS_EXTRA, recipient.getAddress());
    intent.putExtra(ConversationActivity.THREAD_ID_EXTRA, threadId);
    intent.putExtra(ConversationActivity.DISTRIBUTION_TYPE_EXTRA, distributionType);
    intent.putExtra(ConversationActivity.TIMING_EXTRA, System.currentTimeMillis());
    intent.putExtra(ConversationActivity.LAST_SEEN_EXTRA, lastSeen);

    startActivity(intent);
    overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
  }

  @Override
  public void onSwitchToArchive() {
    Intent intent = new Intent(this, ConversationListArchiveActivity.class);
    startActivity(intent);
  }

  private void createGroup() {
    Intent intent = new Intent(this, GroupCreateActivity.class);
    startActivity(intent);
  }

  private void handleDisplaySettings() {
    Intent preferencesIntent = new Intent(this, ApplicationPreferencesActivity.class);
    startActivity(preferencesIntent);
  }

  private void handleClearPassphrase() {
    Intent intent = new Intent(this, KeyCachingService.class);
    intent.setAction(KeyCachingService.CLEAR_KEY_ACTION);
    startService(intent);
  }

  private void handleImportExport() {
    startActivity(new Intent(this, ImportExportActivity.class));
  }

  private void handleMarkAllRead() {
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        DatabaseFactory.getThreadDatabase(ConversationListActivity.this).setAllThreadsRead();
        MessageNotifier.updateNotification(ConversationListActivity.this, masterSecret);
        return null;
      }
    }.execute();
  }

  private void handleManualRefresh() {
    contactsFragment.setRefreshing(true);
    onRefresh();
  }

  private void handleApplications() {
    Intent intent = new Intent(this, ApplicationsActivity.class);
    startActivity(intent);
  }

  private void handleInviteFriend(){
    Intent intent = new Intent(this, InviteActivity.class);
    startActivity(intent);
  }

  private void initializeContactUpdatesReceiver() {
    observer = new ContentObserver(null) {
      @Override
      public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.w(TAG, "Detected android contact data changed, refreshing cache");
        Recipient.clearCache(ConversationListActivity.this);
        ConversationListActivity.this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            chatsFragment.getListAdapter().notifyDataSetChanged();
          }
        });
      }
    };

    getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI,
                                                 true, observer);
  }

  @Override
  public void onRefresh() {
    new RefreshDirectoryTask(this).execute(getApplicationContext());
  }

  @Override
  public void onContactSelected(String number) {
    Recipient recipient = Recipient.from(this, Address.fromExternal(this, number), true);

    Intent intent = new Intent(this, ConversationActivity.class);
    intent.putExtra(ConversationActivity.ADDRESS_EXTRA, recipient.getAddress());
    intent.putExtra(ConversationActivity.TEXT_EXTRA, getIntent().getStringExtra(ConversationActivity.TEXT_EXTRA));
    intent.setDataAndType(getIntent().getData(), getIntent().getType());

    long existingThread = DatabaseFactory.getThreadDatabase(this).getThreadIdIfExistsFor(recipient);

    intent.putExtra(ConversationActivity.THREAD_ID_EXTRA, existingThread);
    intent.putExtra(ConversationActivity.DISTRIBUTION_TYPE_EXTRA, ThreadDatabase.DistributionTypes.DEFAULT);
    startActivity(intent);
  }

  @Override
  public void onContactDeselected(String number) {}

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

  @Override
  public void onPageSelected(int position) {
    viewPager.setCurrentItem(position,false);
    supportInvalidateOptionsMenu();
  }

  @Override
  public void onPageScrollStateChanged(int state) {}

  private static class RefreshDirectoryTask extends AsyncTask<Context, Void, Void> {

    private final WeakReference<ConversationListActivity> activity;
    private final MasterSecret masterSecret;

    private RefreshDirectoryTask(ConversationListActivity activity) {
      this.activity     = new WeakReference<>(activity);
      this.masterSecret = activity.masterSecret;
    }


    @Override
    protected Void doInBackground(Context... params) {

      try {
        DirectoryHelper.refreshDirectory(params[0], masterSecret);
      } catch (IOException e) {
        Log.w(TAG, e);
      }

      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      ConversationListActivity activity = this.activity.get();

      if (activity != null && !activity.isFinishing()) {
        activity.contactsFragment.resetQueryFilter();
      }
    }
  }
}