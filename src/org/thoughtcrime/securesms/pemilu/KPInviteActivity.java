package org.thoughtcrime.securesms.pemilu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import org.thoughtcrime.securesms.ContactSelectionListFragment;
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.ViewUtil;

public class KPInviteActivity extends PassphraseRequiredActionBarActivity {

  private Context context;
  private MasterSecret masterSecret;
  private TextView inviteText;

  @Override
  protected void onCreate(Bundle savedInstanceState, @NonNull MasterSecret masterSecret) {
    this.masterSecret = masterSecret;
    this.context = getBaseContext();

    getIntent().putExtra(ContactSelectionListFragment.DISPLAY_MODE, ContactSelectionListFragment.DISPLAY_MODE_SMS_ONLY);
    getIntent().putExtra(ContactSelectionListFragment.MULTI_SELECT, true);
    getIntent().putExtra(ContactSelectionListFragment.REFRESHABLE, false);

    super.onCreate(savedInstanceState, masterSecret);
    setContentView(R.layout.kp_invite_activity);
    getSupportActionBar().setTitle(R.string.AndroidManifest__invite_friends);

    initializeResources();
  }

  @Override
  protected void onResume() {
    super.onResume();
    getSupportActionBar().hide();
  }

  private void initializeResources() {
    View shareButton = ViewUtil.findById(this, R.id.share_button);
    inviteText = ViewUtil.findById(this, R.id.invite_text);
    inviteText.setText(inviteText.getText().toString().replace("#REFERRAL#", TextSecurePreferences.kpGetMsisdn(context)));
    shareButton.setOnClickListener(new ShareClickListener());
    KPHelper.darkenStatusBar(this, R.color.kawalpilpres_primary);

    if (KPHelper.isFirstTime(context)) {
      TextSecurePreferences.kpSetFirstTime(context, false);
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }

  private class ShareClickListener implements OnClickListener {
    @Override
    public void onClick(View v) {
      String text = inviteText.getText().toString();
      Intent sendIntent = new Intent();
      sendIntent.setAction(Intent.ACTION_SEND);
      sendIntent.putExtra(Intent.EXTRA_TEXT, text);
      sendIntent.setType("text/plain");
      if (sendIntent.resolveActivity(getPackageManager()) != null) {
        startActivity(Intent.createChooser(sendIntent, getString(R.string.InviteActivity_invite_to_signal)));
      } else {
        Toast.makeText(KPInviteActivity.this, R.string.InviteActivity_no_app_to_share_to, Toast.LENGTH_LONG).show();
      }
    }
  }
}
