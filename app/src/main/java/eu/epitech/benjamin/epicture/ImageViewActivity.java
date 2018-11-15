package eu.epitech.benjamin.epicture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.FileNotFoundException;

public class ImageViewActivity extends AppCompatActivity {
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);

        bundle = getIntent().getExtras();
        try {
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeStream(openFileInput((String)bundle.get("filename")));
            ImageView img = findViewById(R.id.image_fullscreen);
            img.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
