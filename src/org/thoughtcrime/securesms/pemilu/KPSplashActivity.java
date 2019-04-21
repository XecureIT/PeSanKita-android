package org.thoughtcrime.securesms.pemilu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import org.thoughtcrime.securesms.R;

/**
 * Created by rendra on 17/03/19.
 */

public class KPSplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kp_splash_activity);

        // Calling Method check and run desired Activity.....
        checkAndRunActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().hide();
    }

    private void checkAndRunActivity() {
        if (KPHelper.isLoggedIn(this)) {
            //show Dashboard Activity
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent splashScreen = new Intent(KPSplashActivity.this, KPDashboardActivity.class);
                    startActivity(splashScreen);
                    finish();
                }
            },1600);
        } else {
            // otherwise show Register Activity
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent splashScreen = new Intent(KPSplashActivity.this, KPRegisterActivity.class);
                    startActivity(splashScreen);
                    finish();
                }
            },1600);
        }
    }

}
