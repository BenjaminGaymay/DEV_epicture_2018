package eu.epitech.benjamin.epicture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    //ImageView image;
    Bundle bundle;
    FloatingActionButton fabCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bundle = getIntent().getExtras();

        fabCamera = findViewById(R.id.fab_camera);
        if (checkCameraHardware(this)) {
            fabCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    try {
                        startActivityForResult(cameraIntent, 1337);
                    } catch (Exception e) {
                        Log.e("openCamera", e.getMessage());
                    }
                }
            });
        } else {
            fabCamera.hide();
            Utilities.displayAlertDialog(this, getResources().getString(R.string.error_camera_disabled));
        }


        Toolbar toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            HomeFragment hf = new HomeFragment();
            hf.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    hf).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        View headerView = navigationView.getHeaderView(0);

        TextView navUsername = (TextView) headerView.findViewById(R.id.account_username);
        String accountUsername = bundle.get("account_username").toString();
        navUsername.setText(accountUsername);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                HomeFragment hf = new HomeFragment();
                hf.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        hf).commit();
                break;
            case R.id.nav_gallery:
                GalleryFragment gf = new GalleryFragment();
                gf.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        gf).commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private String get64BaseImage(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1337 && resultCode == RESULT_OK) {
            final String err_message = getResources().getString(R.string.error_upload_image);
            final AlertDialog.Builder builder = Utilities.createAlertDialogBuilder(this, err_message);
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            String url = "https://api.imgur.com/3/image";
            String b64 = null;
            RequestQueue queue = Volley.newRequestQueue(this);
            String uid = UUID.randomUUID().toString().substring(0, 5);
            if (imageBitmap != null) {
                b64 = get64BaseImage(imageBitmap);
            } else {
                Utilities.displayAlertDialog(this, getResources().getString(R.string.error_upload_image));
                return;
            }
            if (b64 == null ) {
                Utilities.displayAlertDialog(this, getResources().getString(R.string.error_upload_image));
                return;
            }
            JSONObject obj = new JSONObject();
            try {
                obj.put("image", b64);
                obj.put("type", "base64");
                obj.put("title", uid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            CharSequence text = "Image upload with success";
            int duration = Toast.LENGTH_SHORT;
            final Toast toast = Toast.makeText(this, text, duration);
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, url, obj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            toast.show();
                            GalleryFragment gf = new GalleryFragment();
                            gf.setArguments(bundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                    gf).commit();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("VOLLEY", error.toString());
                            builder.setMessage(err_message + "\n" + error.toString());
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    String accessToken = bundle.get("access_token").toString();
                    String clientId = bundle.get("account_id").toString();
                    headers.put("Authorization", "Bearer " + accessToken);
                    return headers;
                }
            };
            queue.add(jsonReq);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public Bundle getUserDatas() {
        return bundle;
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}
