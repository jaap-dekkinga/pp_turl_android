/*
 * Source: https://github.com/bumptech/glide/wiki/Custom-targets#palette-example
 */

package com.dekidea.hearact.core.glide;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;

public class PaletteBitmap {
    public final Palette palette;
    public final Bitmap bitmap;

    public PaletteBitmap(@NonNull Bitmap bitmap, Palette palette) {
        this.bitmap = bitmap;
        this.palette = palette;
    }
}