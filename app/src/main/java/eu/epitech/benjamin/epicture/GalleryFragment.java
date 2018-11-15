package eu.epitech.benjamin.epicture;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryFragment extends Fragment {
    String accessToken;
    String accountUsername;
    ListView listImages;
    JSONObject mResponse;
    View mView = null;
    RecyclerView recyclerView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle b = getArguments();

        mView = inflater.inflate(R.layout.fragment_gallery, container, false);
        accessToken = b != null ? b.get("access_token").toString() : null;
        accountUsername = b != null ? b.get("account_username").toString() : null;
        // listImages = mView.findViewById(R.id.list_images);
        recyclerView = mView.findViewById(R.id.recycler_images);
        getImages(mView);
        return mView;
    }

    private void displayImages(JSONObject response) {
        HashMap<String, String> map;
        recyclerView.setHasFixedSize(true);
        ArrayList<HashMap<String, String>> items = new ArrayList<>();

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        JSONArray array = null;
        try {
            array = response.getJSONArray("data");
            for (int i = 0 ; i < array.length() ; i++) {
                HashMap<String, String> item = new HashMap<>();

                String id = array.getJSONObject(i).getString("id");
                String link = array.getJSONObject(i).getString("link");
                String fav = array.getJSONObject(i).getString("favorite");

                item.put("id", id);
                item.put("link", link);
                item.put("fav", fav);
                items.add(item);
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
                        JSONArray array = null;
                        ArrayList<String> favs = new ArrayList<>();
                        try {
                            array = response.getJSONArray("data");
                            for (int i = 0; i < array.length(); i++) {
                                String id = array.getJSONObject(i).getString("id");
                                favs.add(id);
                            }
                            MyAdapter mAdapter = new MyAdapter(items, accessToken, favs);
                            recyclerView.setAdapter(mAdapter);

                        } catch (Exception e) {
                            Utilities.displayAlertDialog(getContext(), "Error while loading favorites");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                        Utilities.displayAlertDialog(mView.getContext(), "ERROR PD\n" + error.toString());
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

    private void getImages(View view) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "https://api.imgur.com/3/account/me/images";

        JSONObject obj = new JSONObject();
        try {
            obj.put("access_token", accessToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, url, obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mResponse = response;
                        displayImages(response);
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
            }
        };
        queue.add(jsonReq);
    }
}
