package eu.epitech.benjamin.epicture;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

public class Utilities {
    static public AlertDialog.Builder createAlertDialogBuilder(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder;
    }

    static public void displayAlertDialog(Context context, String message) {
        AlertDialog.Builder builder = createAlertDialogBuilder(context, message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    static public String createImageFromBitmap(Context context, Bitmap bitmap) {
        String fileName = "myImage";//no .png or .jpg needed
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.flush();
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }
}
