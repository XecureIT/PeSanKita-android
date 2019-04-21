package org.thoughtcrime.securesms.pemilu;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.pemilu.model.KPLoginStatus;
import org.thoughtcrime.securesms.pemilu.service.KPAuthException;
import org.thoughtcrime.securesms.pemilu.service.KPRestClient;
import org.thoughtcrime.securesms.pemilu.service.KPRestInterface;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

import java.io.IOException;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by rendra on 15/03/19.
 */

public class KPLoginActivity extends AppCompatActivity {
    private UserLoginTask mLoginTask = null;

    private Context context;
    private View parent_view;

    // UI references
    private TextView mMsisdnView;
    private TextView mPasswordView;
    private View mProgressView;
    private Button mLoginView;
    private Button mRegisterView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getBaseContext();
        setContentView(R.layout.kp_login_activity);

        initComponent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().hide();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                mPasswordView.setText(TextSecurePreferences.kpGetPassword(this));
                attemptLogin();
            }
        }
    }

    private void initComponent() {
        parent_view = findViewById(android.R.id.content);

        // Set up the login form.
        mMsisdnView = (TextView) findViewById(R.id.msisdn);
        mPasswordView = (TextView) findViewById(R.id.password);

        mMsisdnView.setText(TextSecurePreferences.getLocalNumber(context).replace("+",""));

        mRegisterView = (Button) findViewById(R.id.no_account_button);
        mRegisterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, KPRegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, 0);
            }
        });

        mLoginView = (Button) findViewById(R.id.login_button);
        mLoginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptLogin() {
        if (mLoginTask != null) {
            return;
        }

        // Reset errors.
        mMsisdnView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String msisdn = mMsisdnView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid msisdn.
        if (TextUtils.isEmpty(msisdn)) {
            mMsisdnView.setError("Field required");
            focusView = mMsisdnView;
            cancel = true;
        } else if (!isMsisdnValid(msisdn)) {
            mMsisdnView.setError("Invalid msisdn");
            focusView = mMsisdnView;
            cancel = true;
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
            mLoginTask = new UserLoginTask(msisdn, password);
            mLoginTask.execute((Void) null);
        }
    }

    private boolean isMsisdnValid(String msisdn) {
        return TextUtils.isDigitsOnly(msisdn) && msisdn.length() >= 10;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8;
    }

    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private class UserLoginTask extends AsyncTask<Void, Void, Object> {

        private final String mMsisdn;
        private final String mPassword;

        UserLoginTask(String msisdn, String password) {
            mMsisdn = msisdn;
            mPassword = password;
        }

        @Override
        protected Object doInBackground(Void... params) {
            final KPRestInterface rest = KPRestClient.getClient(context).create(KPRestInterface.class);

            Call<KPLoginStatus> call = rest.userLogin(Credentials.basic(mMsisdn, mPassword));
            try {
                Response<KPLoginStatus> response = call.execute();
                if (response.code() == 200) {
                    return response.body();
                } else if (response.code() == 401) {
                    return new KPAuthException();
                }
            } catch (IOException e) {
                return e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Object obj) {
            mLoginTask = null;
            showProgress(false);

            if (obj != null) {
                if (obj instanceof KPLoginStatus) {
                    KPLoginStatus auth = (KPLoginStatus) obj;

                    if(auth.getStatus().equals("OK")) {
                        TextSecurePreferences.kpSetMsisdn(context, auth.getProfile().getMsisdn());
                        TextSecurePreferences.kpSetName(context, auth.getProfile().getName());
                        TextSecurePreferences.kpSetEmail(context, auth.getProfile().getEmail());
                        TextSecurePreferences.kpSetNik(context, auth.getProfile().getNik());
                        TextSecurePreferences.kpSetReferral(context, auth.getProfile().getReferral());
                        TextSecurePreferences.kpSetPassword(context, mPassword);
                        TextSecurePreferences.kpSetLoggedIn(context, true);

                        Intent intent = new Intent(context, KPDashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else if(auth.getStatus().equals("ERROR")) {
                        String message = auth.getMessage();
                        Snackbar.make(parent_view, message, Snackbar.LENGTH_SHORT).show();
                    }
                } else if (obj instanceof KPAuthException) {
                    mPasswordView.setError("Incorrect password");
                    mPasswordView.requestFocus();
                } else if (obj instanceof IOException) {
                    Snackbar.make(parent_view, "Connectivity error", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(parent_view, "Unknown error", Snackbar.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mLoginTask = null;
            showProgress(false);
        }
    }

}
