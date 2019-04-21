package org.thoughtcrime.securesms.pemilu;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.pemilu.database.KPDaerahDatabase;
import org.thoughtcrime.securesms.pemilu.database.KPLaporanDatabase;
import org.thoughtcrime.securesms.pemilu.model.KPC1Url;
import org.thoughtcrime.securesms.pemilu.model.KPDaerah;
import org.thoughtcrime.securesms.pemilu.model.KPLaporan;
import org.thoughtcrime.securesms.pemilu.model.KPStatus;
import org.thoughtcrime.securesms.pemilu.service.KPAuthException;
import org.thoughtcrime.securesms.pemilu.service.KPRestClient;
import org.thoughtcrime.securesms.pemilu.service.KPRestInterface;
import org.thoughtcrime.securesms.permissions.Permissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class KPLaporanBaruActivity extends AppCompatActivity {

    public static final int OK = 0;
    public static final int RETRY = 1;
    public static final int GALLERY_REQUEST = 1;
    public static final int CAMERA_REQUEST = 2;
    public static final String KEY_PROVINSI = "provinsi";
    public static final String KEY_KABUPATEN = "kabupaten";
    public static final String KEY_KECAMATAN = "kecamatan";
    public static final String KEY_KELURAHAN = "kelurahan";
    public static final String TITLE_PROVINSI = "Provinsi";
    public static final String TITLE_KABUPATEN = "Kabupaten/Kota";
    public static final String TITLE_KECAMATAN = "Kecamatan";
    public static final String TITLE_KELURAHAN = "Kelurahan/Desa";
    private static final String TAG = KPLaporanBaruActivity.class.getSimpleName();
    private List<KPDaerah.ItemDaerah> itemsProvinsi, itemsKabupaten, itemsKecamatan, itemsKelurahan;
    private KPDaerah.ItemDaerah selectedProvinsi, selectedKabupaten, selectedKecamatan, selectedKelurahan;
    private boolean daerahProses = false;
    private View focusView = null;

    private Context context;
    private View parent;
    private RelativeLayout mProgressView;
    private LinearLayout mContentView;
    private TextView mTitle;
    private String tps;
    private String count1;
    private String count2;
    private String sah;
    private String tidakSah;
    private String currentPhotoPath;

    private Button eProvinsi;
    private Button eKabupaten;
    private Button eKecamatan;
    private Button eKelurahan;
    private TextView lProvinsi;
    private TextView lKabupaten;
    private TextView lKecamatan;
    private TextView lKelurahan;
    private ProgressBar pProvinsi;
    private ProgressBar pKabupaten;
    private ProgressBar pKecamatan;
    private ProgressBar pKelurahan;
    private EditText eTps;
    private EditText eCount1;
    private EditText eCount2;
    private EditText eSah;
    private EditText eTidakSah;
    private RelativeLayout lImage;
    private LinearLayout lButton;
    private ImageView imageC1;
    private ImageView bClose;
    private Button bSubmit;
    private FloatingActionButton fabGallery;
    private FloatingActionButton fabCamera;
    private FloatingActionButton fabExample;
    private AlertDialog dialog = null;

    private KPDaerahDatabase daerahDatabase;
    private KPLaporanDatabase laporanDatabase;

    private boolean shortcut;

    public KPLaporanBaruActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getBaseContext();
        setContentView(R.layout.kp_laporan_baru_activity);
        parent = findViewById(android.R.id.content);

        daerahDatabase = KPDatabaseHelper.getDaerahDatabase(context);
        laporanDatabase = KPDatabaseHelper.getLaporanDatabase(context);

        shortcut = getIntent().getBooleanExtra("shortcut", false);

        initComponent();
//        showAttentionDialog();
        getDataProvinsi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().hide();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                Uri selectedImage = data.getData();
                currentPhotoPath = KPHelper.getRealPathFromURI(this, selectedImage);
                Glide.with(this)
                        .asBitmap()
                        .load(selectedImage)
                        .into(imageC1);
            }

            if (requestCode == CAMERA_REQUEST) {
                Log.d("ARS", "onActivityResult: currentPhotoPath:" + currentPhotoPath);
                File imgFile = new File(currentPhotoPath);
                if (imgFile.exists()) {
                    Glide.with(this)
                            .asBitmap()
                            .load(imgFile)
                            .into(imageC1);
                }
            }

            closeButton(true);
            focusView = imageC1;
            lImage.setVisibility(View.VISIBLE);
            lButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Permissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            showExitDialog();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    public void clickAction(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.provinsi:
                showProvinsiDialog();
                break;
            case R.id.kabupaten:
                showKabupatenDialog();
                break;
            case R.id.kecamatan:
                showKecamatanDialog();
                break;
            case R.id.kelurahan:
                showKelurahanDialog();
                break;
        }
    }

    private void initComponent() {
        // Inflate the layout for this fragment
        mProgressView = findViewById(R.id.progress);
        mContentView = findViewById(R.id.content);
        mTitle = findViewById(R.id.title);
        fabGallery = findViewById(R.id.fab_gallery);
        fabCamera = findViewById(R.id.fab_camera);
        fabExample = findViewById(R.id.fab_example);

        eProvinsi = findViewById(R.id.provinsi);
        eKabupaten = findViewById(R.id.kabupaten);
        eKecamatan = findViewById(R.id.kecamatan);
        eKelurahan = findViewById(R.id.kelurahan);
        lProvinsi = findViewById(R.id.labelProvinsi);
        lKabupaten = findViewById(R.id.labelKabupaten);
        lKecamatan = findViewById(R.id.labelKecamatan);
        lKelurahan = findViewById(R.id.labelKelurahan);
        pProvinsi = findViewById(R.id.progressProvinsi);
        pKabupaten = findViewById(R.id.progressKabupaten);
        pKecamatan = findViewById(R.id.progressKecamatan);
        pKelurahan = findViewById(R.id.progressKelurahan);
        eTps = findViewById(R.id.tps);
        eCount1 = findViewById(R.id.count1);
        eCount2 = findViewById(R.id.count2);
        eSah = findViewById(R.id.suara_sah);
        eTidakSah = findViewById(R.id.suara_tidak_sah);
        imageC1 = findViewById(R.id.add_c1);
        lImage = findViewById(R.id.layout_image);
        lButton = findViewById(R.id.layout_button);
        bClose = findViewById(R.id.button_close);
        bSubmit = findViewById(R.id.submit);

        mTitle.setText("Form Laporan" + (!KPHelper.isSimulasiEnded() ? " (simulasi)" : ""));

        findViewById(R.id.exit).setOnClickListener(v -> {
            showExitDialog();
        });

        bClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeButton(false);
                focusView = bSubmit;
                lImage.setVisibility(View.GONE);
                lButton.setVisibility(View.VISIBLE);
            }
        });

        imageC1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previewImage(currentPhotoPath);
            }
        });

        fabGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
            }
        });

        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromCamera();
            }
        });

        fabExample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previewImage();
            }
        });

        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitData();
            }
        });

        eCount1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                jumlahSuaraSah();
            }
        });

        eCount2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                jumlahSuaraSah();
            }
        });
    }

    private void jumlahSuaraSah() {
        int iCount1 = 0;
        int iCount2 = 0;

        if (!TextUtils.isEmpty(eCount1.getText())) {
            iCount1 = Integer.parseInt(eCount1.getText().toString().trim());
        }
        if (!TextUtils.isEmpty(eCount2.getText())) {
            iCount2 = Integer.parseInt(eCount2.getText().toString().trim());
        }

        eSah.setText(String.valueOf(iCount1 + iCount2));
    }

    private void getDataProvinsi() {
        itemsProvinsi = daerahDatabase.getProvinsiDb();
        Log.d(TAG, "getDataProvinsi: item:" + itemsProvinsi.size());

        if (itemsProvinsi.size() == 0 && KPHelper.isNetworkAvailable(context)) {
            new GetDaerahTask(KEY_PROVINSI, null).execute();
        }
    }

    private void getDataKabupaten() {
        String parentId = selectedProvinsi.getId();
        itemsKabupaten = daerahDatabase.getKabupatenDb(parentId);
        Log.d(TAG, "getDataKabupaten: parent:" + parentId + " item:" + itemsKabupaten.size());

        if (itemsKabupaten.size() == 0 && KPHelper.isNetworkAvailable(context)) {
            new GetDaerahTask(KEY_KABUPATEN, parentId).execute();
        }
    }

    private void getDataKecamatan() {
        String parentId = selectedKabupaten.getId();
        itemsKecamatan = daerahDatabase.getKecamatanDb(parentId);
        Log.d(TAG, "getDataKecamatan: parent:" + parentId + " item:" + itemsKecamatan.size());

        if (itemsKecamatan.size() == 0 && KPHelper.isNetworkAvailable(context)) {
            new GetDaerahTask(KEY_KECAMATAN, parentId).execute();
        }
    }

    private void getDataKelurahan() {
        String parentId = selectedKecamatan.getId();
        itemsKelurahan = daerahDatabase.getKelurahanDb(parentId);
        Log.d(TAG, "getDataKelurahan: parent:" + parentId + " item:" + itemsKelurahan.size());

        if (itemsKelurahan.size() == 0 && KPHelper.isNetworkAvailable(context)) {
            new GetDaerahTask(KEY_KELURAHAN, parentId).execute();
        }
    }

    private void showProvinsiDialog() {
        // Custom View
        LayoutInflater inflaterr = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflaterr.inflate(R.layout.kp_dialog_daerah, null);
        ListView listView = customView.findViewById(R.id.listview);
        KPDaerahAdapter adap = new KPDaerahAdapter(itemsProvinsi, context);
        listView.setAdapter(adap);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView radio = view.findViewById(R.id.name);
                radio.setChecked(true);

                selectedProvinsi = itemsProvinsi.get(position);
                eProvinsi.setText(selectedProvinsi.getNama());
                eProvinsi.setError(null);
                lProvinsi.setVisibility(View.VISIBLE);

                selectedKabupaten = null;
                eKabupaten.setText(TITLE_KABUPATEN);
                lKabupaten.setVisibility(View.INVISIBLE);

                selectedKecamatan = null;
                eKecamatan.setText(TITLE_KECAMATAN);
                lKecamatan.setVisibility(View.INVISIBLE);

                selectedKelurahan = null;
                eKelurahan.setText(TITLE_KELURAHAN);
                lKelurahan.setVisibility(View.INVISIBLE);

                getDataKabupaten();
                closeDialog();
            }
        });
        createDaerahDialog(TITLE_PROVINSI, customView);
    }

    private void showKabupatenDialog() {
        if (selectedProvinsi == null) {
            Snackbar.make(parent, "Pilih Provinsi terlebih dahulu", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (daerahProses) {
            Snackbar.make(parent, "Mohon tunggu, sedang mengambil data " + TITLE_KABUPATEN, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (itemsKabupaten == null) {
            Snackbar.make(parent, "Tidak ada data " + TITLE_KABUPATEN, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Custom View
        LayoutInflater inflaterr = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflaterr.inflate(R.layout.kp_dialog_daerah, null);
        ListView listView = customView.findViewById(R.id.listview);
        KPDaerahAdapter adap = new KPDaerahAdapter(itemsKabupaten, context);
        listView.setAdapter(adap);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView radio = view.findViewById(R.id.name);
                radio.setChecked(true);

                selectedKabupaten = itemsKabupaten.get(position);
                eKabupaten.setText(selectedKabupaten.getNama());
                eKabupaten.setError(null);
                lKabupaten.setVisibility(View.VISIBLE);

                selectedKecamatan = null;
                eKecamatan.setText(TITLE_KECAMATAN);
                lKecamatan.setVisibility(View.INVISIBLE);

                selectedKelurahan = null;
                eKelurahan.setText(TITLE_KELURAHAN);
                lKelurahan.setVisibility(View.INVISIBLE);

                getDataKecamatan();
                closeDialog();
            }
        });

        createDaerahDialog(TITLE_KABUPATEN, customView);
    }

    private void showKecamatanDialog() {
        if (selectedKabupaten == null) {
            Snackbar.make(parent, "Pilih Kabupaten/Kota terlebih dahulu", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (daerahProses) {
            Snackbar.make(parent, "Mohon tunggu, sedang mengambil data " + TITLE_KECAMATAN, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (itemsKecamatan == null) {
            Snackbar.make(parent, "Tidak ada data " + TITLE_KECAMATAN, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Custom View
        LayoutInflater inflaterr = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflaterr.inflate(R.layout.kp_dialog_daerah, null);
        ListView listView = customView.findViewById(R.id.listview);
        KPDaerahAdapter adap = new KPDaerahAdapter(itemsKecamatan, context);
        listView.setAdapter(adap);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView radio = view.findViewById(R.id.name);
                radio.setChecked(true);

                selectedKecamatan = itemsKecamatan.get(position);
                eKecamatan.setText(selectedKecamatan.getNama());
                eKecamatan.setError(null);
                lKecamatan.setVisibility(View.VISIBLE);

                selectedKelurahan = null;
                eKelurahan.setText(TITLE_KELURAHAN);
                lKelurahan.setVisibility(View.INVISIBLE);

                getDataKelurahan();
                closeDialog();
            }
        });

        createDaerahDialog(TITLE_KECAMATAN, customView);
    }

    private void showKelurahanDialog() {
        if (selectedKecamatan == null) {
            Snackbar.make(parent, "Pilih Kecamatan terlebih dahulu", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (daerahProses) {
            Snackbar.make(parent, "Mohon tunggu, sedang mengambil data " + TITLE_KELURAHAN, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (itemsKelurahan == null) {
            Snackbar.make(parent, "Tidak ada data " + TITLE_KELURAHAN, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Custom View
        LayoutInflater inflaterr = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflaterr.inflate(R.layout.kp_dialog_daerah, null);
        ListView listView = customView.findViewById(R.id.listview);
        KPDaerahAdapter adap = new KPDaerahAdapter(itemsKelurahan, context);
        listView.setAdapter(adap);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView radio = view.findViewById(R.id.name);
                radio.setChecked(true);

                selectedKelurahan = itemsKelurahan.get(position);
                eKelurahan.setText(selectedKelurahan.getNama());
                eKelurahan.setError(null);
                lKelurahan.setVisibility(View.VISIBLE);

                closeDialog();
            }
        });

        createDaerahDialog(TITLE_KELURAHAN, customView);
    }

    private void createDaerahDialog(String title, View customView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        this.dialog = builder.create();
        this.dialog.setTitle(title);
        this.dialog.setView(customView);
        this.dialog.show();
    }

    private void closeDialog() {
        if (KPLaporanBaruActivity.this.dialog != null)
            KPLaporanBaruActivity.this.dialog.dismiss();
    }

    private void submitData() {
        if (isValidFormData()) {
            boolean cancel = false;

            if (TextUtils.isEmpty(currentPhotoPath)) {
                Snackbar.make(parent, "Silahkan tambahkan foto C1 Plano", Snackbar.LENGTH_SHORT).show();
                focusView = imageC1;
                cancel = true;
            }

            if (cancel) {
                if (imageC1 != focusView) {
                    focusView.requestFocus();
                }
            } else {
                Log.d(TAG, "submitData: start");

                String attachmentId = KPHelper.getAttachmentId(tps, selectedKelurahan.getId());

                KPLaporan laporan = new KPLaporan();
                laporan.setKelurahan(selectedKelurahan.getId());
                laporan.setTps(tps);
                laporan.setCount1(Integer.valueOf(count1));
                laporan.setCount2(Integer.valueOf(count2));
                laporan.setS1(Integer.valueOf(sah));
                laporan.setN1(Integer.valueOf(tidakSah));
                laporan.setAttachmentId(attachmentId);
                laporan.setAttachmentIdBase64(currentPhotoPath);
                laporan.setProvinsi(selectedProvinsi.getId());
                laporan.setKabupaten(selectedKabupaten.getId());
                laporan.setKecamatan(selectedKecamatan.getId());
                laporan.setCreated_at(KPHelper.getTimestamp());

                new SubmitLaporanTask(laporan).execute();

            }
        }
    }

    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//        mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showAttentionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Perhatian!!");
        builder.setMessage("Form laporan hanya untuk uji coba, tidak perlu memasukkan data riil.");
        builder.setPositiveButton("OK, SAYA MENGERTI", null);
        builder.show();
    }

    private void showExitDialog() {
        if (selectedProvinsi != null ||
                selectedKabupaten != null ||
                selectedKecamatan != null ||
                selectedKelurahan != null ||
                !TextUtils.isEmpty(tps) ||
                !TextUtils.isEmpty(count1) ||
                !TextUtils.isEmpty(count2) ||
                !TextUtils.isEmpty(sah) ||
                !TextUtils.isEmpty(tidakSah) ||
                !TextUtils.isEmpty(currentPhotoPath)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Batalkan laporan?");
            builder.setMessage("Data yang sudah terisi akan terhapus.");
            builder.setPositiveButton("YA", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    setResultAndFinish(RESULT_CANCELED);
                }
            });
            builder.setNegativeButton("BATAL", null);
            builder.show();
        } else {
            setResultAndFinish(RESULT_CANCELED);
        }
    }

    private void pickFromGallery() {
        Permissions.with(KPLaporanBaruActivity.this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .ifNecessary()
                .withPermanentDenialDialog(getString(R.string.ImportExportFragment_signal_needs_the_storage_permission_in_order_to_read_from_external_storage_but_it_has_been_permanently_denied))
                .onAllGranted(() -> {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    String[] mimeTypes = {"image/jpeg", "image/png"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), GALLERY_REQUEST);
                })
                .onAnyDenied(() -> Snackbar.make(parent, R.string.ImportExportFragment_signal_needs_the_storage_permission_in_order_to_read_from_external_storage, Snackbar.LENGTH_SHORT).show())
                .execute();
    }

    private void pickFromCamera() {
        Permissions.with(KPLaporanBaruActivity.this)
                .request(Manifest.permission.CAMERA)
                .ifNecessary()
                .withRationaleDialog(getString(R.string.ConversationActivity_to_capture_photos_and_video_allow_signal_access_to_the_camera), R.drawable.ic_photo_camera_white_48dp)
                .withPermanentDenialDialog(getString(R.string.ConversationActivity_signal_needs_the_camera_permission_to_take_photos_or_video))
                .onAllGranted(() -> {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        try {
                            Uri photoURI;
                            File photoFile = KPHelper.createImageFile(KPLaporanBaruActivity.this);
                            currentPhotoPath = photoFile.getAbsolutePath();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                photoURI = FileProvider.getUriForFile(this,
                                        "com.example.android.fileprovider",
                                        photoFile);
                            } else {
                                photoURI = Uri.fromFile(photoFile);
                            }

                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                        } catch (IOException ex) {
                            Log.d(TAG, "pickFromCamera: error");
                        }
                    }
                })
                .onAnyDenied(() -> Snackbar.make(parent, R.string.ConversationActivity_signal_needs_camera_permissions_to_take_photos_or_video, Snackbar.LENGTH_SHORT).show())
                .execute();
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

    private void previewImage() {
        Bundle bundle = new Bundle();
        bundle.putInt("resourceId", R.drawable.contoh);

        FragmentManager fragmentManager = getSupportFragmentManager();
        PreviewImageFragment previewFragment = new PreviewImageFragment();
        previewFragment.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, previewFragment).addToBackStack(null).commit();
    }

    private void closeButton(boolean show) {
        bClose.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            imageC1.setImageBitmap(null);
            imageC1.setImageDrawable(null);
            focusView = imageC1;
            currentPhotoPath = null;
        }
    }

    private boolean isValidFormData() {
        tps = eTps.getText().toString().trim();
        count1 = eCount1.getText().toString().trim();
        count2 = eCount2.getText().toString().trim();
        sah = eSah.getText().toString().trim();
        tidakSah = eTidakSah.getText().toString().trim();

        boolean cancel = false;

        if (selectedProvinsi == null) {
            eProvinsi.setError("Harus diisi");
            focusView = eProvinsi;
            cancel = true;
        }

        if (selectedKabupaten == null) {
            eKabupaten.setError("Harus diisi");
            focusView = eKabupaten;
            cancel = true;
        }

        if (selectedKecamatan == null) {
            eKecamatan.setError("Harus diisi");
            focusView = eKecamatan;
            cancel = true;
        }

        if (selectedKelurahan == null) {
            eKelurahan.setError("Harus diisi");
            focusView = eKelurahan;
            cancel = true;
        }

        if (TextUtils.isEmpty(tps)) {
            eTps.setError("Harus diisi");
            focusView = eTps;
            cancel = true;
        } else {
            int iTps = Integer.parseInt(tps);
            tps = String.valueOf(iTps);
            if (iTps < 1) {
                eTps.setError("Minimum 1");
                focusView = eTps;
                cancel = true;
            } else if (selectedKelurahan != null && iTps > selectedKelurahan.getJmltps()) {
                eTps.setError("Maksimum " + selectedKelurahan.getJmltps());
                focusView = eTps;
                cancel = true;
            }
        }

        if (TextUtils.isEmpty(count1)) {
            eCount1.setError("Harus diisi");
            focusView = eCount1;
            cancel = true;
        }

        if (TextUtils.isEmpty(count2)) {
            eCount2.setError("Harus diisi");
            focusView = eCount2;
            cancel = true;
        }

        if (TextUtils.isEmpty(sah)) {
            eSah.setError("Harus diisi");
            focusView = eSah;
            cancel = true;
        }

        if (TextUtils.isEmpty(tidakSah)) {
            eTidakSah.setError("Harus diisi");
            focusView = eTidakSah;
            cancel = true;
        }

        if (cancel) {
            Snackbar.make(parent, "Data tidak valid, silahkan periksa kembali", Snackbar.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void showDaerahProgress(String type, boolean show) {
        Button button;
        ProgressBar progress;
        String message;

        if (type.contains(KEY_PROVINSI)) {
            button = eProvinsi;
            progress = pProvinsi;
            message = show ? "Mengambil data Provinsi ..." : "Data Provinsi tersedia";
        } else if (type.contains(KEY_KABUPATEN)) {
            button = eKabupaten;
            progress = pKabupaten;
            message = show ? "Mengambil data Kabupaten/Kota ..." : "Data Kabupaten/Kota tersedia";
        } else if (type.contains(KEY_KECAMATAN)) {
            button = eKecamatan;
            progress = pKecamatan;
            message = show ? "Mengambil data Kecamatan ..." : "Data Kecamatan tersedia";
        } else {
            button = eKelurahan;
            progress = pKelurahan;
            message = show ? "Mengambil data Kelurahan/Desa ..." : "Data Kelurahan tersedia";
        }

        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, (show) ? 0 : R.drawable.ic_keyboard_arrow_down_dark_24dp, 0);
        progress.setVisibility((show) ? View.VISIBLE : View.GONE);
        Snackbar.make(parent, message, Snackbar.LENGTH_SHORT).show();
    }

    private void forceLogout() {
        KPHelper.clearCredentials(context);
        Intent intent = new Intent(context, KPRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void setResultAndFinish(int resultCode) {
        if (shortcut) {
            Intent intent = new Intent(context, KPLaporanActivity.class);
            intent.putExtra("resultCode", resultCode);
            startActivity(intent);
        } else {
            setResult(resultCode);
        }
        finish();
    }

    private class GetDaerahTask extends AsyncTask<Void, Void, Object> {
        private final String mType;
        private final String mParams;

        GetDaerahTask(String type, String param) {
            mType = type;
            mParams = param;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            daerahProses = true;
            showDaerahProgress(mType, true);
        }

        @Override
        protected Object doInBackground(Void... voids) {
            final KPRestInterface rest = KPRestClient.getClient(context).create(KPRestInterface.class);

            Call<KPDaerah> call = rest.getDaerah(mType, mParams);
            try {
                Response<KPDaerah> response = call.execute();
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
        protected void onPostExecute(Object obj) {
            super.onPostExecute(obj);
            if (obj != null) {
                if (obj instanceof KPDaerah) {
                    KPDaerah daerah = (KPDaerah) obj;
                    if (daerah.getStatus().equals("OK")) {
                        List<KPDaerah.ItemDaerah> items = daerah.getData();
                        if (mType.contains(KEY_PROVINSI)) {
                            itemsProvinsi = items;
                        } else if (mType.contains(KEY_KABUPATEN)) {
                            itemsKabupaten = items;
                        } else if (mType.contains(KEY_KECAMATAN)) {
                            itemsKecamatan = items;
                        } else {
                            itemsKelurahan = items;
                        }
                        daerahDatabase.insertDaerah(items, mParams);
                        showDaerahProgress(mType, false);
                        daerahProses = false;
                    }
                } else if (obj instanceof KPAuthException) {
                    forceLogout();
                } else if (obj instanceof IOException) {
                    Snackbar.make(parent, "Connectivity error", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(parent, "Unknown error", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void gagalKirimLaporan(String reason, KPLaporan laporan) {
        Snackbar.make(parent, reason + " - Anda dapat menyimpan laporan untuk dikirim kemudian.", Snackbar.LENGTH_INDEFINITE)
                .setAction("SIMPAN", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        laporan.setType(RETRY);
                        laporanDatabase.addOrUpdateLaporan(laporan);
                        setResultAndFinish(RESULT_FIRST_USER);
                    }
                }).show();
    }

    private class SubmitLaporanTask extends AsyncTask<Void, Void, Object> {
        private KPLaporan laporan;
        private String c1Path;
        private String cType;

        SubmitLaporanTask(KPLaporan laporan) {
            this.laporan = laporan;
            this.c1Path = laporan.getAttachmentIdBase64();
            this.cType = KPHelper.getCType(c1Path);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
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
            showProgress(false);

            if (obj != null) {
                if (obj instanceof KPStatus) {
                    KPStatus post = (KPStatus) obj;
                    if (post.getStatus().equals("OK")) {
                        laporan.setType(OK);
                        laporanDatabase.addOrUpdateLaporan(laporan);
                        setResultAndFinish(RESULT_OK);
                    } else if (post.getStatus().equals("ERROR")) {
                        Snackbar.make(parent, post.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                } else if (obj instanceof KPAuthException) {
                    forceLogout();
                } else if (obj instanceof IOException) {
                    gagalKirimLaporan("Connectivity error", laporan);
                }
            } else {
                gagalKirimLaporan("Unknown error", laporan);
            }
        }
    }

}
