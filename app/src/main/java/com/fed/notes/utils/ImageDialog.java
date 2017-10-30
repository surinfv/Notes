package com.fed.notes.utils;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fed.notes.R;


/**
 * Created by f on 10.04.2017.
 */

public class ImageDialog extends DialogFragment {
    private static final String IMAGEPATH = "imagepath";

    private String mPath;
    private ImageView mImageView;

    public static ImageDialog newInstance(String path){
        Bundle args = new Bundle();
        args.putSerializable(IMAGEPATH, path);
        ImageDialog imageDialog = new ImageDialog();
        imageDialog.setArguments(args);
        return imageDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mPath = getArguments().getSerializable(IMAGEPATH).toString();

        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.image_dialog);

        Bitmap bitmap = PictureUtils.getScaledBitmap(mPath, getActivity());

        mImageView = dialog.findViewById(R.id.imageView);
        mImageView.setImageBitmap(bitmap);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        dialog.show();
        return dialog;
    }


    // делаем фулскрин диалог
    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
