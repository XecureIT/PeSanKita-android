package org.thoughtcrime.securesms.applications;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.thoughtcrime.securesms.BuildConfig;
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.Address;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.sms.MessageSender;
import org.thoughtcrime.securesms.sms.OutgoingEncryptedMessage;
import org.thoughtcrime.securesms.sms.OutgoingTextMessage;
import org.thoughtcrime.securesms.util.DynamicLanguage;
import org.thoughtcrime.securesms.util.DynamicTheme;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.ViewUtil;
import org.whispersystems.libsignal.InvalidMessageException;

public class KitsiaActivity extends PassphraseRequiredActionBarActivity {
  private Context context;
  private MasterSecret masterSecret;
  private EditText name;
  private EditText email;
  private EditText source;
  private Button invite;

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
    this.context = getBaseContext();
    this.masterSecret = masterSecret;

    setContentView(R.layout.activity_kitsia);
    getSupportActionBar().setTitle(R.string.application_kitsia);
    initializeResources();
  }

  public void initializeResources() {
    name          = ViewUtil.findById(this, R.id.kitsia_name);
    email         = ViewUtil.findById(this, R.id.kitsia_email);
    source         = ViewUtil.findById(this, R.id.kitsia_source);
    invite        = ViewUtil.findById(this, R.id.kitsia_invite);

    invite.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        submitRegister();
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  private void submitRegister() {
    if (TextUtils.isEmpty(name.getText())){
      Toast.makeText(context, R.string.application_kitsia_name__empty_message, Toast.LENGTH_SHORT).show();
      name.requestFocus();
    } else if (TextUtils.isEmpty(email.getText())) {
      Toast.makeText(context, R.string.application_kitsia_email__empty_message, Toast.LENGTH_SHORT).show();
      email.requestFocus();
    } else if (TextUtils.isEmpty(source.getText())) {
      Toast.makeText(context, R.string.application_kitsia_source__empty_message, Toast.LENGTH_SHORT).show();
      source.requestFocus();
    } else {
      String msg = name.getText() + "#" + email.getText() + "#" + getLocalNumber() + "#" + source.getText();
      try {
        sendTextMessage(msg);
      } catch (InvalidMessageException e) {
        Toast.makeText(context, R.string.application_kitsia_submit_error, Toast.LENGTH_SHORT).show();
      }
    }
  }

  private String getLocalNumber() {
    return TextSecurePreferences.getLocalNumber(context);
  }

  private void sendTextMessage(String msg) throws InvalidMessageException {
    Address address = Address.fromExternal(context, BuildConfig.REGISTRATION_NUMBER);

    Recipient recipient = Recipient.from(context, address, true);
    long expiresIn = recipient.getExpireMessages() * 1000;

    OutgoingTextMessage message = new OutgoingEncryptedMessage(recipient, msg, expiresIn);

    new AsyncTask<OutgoingTextMessage, Void, Long>() {
      @Override
      protected Long doInBackground(OutgoingTextMessage... messages) {
        return MessageSender.send(context, masterSecret, messages[0], 0, false, null);
      }

      @Override
      protected void onPostExecute(Long result) {
        finish();
        Toast.makeText(context, R.string.application_kitsia_submit_success, Toast.LENGTH_LONG).show();
      }
    }.execute(message);
  }
}
