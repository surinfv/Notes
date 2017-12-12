package com.fed.notes.utils;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fed.notes.R;


/**
 * Created by f on 10.04.2017.
 */

public class ImageDialog extends DialogFragment {
    private static final String IMAGEPATH = "imagepath";

    public static ImageDialog newInstance(String path) {
        Bundle args = new Bundle();
        args.putSerializable(IMAGEPATH, path);
        ImageDialog imageDialog = new ImageDialog();
        imageDialog.setArguments(args);
        return imageDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.image_dialog);

        String path = getArguments().getSerializable(IMAGEPATH).toString();
        Bitmap bitmap = PictureUtils.getScaledBitmap(path, getActivity());

        ImageView imageView = dialog.findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
        imageView.setOnClickListener(v -> dismiss());

        dialog.show();
        return dialog;
    }

    // fullscreen dialog
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
