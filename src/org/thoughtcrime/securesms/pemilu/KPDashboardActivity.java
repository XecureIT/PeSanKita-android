package org.thoughtcrime.securesms.pemilu;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.ViewUtil;

import java.util.concurrent.TimeUnit;

/**
 * Created by rendra on 17/03/19.
 */

public class KPDashboardActivity extends AppCompatActivity {
    private Context context;
    private View parent_view;
    private FloatingActionButton fabLaporan;
    private FloatingActionButton fabProfile;
    private FloatingActionButton fabInvite;
    private FloatingActionButton fabRelawan;
    private FloatingActionButton fabPantau;
    private FloatingActionButton fabLapor;
    private CardView simulasiEnd;
    private CardView pelaporanStart;
    private CardView pelaporanStarted;
    private TextView simulasiCounter;
    private TextView pelaporanCounter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getBaseContext();

        setContentView(R.layout.kp_dashboard_activity_grid);

        initToolbar();
        initComponent();
        pokeToInvite();
        checkAndRunAction();
//        handleFirstTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().hide();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Halo, " + TextSecurePreferences.kpGetName(context));
        toolbar.setSubtitle("ID: " + TextSecurePreferences.kpGetMsisdn(context));
    }

    private void initComponent() {
        parent_view = findViewById(android.R.id.content);
        fabLaporan = ViewUtil.findById(this, R.id.fab_laporan);
        fabProfile = ViewUtil.findById(this, R.id.fab_profile);
        fabInvite = ViewUtil.findById(this, R.id.fab_invite);
        fabRelawan = ViewUtil.findById(this, R.id.fab_relawan);
        fabPantau = ViewUtil.findById(this, R.id.fab_pantau);
        fabLapor = ViewUtil.findById(this, R.id.fab_lapor);
        simulasiEnd = ViewUtil.findById(this, R.id.simulasi_end);
        pelaporanStart = ViewUtil.findById(this, R.id.pelaporan_start);
        pelaporanStarted = ViewUtil.findById(this, R.id.pelaporan_started);
        simulasiCounter = ViewUtil.findById(this, R.id.simulasi_counter);
        pelaporanCounter = ViewUtil.findById(this, R.id.pelaporan_counter);

        fabLaporan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLaporan();
            }
        });

        fabProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleProfile();
            }
        });

        fabInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleInviteFriend();
            }
        });

        fabRelawan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRelawan();
            }
        });

        fabPantau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePantau();
            }
        });

        fabLapor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLapor();
            }
        });
    }

    private void checkAndRunAction() {
        if (!KPHelper.isSimulasiEnded()) {
            simulasiEnd.setVisibility(View.VISIBLE);
            pelaporanStart.setVisibility(View.GONE);
            pelaporanStarted.setVisibility(View.GONE);
            updateSimulasiCounter();
        } else if (!KPHelper.isPelaporanStarted()) {
            simulasiEnd.setVisibility(View.GONE);
            pelaporanStart.setVisibility(View.VISIBLE);
            pelaporanStarted.setVisibility(View.GONE);
            updatePelaporanCounter();
        } else {
            simulasiEnd.setVisibility(View.GONE);
            pelaporanStart.setVisibility(View.GONE);
            pelaporanStarted.setVisibility(View.VISIBLE);
        }
    }

    private void updateSimulasiCounter() {
        Long diffMillis = KPHelper.simulasiEndMillis();

        if (diffMillis > 0) {
            new CountDownTimer(diffMillis, 1000) {
                public void onTick(long millisUntilFinished) {

                    long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                    millisUntilFinished -= TimeUnit.DAYS.toMillis(days);

                    long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                    millisUntilFinished -= TimeUnit.HOURS.toMillis(hours);

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                    millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes);

                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

                    simulasiCounter.setText(days + " hari " + hours + " jam " + minutes + " menit " + seconds + " detik");
                }

                public void onFinish() {
                    checkAndRunAction();
                }
            }.start();
        } else {
            checkAndRunAction();
        }
    }

    private void updatePelaporanCounter() {
        Long diffMillis = KPHelper.pelaporanStartMillis();

        if (diffMillis > 0) {
            new CountDownTimer(diffMillis, 1000) {
                public void onTick(long millisUntilFinished) {

                    long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                    millisUntilFinished -= TimeUnit.HOURS.toMillis(hours);

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                    millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes);

                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

                    pelaporanCounter.setText(hours + " jam " + minutes + " menit " + seconds + " detik");
                }

                public void onFinish() {
                    checkAndRunAction();
                }
            }.start();
        } else {
            checkAndRunAction();
        }
    }

    private void handleLaporan() {
        Intent intent = new Intent(this, KPLaporanActivity.class);
        startActivity(intent);
    }

    private void handleProfile() {
        Intent intent = new Intent(this, KPProfileActivity.class);
        startActivity(intent);
    }

    private void handleInviteFriend() {
        Intent intent = new Intent(this, KPInviteActivity.class);
        startActivity(intent);
    }

    private void handleRelawan() {
        String url = "https://kawalpilpres2019.id/relawan";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void handlePantau() {
        String url = "https://pantau.kawalpilpres2019.id";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void handleLapor() {
        Intent intent = new Intent(this, KPLaporanBaruActivity.class);
        intent.putExtra("shortcut", true);
        startActivity(intent);
    }

    private void pokeToInvite() {
        if (KPHelper.isPelaporanStarted()) return;

        final Snackbar snackbar = Snackbar.make(parent_view, "", Snackbar.LENGTH_LONG);
        //inflate view
        View custom_view = getLayoutInflater().inflate(R.layout.kp_snackbar_floating, null);
        TextView action = (TextView) custom_view.findViewById(R.id.snackbar_action);

        action.setText("UNDANG");
        ((TextView) custom_view.findViewById(R.id.snackbar_title)).setText("Undang Teman");
        ((TextView) custom_view.findViewById(R.id.snackbar_text)).setText("Ayo undang temanmu sekarang");

        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackBarView = (Snackbar.SnackbarLayout) snackbar.getView();
        snackBarView.setPadding(0, 0, 0, 0);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                handleInviteFriend();
            }
        });

        snackBarView.addView(custom_view, 0);
        snackbar.show();
    }

    public void handleFirstTime() {
        if (KPHelper.isFirstTime(context)) {
            handleInviteFriend();
        }
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Anda yakin untuk logout ?");
        builder.setPositiveButton("LOGOUT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                KPHelper.clearCredentials(context);
                Intent intent = new Intent(context, KPLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("BATAL", null);
        builder.show();
    }

}
