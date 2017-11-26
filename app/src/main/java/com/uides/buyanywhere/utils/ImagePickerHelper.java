package com.uides.buyanywhere.utils;

import android.app.Activity;
import android.content.Intent;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.uides.buyanywhere.R;

import java.util.Collections;
import java.util.List;

/**
 * Created by TranThanhTung on 19/11/2017.
 */

public class ImagePickerHelper {

    public static void startImagePickerActivity(Activity activity, int requestCode) {
        new ImagePickerBuilder(activity)
                .withTitle(activity.getString(R.string.select_an_image))
                .withSingleSelectionMode()
                .build()
                .start(requestCode);
    }

    public static void startImagePickerActivity(Activity activity, int requestCode, int limit) {
        new ImagePickerBuilder(activity)
                .withTitle("Chọn " + limit + " ảnh")
                .withMultipleSelectionMode(limit)
                .build()
                .start(requestCode);
    }

    public static List<Image> getSelectedImage(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            return ImagePicker.getImages(data);
        }
        return Collections.emptyList();
    }
}
