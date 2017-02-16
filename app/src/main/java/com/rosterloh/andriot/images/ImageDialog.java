package com.rosterloh.andriot.images;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.rosterloh.andriot.R;

public class ImageDialog extends DialogFragment {

    private final static String PARAM_BMP = "param_bmp";
    private Bitmap image;

    public static ImageDialog getInstance(final Bitmap image) {
        final ImageDialog fragment = new ImageDialog();

        final Bundle args = new Bundle();
        if (image != null)
            args.putParcelable(PARAM_BMP, image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        this.image = args.getParcelable(PARAM_BMP);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.image_frag, null);
        final ImageView imageView = (ImageView) dialogView.findViewById(R.id.iv_image);
        imageView.setImageBitmap(image);
        builder.setTitle(R.string.image_title);
        return builder.setView(dialogView).create();
    }
}
