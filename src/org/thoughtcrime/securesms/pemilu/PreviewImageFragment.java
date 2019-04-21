package org.thoughtcrime.securesms.pemilu;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import org.thoughtcrime.securesms.R;

public class PreviewImageFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.kp_preview_image_fragment, container, false);
        TextView title = view.findViewById(R.id.title);
        PhotoView photo = view.findViewById(R.id.photo);
        ImageButton close = view.findViewById(R.id.bt_close);
        KPHelper.darkenStatusBar(getActivity(), R.color.black);

        String pathImage = getArguments().getString("PathImage");
        int resourceId = getArguments().getInt("resourceId",0 );

        if (pathImage != null) {
            Glide.with(this).asBitmap().load(pathImage).into(photo);
            title.setText("Preview C1 Plano");
        }

        if (resourceId != 0) {
            Glide.with(this).asBitmap().load(resourceId).into(photo);
            title.setText("Contoh C1 Plano");
        }


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}