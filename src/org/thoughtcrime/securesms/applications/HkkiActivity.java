package org.thoughtcrime.securesms.applications;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.util.DynamicLanguage;
import org.thoughtcrime.securesms.util.DynamicTheme;

public class HkkiActivity extends PassphraseRequiredActionBarActivity {
  private Context context;
  private MasterSecret masterSecret;

  private final DynamicTheme dynamicTheme       = new DynamicTheme   ();
  private final DynamicLanguage dynamicLanguage = new DynamicLanguage();

  @Override
  protected void onPreCreate() {
    dynamicTheme.onCreate(this);
    dynamicLanguage.onCreate(this);
  }

  protected void onCreate(Bundle savedInstanceState, @NonNull MasterSecret masterSecret) {
    super.onCreate(savedInstanceState, masterSecret);
    this.masterSecret = masterSecret;
    this.context      = getBaseContext();

    setContentView(R.layout.activity_hkki);
    getSupportActionBar().setTitle(R.string.application_hkki);
  }
}