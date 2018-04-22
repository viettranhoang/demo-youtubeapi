package com.vit.demoyoutubeapi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.edit_search) EditText mEditSearch;
    @BindView(R.id.button_search) Button mButtonSearch;
    @BindView(R.id.list_video) ListView mListVideo;

    private ArrayList<VideoYoutube> mVideoYoutubeList;
    private VideoYoutubeAdapter mVideoYoutubeAdapter;

    private String keyword;
    private String mUrlGetJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mVideoYoutubeList = new ArrayList<>();
        mVideoYoutubeAdapter = new VideoYoutubeAdapter(this, R.layout.item_video_youtube, mVideoYoutubeList);
        mListVideo.setAdapter(mVideoYoutubeAdapter);

        mListVideo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PlayVideoActivity.class);
                intent.putExtra("videoId", mVideoYoutubeList.get(position).getVideoId());
                startActivity(intent);
            }
        });
    }

    @OnClick(R.id.button_search)
    void onClickSearch(View view) {
        mVideoYoutubeList.clear();
        mVideoYoutubeAdapter.notifyDataSetChanged();

        keyword = mEditSearch.getText().toString();
        keyword = keyword.replace(' ', '+');
        mUrlGetJson = "https://www.googleapis.com/youtube/v3/search?part=snippet&q="+ keyword +"&type=video&key="
                + Constant.API_KEY + "&maxResults=" + Constant.MAX_RESULTS;

        getJsonYoutube(mUrlGetJson);
    }

    private void getJsonYoutube(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray jsonItems = response.getJSONArray("items");

                            String videoId = "", title = "", url = "";

                            for(int i = 0; i < jsonItems.length(); i++) {
                                JSONObject jsonItem = jsonItems.getJSONObject(i);

                                JSONObject jsonId = jsonItem.getJSONObject("id");
                                JSONObject jsonSnippet = jsonItem.getJSONObject("snippet");

                                videoId = jsonId.getString("videoId");
                                title = jsonSnippet.getString("title");

                                JSONObject jsonThumbnail = jsonSnippet.getJSONObject("thumbnails");
                                JSONObject jsonMedium = jsonThumbnail.getJSONObject("medium");
                                url = jsonMedium.getString("url");

                                mVideoYoutubeList.add(new VideoYoutube(title, url, videoId));
                            }

                            mVideoYoutubeAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

}
