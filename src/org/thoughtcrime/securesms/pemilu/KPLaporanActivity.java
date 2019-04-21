package org.thoughtcrime.securesms.pemilu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.pemilu.database.KPDaerahDatabase;
import org.thoughtcrime.securesms.pemilu.database.KPLaporanDatabase;
import org.thoughtcrime.securesms.pemilu.model.KPC1Url;
import org.thoughtcrime.securesms.pemilu.model.KPDaerah;
import org.thoughtcrime.securesms.pemilu.model.KPLaporan;
import org.thoughtcrime.securesms.pemilu.model.KPListLaporan;
import org.thoughtcrime.securesms.pemilu.model.KPStatus;
import org.thoughtcrime.securesms.pemilu.service.KPAuthException;
import org.thoughtcrime.securesms.pemilu.service.KPRestClient;
import org.thoughtcrime.securesms.pemilu.service.KPRestInterface;
import org.thoughtcrime.securesms.util.ViewUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class KPLaporanActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final int DIALOG_QUEST_CODE = 300;
    private static final String TAG = KPLaporanBaruActivity.class.getSimpleName();
    private Context context;
    private View parent;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeLayout;
    private KPLaporanAdapter adapter;
    private List<KPLaporan> items = new ArrayList<>();
    private KPLaporanDatabase laporanDatabase;
    private KPDaerahDatabase daerahDatabase;
    private AlertDialog dialog = null;

    private boolean laporanProses = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getBaseContext();
        setContentView(R.layout.kp_laporan_activity);
        getSupportActionBar().setTitle("Laporan Saya" + (!KPHelper.isSimulasiEnded() ? " (simulasi)" : ""));
        parent = findViewById(android.R.id.content);

        laporanDatabase = KPDatabaseHelper.getLaporanDatabase(context);
        daerahDatabase = KPDatabaseHelper.getDaerahDatabase(context);

        int resultCode = getIntent().getIntExtra("resultCode", RESULT_CANCELED);
        if (resultCode == RESULT_OK) {
            Snackbar.make(parent, "Laporan berhasil dikirim", Snackbar.LENGTH_SHORT).show();
        } else if (resultCode == RESULT_FIRST_USER) {
            Snackbar.make(parent, "Laporan berhasil disimpan", Snackbar.LENGTH_SHORT).show();
        }

        initComponent();
        purgeLaporanSimulasi();
        loadData();
        reSubmitLaporan();
//        showAttentionDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(parent, "Laporan berhasil dikirim", Snackbar.LENGTH_SHORT).show();
                loadData();
            } else if (resultCode == RESULT_FIRST_USER) {
                Snackbar.make(parent, "Laporan berhasil disimpan", Snackbar.LENGTH_SHORT).show();
                loadData();
            }
        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    private void initComponent() {
        swipeLayout = ViewUtil.findById(this, R.id.swipe_layout);
        swipeLayout.setOnRefreshListener(this);

        fab = ViewUtil.findById(this, R.id.fab);
        fab.setOnClickListener(v -> {
            if (!KPHelper.isSimulasiEnded() || KPHelper.isPelaporanStarted()) {
                Intent intent = new Intent(this, KPLaporanBaruActivity.class);
                startActivityForResult(intent, 0);
            } else {
                Snackbar.make(parent, "Laporan TPS dimulai pukul 13.00 WIB", Snackbar.LENGTH_LONG).show();
            }
        });

        if (KPHelper.isSimulasiEnded() && !KPHelper.isPelaporanStarted()) {
            Snackbar.make(parent, "Laporan TPS dimulai pukul 13.00 WIB", Snackbar.LENGTH_LONG).show();
        }
    }

    private void purgeLaporanSimulasi() {
        if (KPHelper.isSimulasiEnded()) {
            List<KPLaporan> itemsLaporan = laporanDatabase.getAllLaporan();
            for (KPLaporan laporan: itemsLaporan) {
                System.out.println(laporan.getCreated_at());
                if (KPHelper.isLaporanSimulasi("yyyyMMdd_HHmmss", laporan.getCreated_at())) {
                    laporanDatabase.deleteLaporan(laporan);
                }
            }
        }
    }

    private void loadData() {
        Log.d(TAG, "loadData");
        items = laporanDatabase.getAllLaporan();
        setRecyclerView();
        swipeLayout.setRefreshing(false);
        adapter.notifyDataSetChanged();
    }

    private void showAttentionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Perhatian!!");
        builder.setMessage("Data laporan hanya untuk uji coba, bukan data riil.");
        builder.setPositiveButton("OK, SAYA MENGERTI", null);
        builder.show();
    }

    @Override
    public void onRefresh() {
        if (!KPHelper.isSimulasiEnded() || KPHelper.isPelaporanStarted()) {
            new GetLaporanTask().execute((Void) null);
        } else {
            swipeLayout.setRefreshing(false);
            Snackbar.make(parent, "Laporan TPS dimulai pukul 13.00 WIB", Snackbar.LENGTH_LONG).show();
        }
    }

    public void reSubmitLaporan() {
        if (laporanDatabase.getRetryLaporan().size() > 0) {
            List<KPLaporan> listLaporan = laporanDatabase.getRetryLaporan();
            List<KPLaporan> tobeDeleted = new ArrayList<KPLaporan>();
            for (KPLaporan laporan : listLaporan) {
                String c1Path = laporan.getAttachmentIdBase64();
                if (c1Path != null && new File(c1Path).exists()) {
                    new ReSubmitLaporanTask(laporan).execute();
                } else {
                    tobeDeleted.add(laporan);
                }
            }
            if (tobeDeleted.size() > 0) {
                for (KPLaporan laporan : tobeDeleted) {
                    laporanDatabase.deleteLaporan(laporan);
                }
                items = laporanDatabase.getAllLaporan();
                adapter.setItems(items);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void setRecyclerView() {
        List<KPLaporan> newList = new ArrayList<>();
        KPLaporan last, tmp;

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);

        for (int i = 0; i < items.size(); i++) {
            KPLaporan current = items.get(i);

            if (i == 0) {
                tmp = new KPLaporan();
                tmp.setType(99);
                tmp.setKelurahan(current.getKelurahan());
                newList.add(tmp);
            }

            if (i > 0) {
                last = items.get(i - 1);
                if (!last.getKelurahan().equals(current.getKelurahan())) {
                    tmp = new KPLaporan();
                    tmp.setType(99);
                    tmp.setKelurahan(current.getKelurahan());
                    newList.add(tmp);
                }
            }

            newList.add(items.get(i));
        }

        adapter = new KPLaporanAdapter(context, newList);
        recyclerView.setAdapter(adapter);
        recyclerView.invalidate();

        // on item list clicked
        adapter.setOnItemClickListener(new KPLaporanAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, KPLaporan obj, int position) {
                showPreviewLaporanDialog(obj);
            }
        });
    }

    private void forceLogout() {
        KPHelper.clearCredentials(context);
        Intent intent = new Intent(context, KPRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showPreviewLaporanDialog(KPLaporan l) {
        // Custom View
        LayoutInflater inflaterr = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflaterr.inflate(R.layout.kp_dialog_preview_laporan, null);

        ImageView attachment = customView.findViewById(R.id.attachment);
        ImageView status = customView.findViewById(R.id.status);
        Button tps = customView.findViewById(R.id.tps);
        TextView daerah = customView.findViewById(R.id.daerah);
        TextView count1 = customView.findViewById(R.id.count1);
        TextView count2 = customView.findViewById(R.id.count2);
        TextView s1 = customView.findViewById(R.id.s1);
        TextView n1 = customView.findViewById(R.id.n1);

        if (l.getAttachmentIdBase64() != null) {
            File imgFile = new File(l.getAttachmentIdBase64());
            if (imgFile.exists()) {
                Glide.with(this)
                        .asBitmap()
                        .load(imgFile)
                        .into(attachment);

                attachment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        previewImage(l.getAttachmentIdBase64());
                        closeDialog();
                    }
                });
            }
        } else {
            String url = KPHelper.getC1Url(l.getAttachmentId());
            Glide.with(this)
                    .asBitmap()
                    .load(url)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            attachment.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    previewImage(url);
                                    closeDialog();
                                }
                            });
                            return false;
                        }
                    })
                    .into(attachment);
        }

        if (l.getType() == KPLaporanBaruActivity.RETRY) {
            status.setImageResource(R.drawable.ic_error_white_18dp);
            status.setColorFilter(ContextCompat.getColor(context, R.color.kawalpilpres_primary), android.graphics.PorterDuff.Mode.MULTIPLY);
        } else {
            status.setImageResource(R.drawable.ic_check_circle_white_18dp);
            status.setColorFilter(ContextCompat.getColor(context, R.color.blue), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        tps.setText("TPS " + l.getTps());
        daerah.setText(daerahDatabase.getSectionString(l.getKelurahan()));
        count1.setText(String.valueOf(l.getCount1()));
        count2.setText(String.valueOf(l.getCount2()));
        s1.setText(String.valueOf(l.getS1()));
        n1.setText(String.valueOf(l.getN1()));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setView(customView);
        dialog.show();

    }

    private void closeDialog() {
        if (KPLaporanActivity.this.dialog != null)
            KPLaporanActivity.this.dialog.dismiss();
    }

    private void previewImage(String path) {
        Bundle bundle = new Bundle();
        bundle.putString("PathImage", path);

        FragmentManager fragmentManager = getSupportFragmentManager();
        PreviewImageFragment previewFragment = new PreviewImageFragment();
        previewFragment.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, previewFragment).addToBackStack(null).commit();
    }

    private class GetLaporanTask extends AsyncTask<Void, Void, Object> {
        GetLaporanTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            laporanProses = true;
        }

        @Override
        protected Object doInBackground(Void... voids) {
            final KPRestInterface rest = KPRestClient.getClient(context).create(KPRestInterface.class);

            Call<KPListLaporan> call = rest.getLaporan();
            try {
                Response<KPListLaporan> response = call.execute();
                if (response.code() == 200) {
                    KPListLaporan listLaporan = response.body();
                    if (listLaporan.getStatus().equals("OK")) {
                        List<KPLaporan> laporans = listLaporan.getData();
                        if (laporans.size() > 0) {
                            for (KPLaporan laporan : laporans) {

                                KPDaerah.ItemDaerah kelurahan = daerahDatabase.getDaerah(laporan.getKelurahan());
                                KPDaerah.ItemDaerah kecamatan = daerahDatabase.getDaerah(kelurahan.getParent());
                                KPDaerah.ItemDaerah kabupaten = daerahDatabase.getDaerah(kecamatan.getParent());
                                KPDaerah.ItemDaerah provinsi = daerahDatabase.getDaerah(kabupaten.getParent());

                                laporan.setKecamatan(kecamatan.getId());
                                laporan.setKabupaten(kabupaten.getId());
                                laporan.setProvinsi(provinsi.getId());
                                laporan.setAttachmentId(laporan.getC1());

                                laporanDatabase.addOrUpdateLaporan(laporan);
                            }
                            items = laporanDatabase.getAllLaporan();
                        }
                    }
                    return (KPStatus) listLaporan;
                } else if (response.code() == 400) {
                    return new Gson().fromJson(response.errorBody().string(), KPStatus.class);
                } else if (response.code() == 401) {
                    return new KPAuthException();
                }
            } catch (IOException e) {
                return e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object obj) {
            super.onPostExecute(obj);
            if (obj != null) {
                if (obj instanceof KPStatus) {
                    KPStatus status = (KPStatus) obj;
                    if (status.getStatus().equals("OK")) {
                        laporanProses = false;
                        setRecyclerView();
                    } else {
                        Snackbar.make(parent, status.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                } else if (obj instanceof KPAuthException) {
                    forceLogout();
                } else if (obj instanceof IOException) {
                    Snackbar.make(parent, "Connectivity error", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(parent, "Unknown error", Snackbar.LENGTH_SHORT).show();
            }

            adapter.notifyDataSetChanged();
            swipeLayout.setRefreshing(false);
        }
    }

    private class ReSubmitLaporanTask extends AsyncTask<Void, Void, Object> {
        private KPLaporan laporan;
        private String c1Path;
        private String cType;

        ReSubmitLaporanTask(KPLaporan laporan) {
            this.laporan = laporan;
            this.c1Path = laporan.getAttachmentIdBase64();
            this.cType = KPHelper.getCType(c1Path);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Void... voids) {
            final KPRestInterface rest = KPRestClient.getClient(context).create(KPRestInterface.class);

            // get presigned url for attachment
            Call<KPC1Url> call = rest.getAttachmentUrl(laporan.getAttachmentId(), cType);
            try {
                Response<KPC1Url> response = call.execute();
                Log.d(TAG, "KPC1Url: response:" + response.code());
                if (response.code() == 200) {
                    KPC1Url kpc1Url = response.body();
                    Log.d(TAG, "KPC1Url: status:" + kpc1Url.getStatus());
                    if (kpc1Url.getStatus().equals("OK")) {
                        URL url = new URL(kpc1Url.getUrl());

                        // upload attachment
                        File file = KPHelper.getImageCompressed(context, c1Path);
                        Log.d(TAG, "S3: attachment resized to " + file.length());
                        KPHelper.uploadAttachment("PUT", url, cType, new FileInputStream(file), file.length());
                        Log.d(TAG, "S3: attachment uploaded");

                        // submit laporan
                        Call<KPStatus> callLaporan = rest.postLaporan(laporan);
                        Response<KPStatus> respLaporan = callLaporan.execute();
                        Log.d(TAG, "KPStatus: response:" + respLaporan.code());
                        if (respLaporan.code() == 200) {
                            return respLaporan.body();
                        } else if (respLaporan.code() == 400) {
                            return new Gson().fromJson(respLaporan.errorBody().string(), KPStatus.class);
                        } else if (respLaporan.code() == 401) {
                            return new KPAuthException();
                        }
                    }
                } else if (response.code() == 400) {
                    return new Gson().fromJson(response.errorBody().string(), KPStatus.class);
                } else if (response.code() == 401) {
                    return new KPAuthException();
                }
            } catch (IOException e) {
                return e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object obj) {
            super.onPostExecute(obj);

            if (obj != null) {
                if (obj instanceof KPStatus) {
                    KPStatus post = (KPStatus) obj;
                    if (post.getStatus().equals("OK")) {
                        laporan.setType(KPLaporanBaruActivity.OK);
                        laporanDatabase.addOrUpdateLaporan(laporan);
                        loadData();
                    } else if (post.getStatus().equals("ERROR")) {
//                        Snackbar.make(parent, post.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                } else if (obj instanceof KPAuthException) {
                    forceLogout();
                } else if (obj instanceof IOException) {
                    laporan.setType(KPLaporanBaruActivity.RETRY);
                    laporanDatabase.addOrUpdateLaporan(laporan);
//                    Snackbar.make(parent, "Connectivity error", Snackbar.LENGTH_SHORT).show();
                }
            } else {
//                Snackbar.make(parent, "Unknown error", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
