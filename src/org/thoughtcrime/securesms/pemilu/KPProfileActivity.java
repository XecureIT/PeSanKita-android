package org.thoughtcrime.securesms.pemilu;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.TextSecurePreferences;


public class KPProfileActivity extends AppCompatActivity {
  private Context context;
  private View parent_view;
  private TextView mMsisdnView;
  private TextView mFullnameView;
  private EditText mPasswordView;
  private ImageButton mCopyPasswordView;
  private ClipboardManager myClipboard;
  private ClipData myClip;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.context = getBaseContext();
    setContentView(R.layout.kp_profile_activity);
    initComponent();
  }

  @Override
  protected void onResume() {
    super.onResume();
    getSupportActionBar().hide();
  }

  private void initComponent() {
    parent_view = findViewById(android.R.id.content);
    mMsisdnView = findViewById(R.id.msisdn);
    mFullnameView = findViewById(R.id.fullname);
    mPasswordView = findViewById(R.id.password);
    mCopyPasswordView = findViewById(R.id.copy);
    myClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);

    mFullnameView.setText(TextSecurePreferences.kpGetName(context));
    mMsisdnView.setText(TextSecurePreferences.getLocalNumber(context).replace("+", ""));
    mPasswordView.setText(TextSecurePreferences.kpGetPassword(context));

    mCopyPasswordView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        myClip = ClipData.newPlainText("text", mPasswordView.getText().toString());
        myClipboard.setPrimaryClip(myClip);

        Snackbar.make(parent_view, "Password copied", Snackbar.LENGTH_SHORT).show();
      }
    });

  }
}
