package org.thoughtcrime.securesms;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.thoughtcrime.securesms.applications.Application;
import org.thoughtcrime.securesms.applications.ApplicationAdapter;
import org.thoughtcrime.securesms.applications.ApplicationData;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.util.DynamicLanguage;
import org.thoughtcrime.securesms.util.DynamicTheme;
import org.thoughtcrime.securesms.util.ViewUtil;

import java.util.ArrayList;

public class ApplicationsActivity extends PassphraseRequiredActionBarActivity {
  private ArrayList<Application>  list;
  private RecyclerView            recyclerView;

  private final DynamicTheme dynamicTheme     = new DynamicTheme   ();
  private final DynamicLanguage dynamicLanguage = new DynamicLanguage();

  @Override
  protected void onPreCreate() {
    dynamicTheme.onCreate(this);
    dynamicLanguage.onCreate(this);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState, @NonNull MasterSecret masterSecret) {
    super.onCreate(savedInstanceState, masterSecret);
    setContentView(R.layout.activity_applications);
    getSupportActionBar().setTitle(R.string.AndroidManifest__applications);

    initializeResources();
  }

  private void initializeResources() {
    recyclerView  = ViewUtil.findById(this, R.id.application_recyler_view);
    list          = new ArrayList<>();
    list.addAll(ApplicationData.get());

    recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    ApplicationAdapter applicationAdapter = new ApplicationAdapter(this);
    applicationAdapter.setList(list);
    recyclerView.setAdapter(applicationAdapter);
  }
}