package eu.epitech.benjamin.epicture;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    EditText searchInput;
    RecyclerView recyclerView;
    String accessToken;
    String accountUsername;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle b = getArguments();
        view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_images);
        accessToken = b != null ? b.get("access_token").toString() : null;
        accountUsername = b != null ? b.get("account_username").toString() : null;
        searchInput = view.findViewById(R.id.search_input);
        searchInput.clearFocus();
        searchInput.setSelected(false);
        if (!searchInput.getText().toString().equals(""))
            makeRequest(searchInput.getText().toString());
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    makeRequest(searchInput.getText().toString());
                    searchInput.clearFocus();
                }
                return false;
            }
        });

        return view;
    }

    private void displayImages(JSONObject response) {
        recyclerView.setHasFixedSize(true);
        ArrayList<HashMap<String, String>> items = new ArrayList<>();

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        try {
            JSONArray array = response.getJSONArray("data");
            for (int i = 0 ; i < array.length(); i++) {
                JSONArray post = array.getJSONObject(i).optJSONArray("images");
                if (post == null) {
                    HashMap<String, String> item = new HashMap<>();

                    String id = array.getJSONObject(i).getString("id");
                    String link = array.getJSONObject(i).getString("link");
                    String fav = array.getJSONObject(i).getString("favorite");

                    item.put("id", id);
                    item.put("link", link);
                    item.put("fav", fav);
                    if (array.getJSONObject(i).getString("type").contains("image/"))
                        items.add(item);
                } else {
                    for (int j = 0; j < post.length(); j++) {
                        HashMap<String, String> item = new HashMap<>();

                        String id = post.getJSONObject(j).getString("id");
                        String link = post.getJSONObject(j).getString("link");
                        String fav = post.getJSONObject(j).getString("favorite");

                        item.put("id", id);
                        item.put("link", link);
                        item.put("fav", fav);
                        if (post.getJSONObject(j).getString("type").contains("image/"))
                            items.add(item);
                    }
                }
            }
            fetchFavorites(items, accessToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchFavorites(final ArrayList<HashMap<String, String>> items, final String accessToken) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "https://api.imgur.com/3/account/" + accountUsername + "/favorites/0";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<String> favs = new ArrayList<>();
                        try {
                            JSONArray array = response.getJSONArray("data");
                            for (int i = 0; i < array.length(); i++) {
                                String id = array.getJSONObject(i).getString("id");
                                favs.add(id);
                            }
                            MyAdapter mAdapter = new MyAdapter(items, accessToken, favs);
                            /*recyclerView.setHasFixedSize(true);
                            recyclerView.setItemViewCacheSize(20);
                            recyclerView.setDrawingCacheEnabled(true);
                            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);*/
                            recyclerView.setAdapter(mAdapter);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }};
        queue.add(request);
    }

    public void makeRequest(String wantedPictures) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "https://api.imgur.com/3/gallery/search?q=" + wantedPictures;

        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        displayImages(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Client-ID " + getResources().getString(R.string.api_key));
                return headers;
            }
        };

        queue.add(jsonReq);
    }
}
