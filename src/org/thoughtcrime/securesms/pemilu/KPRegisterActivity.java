package org.thoughtcrime.securesms.pemilu;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.Address;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.EncryptingSmsDatabase;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.pemilu.model.KPRegistrationBody;
import org.thoughtcrime.securesms.pemilu.model.KPStatus;
import org.thoughtcrime.securesms.pemilu.service.KPRestClient;
import org.thoughtcrime.securesms.pemilu.service.KPRestInterface;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.sms.MessageSender;
import org.thoughtcrime.securesms.sms.OutgoingEncryptedMessage;
import org.thoughtcrime.securesms.sms.OutgoingTextMessage;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Response;

import static org.thoughtcrime.securesms.util.TextSecurePreferences.getLocalNumber;

/**
 * Created by rendra on 15/03/19.
 */

public class KPRegisterActivity extends PassphraseRequiredActionBarActivity {
    private Context context;
    private MasterSecret masterSecret;
    private UserRegisterTask mRegisterTask = null;
    private View parent_view;

    // UI references
    private TextView mMsisdnView;
    private TextView mFullnameView;
    private TextView mReferralView;
    private TextView mPasswordView;
    private ImageButton mGenPasswordView;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState, @NonNull MasterSecret masterSecret) {
        super.onCreate(savedInstanceState, masterSecret);
        setContentView(R.layout.kp_register_activity);

        this.context = getBaseContext();
        this.masterSecret = masterSecret;

        initComponent();
    }

    private void initComponent() {
        parent_view = findViewById(android.R.id.content);

        // Set up the register form.
        mMsisdnView = (TextView) findViewById(R.id.msisdn);
        mFullnameView = (TextView) findViewById(R.id.fullname);
        mReferralView = (TextView) findViewById(R.id.referral);
        mPasswordView = (TextView) findViewById(R.id.password);
        mGenPasswordView = (ImageButton) findViewById(R.id.generate_password);

        mMsisdnView.setText(TextSecurePreferences.getLocalNumber(KPRegisterActivity.this).replace("+", ""));
        mFullnameView.setText(TextSecurePreferences.getProfileName(this));

        genPassword();

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mGenPasswordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genPassword();
            }
        });

        mProgressView = findViewById(R.id.register_progress);
    }

    private void attemptRegister() {
        if (mRegisterTask != null) {
            return;
        }

        // Reset errors.
        mMsisdnView.setError(null);
        mFullnameView.setError(null);
        mPasswordView.setError(null);
        mReferralView.setError(null);

        // Store values at the time of the register attempt.
        String msisdn = mMsisdnView.getText().toString().trim();
        String fullname = mFullnameView.getText().toString().trim();
        String referral = mReferralView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid msisdn.
        if (TextUtils.isEmpty(msisdn)) {
            mMsisdnView.setError("Field required");
            focusView = mMsisdnView;
            cancel = true;
        } else if (!isMsisdnValid(msisdn)) {
            mMsisdnView.setError("Min 10 digit, must not start with 0");
            focusView = mMsisdnView;
            cancel = true;
        }

        // Check for a valid fullname.
        if (TextUtils.isEmpty(fullname)) {
            mFullnameView.setError("Field required");
            if (!cancel) {
                focusView = mFullnameView;
                cancel = true;
            }
        }

        // Check for a valid referral.
        if (!TextUtils.isEmpty(referral) && !isMsisdnValid(referral)) {
            mReferralView.setError("Min 10 digit, must not start with 0");
            if (!cancel) {
                focusView = mReferralView;
                cancel = true;
            }
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("Field required");
            if (!cancel) {
                focusView = mPasswordView;
                cancel = true;
            }
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError("Min 8 character");
            if (!cancel) {
                focusView = mPasswordView;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to perform the user login attempt.
            showProgress(true);
            mRegisterTask = new UserRegisterTask(msisdn, fullname, referral, password);
            mRegisterTask.execute((Void) null);
        }
    }

    private boolean isMsisdnValid(String msisdn) {
        return TextUtils.isDigitsOnly(msisdn) && msisdn.length() >= 10 && !msisdn.startsWith("0");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8;
    }

    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void genPassword() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String format = simpleDateFormat.format(new Date());
        String password = KPHelper.generatePassword(format, 10);
        mPasswordView.setText(password);
        Log.d("ARs", "genPassword: genPassword: " + password);
    }

    private class UserRegisterTask extends AsyncTask<Void, Void, Object> {

        private final String mMsisdn;
        private final String mFullname;
        private final String mReferral;
        private final String mPassword;

        UserRegisterTask(String msisdn, String fullname, String referral, String password) {
            mMsisdn = msisdn;
            mFullname = fullname;
            mReferral = referral;
            mPassword = password;
        }

        @Override
        protected Object doInBackground(Void... params) {
            final KPRestInterface rest = KPRestClient.getClient(KPRegisterActivity.this).create(KPRestInterface.class);

            Call<KPStatus> call = rest.userRegistration(new KPRegistrationBody(mMsisdn, mFullname, null, null, mReferral, mPassword));
            try {
                Response<KPStatus> response = call.execute();
                if (response.code() == 200) {
                    return response.body();
                } else if (response.code() == 400) {
                    if (response.errorBody() != null) {
                        Gson gson = new Gson();
                        TypeAdapter<KPStatus> adapter = gson.getAdapter(KPStatus.class);
                        try {
                            return adapter.fromJson(response.errorBody().string());
                        } catch (IOException e) {
                            return e;
                        }
                    }
                }
            } catch (IOException e) {
                return e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Object obj) {
            mRegisterTask = null;
            showProgress(false);

            if (obj != null) {
                if (obj instanceof KPStatus) {
                    KPStatus auth = (KPStatus) obj;

                    if(auth.getStatus().equals("OK")) {
                        TextSecurePreferences.kpSetMsisdn(context, mMsisdn);
                        TextSecurePreferences.kpSetName(context, mFullname);
                        TextSecurePreferences.kpSetReferral(context, mReferral);
                        TextSecurePreferences.kpSetPassword(context, mPassword);
                        TextSecurePreferences.kpSetFirstTime(context, true);
                        TextSecurePreferences.kpSetLoggedIn(context, true);

                        sendPersonalNote("Selamat bergabung di Gerakan Relawan #AyoNyoblos #AyoPantau yang #NetralBerintegritasTerbuka." +
                                "\nData registrasi Anda adalah:" +
                                "\nNo.HP: " + mMsisdn +
                                "\nNama Lengkap: " + mFullname +
                                "\nKode Referensi: " + mReferral +
                                "\nPassword: " + mPassword);

                        Intent intent = new Intent(context, KPDashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else if(auth.getStatus().equals("ERROR")) {
                        String message = auth.getMessage();
                        if (message.toLowerCase().contains("no. hp")) {
                            mMsisdnView.setError(message);
                            mMsisdnView.requestFocus();
                        } else if (message.toLowerCase().contains("nama")) {
                            mFullnameView.setError(message);
                            mFullnameView.requestFocus();
                        } else if (message.toLowerCase().contains("referensi")) {
                            mReferralView.setError(message);
                            mReferralView.requestFocus();
                        } else if (message.toLowerCase().contains("password")) {
                            mPasswordView.setError(message);
                            mPasswordView.requestFocus();
                        }
                        Snackbar.make(parent_view, message, Snackbar.LENGTH_SHORT).show();
                    }
                } else if (obj instanceof IOException) {
                    Snackbar.make(parent_view, "Connectivity error", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(parent_view, "Unknown error", Snackbar.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mRegisterTask = null;
            showProgress(false);
        }
    }

    private void sendPersonalNote(String msg) {
        Address address             = Address.fromExternal(context, getLocalNumber(context));
        Recipient recipient         = Recipient.from(context, address, true);
        long expiresIn              = recipient.getExpireMessages() * 1000;
        ThreadDatabase threads      = DatabaseFactory.getThreadDatabase(context);
        long threadId               = threads.getThreadIdFor(recipient);
        OutgoingTextMessage message = new OutgoingEncryptedMessage(recipient, msg, expiresIn);

        new AsyncTask<OutgoingTextMessage, Void, Long>() {
            @Override
            protected Long doInBackground(OutgoingTextMessage... messages) {
                return MessageSender.send(context, masterSecret, messages[0], threadId, false, null);
            }

            @Override
            protected void onPostExecute(Long result) {
                Log.d("sendPersonalNote", "onPostExecute: msg: " + msg);
                EncryptingSmsDatabase database = DatabaseFactory.getEncryptingSmsDatabase(context);
                database.markAsSent(result, true);
                return;
            }
        }.execute(message);
    }
}
