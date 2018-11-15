package eu.epitech.benjamin.epicture;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.TransactionTooLargeException;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.Holder> {
    private ArrayList<HashMap<String, String>> mItems;
    private String accessToken;
    private View mView;
    ArrayList<String> favImages;

    MyAdapter(ArrayList<HashMap<String, String>> Items, String AccessToken, ArrayList<String> fav) {
        mItems = Items;
        accessToken = AccessToken;
        favImages = fav;
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ImageView favImage;

        String id;
        String accessToken;
        Boolean isFav;

        public void setId(String Id) {
            id = Id;
        }
        public void setFav(Boolean b) {
            isFav = b;
        }
        public void setAccessToken(String a) {
            accessToken = a;
        }
        public Holder(View itemView) {
            super(itemView);
            isFav = false;
            imageView = (ImageView)itemView.findViewById(R.id.iv_photo);
            favImage = itemView.findViewById(R.id.iv_fav);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                        String filename = Utilities.createImageFromBitmap(v.getContext(), bitmap);
                        Intent intent = new Intent(v.getContext(), ImageViewActivity.class);
                        intent.putExtra("filename", filename);
                        v.getContext().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utilities.displayAlertDialog(v.getContext(), "Can't open image in fullscreen");
                    }
                }
            });

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String url = "https://api.imgur.com/3/image/" + id + "/favorite";

                    CharSequence txt = "Image favorited";
                    final Toast toast = Toast.makeText(v.getContext(), txt, Toast.LENGTH_SHORT);
                    final String errMessage = v.getResources().getString(R.string.error_favorites);
                    final AlertDialog.Builder builder = Utilities.createAlertDialogBuilder(v.getContext(),
                            errMessage);
                    RequestQueue queue = Volley.newRequestQueue(v.getContext());
                    JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    CharSequence newTxt;
                                    if (!isFav) {
                                        newTxt = "Image put in the favorites";
                                    } else {
                                        newTxt = "Image deleted from the favorites";
                                    }
                                    toast.setText(newTxt);
                                    toast.show();
                                    setFav(!isFav);
                                    if (isFav) {
                                        favImage.setImageResource(R.drawable.ic_star_full);
                                    } else {
                                        favImage.setImageResource(R.drawable.ic_star_border);
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("VOLLEY", error.toString());
                                    builder.setMessage(errMessage + "\n" + error.toString());
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("Authorization", "Bearer " + _token);
                            return headers;
                        }
                        private String _token;
                        private JsonObjectRequest init(String token) {
                            _token = token;
                            return this;
                        }
                    }.init(accessToken);
                    queue.add(jsonReq);
                    return true;
                }
            });
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, final int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        Holder holder = new Holder(view);
        mView = view;
        return holder;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        String id = mItems.get(position).get("id");
        String url = mItems.get(position).get("link");

        holder.setId(id);
        holder.setAccessToken(accessToken);
        if (favImages.contains(id)) {
            holder.favImage.setImageResource(R.drawable.ic_star_full);
            holder.setFav(true);
        } else {
            holder.favImage.setImageResource(R.drawable.ic_star_border);
            holder.setFav(false);
        }

        Picasso.get().load(url).fit().centerCrop().into(holder.imageView, new Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(Exception e) {
                Log.d("skia", "ERROR");
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}