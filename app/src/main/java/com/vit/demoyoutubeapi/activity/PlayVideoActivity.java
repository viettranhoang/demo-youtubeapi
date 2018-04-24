package com.vit.demoyoutubeapi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
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
import com.vit.demoyoutubeapi.utils.Constants;
import com.vit.demoyoutubeapi.R;
import com.vit.demoyoutubeapi.adpater.VideoYoutubeAdapter;
import com.vit.demoyoutubeapi.model.VideoYoutube;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class PlayVideoActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener, View.OnClickListener{

    private String mVideoId;
    private String mUrlGetJson;

    @BindView(R.id.view_youtube) YouTubePlayerView mViewYoutube;
    @BindView(R.id.list_relate_video) RecyclerView mListVideo;

    private ArrayList<VideoYoutube> mVideoYoutubeList;
    private VideoYoutubeAdapter mVideoYoutubeAdapter;

    int REQUEST_VIDEO = 12;


    private YouTubePlayer mPlayer;

    @BindView(R.id.video_control) View mPlayButtonLayout;
    @BindView(R.id.text_play_time) TextView mPlayTimeTextView;
    @BindView(R.id.seekbar_video) SeekBar mSeekBar;
    private Handler mHandler = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        ButterKnife.bind(this);

        mVideoId = getIntent().getStringExtra("videoId");
        mUrlGetJson = "https://www.googleapis.com/youtube/v3/search?part=snippet&relatedToVideoId="
                + mVideoId +"&type=video&key=" + Constants.API_KEY + "&maxResults=" + Constants.MAX_RESULTS;

        mViewYoutube.initialize(Constants.API_KEY, this);

        mVideoYoutubeList = new ArrayList<>();
        mVideoYoutubeAdapter = new VideoYoutubeAdapter(mVideoYoutubeList);
        mListVideo.setAdapter(mVideoYoutubeAdapter);
        mListVideo.setLayoutManager(new LinearLayoutManager(this));

        mVideoYoutubeAdapter.setOnItemClickListener(new VideoYoutubeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(PlayVideoActivity.this, PlayVideoActivity.class);
                intent.putExtra("videoId", mVideoYoutubeList.get(position).getVideoId());
                startActivity(intent);
            }
        });

        getJsonYoutube(mUrlGetJson);

        mSeekBar.setOnSeekBarChangeListener(mVideoSeekBarChangeListener);
        mHandler = new Handler();
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
                        Toast.makeText(PlayVideoActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
//        player.loadVideo(mVideoId);

        if (null == player) return;
        mPlayer = player;

        displayCurrentTime();

        // Start buffering
        if (!wasRestored) {
            player.cueVideo(mVideoId);
        }

        player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        mPlayButtonLayout.setVisibility(View.VISIBLE);

        // Add listeners to YouTubePlayer instance
        player.setPlayerStateChangeListener(mPlayerStateChangeListener);
        player.setPlaybackEventListener(mPlaybackEventListener);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        if (result.isUserRecoverableError()) {
            result.getErrorDialog(PlayVideoActivity.this, REQUEST_VIDEO);
        } else {
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_VIDEO) {
            mViewYoutube.initialize(Constants.API_KEY, PlayVideoActivity.this);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {

    }

    @OnClick(R.id.image_play_video)
    void onClickPlayVideo(View view) {
        if (null != mPlayer && !mPlayer.isPlaying())
            mPlayer.play();
    }

    @OnClick(R.id.image_pause_video)
    void onClickPauseVideo(View view) {
        if (null != mPlayer && mPlayer.isPlaying())
            mPlayer.pause();
    }

    YouTubePlayer.PlaybackEventListener mPlaybackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onBuffering(boolean arg0) {
        }

        @Override
        public void onPaused() {
            mHandler.removeCallbacks(runnable);
        }

        @Override
        public void onPlaying() {
            mHandler.postDelayed(runnable, 100);
            displayCurrentTime();
        }

        @Override
        public void onSeekTo(int arg0) {
            mHandler.postDelayed(runnable, 100);
        }

        @Override
        public void onStopped() {
            mHandler.removeCallbacks(runnable);
        }
    };

    YouTubePlayer.PlayerStateChangeListener mPlayerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onAdStarted() {
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason arg0) {
        }

        @Override
        public void onLoaded(String arg0) {
        }

        @Override
        public void onLoading() {
        }

        @Override
        public void onVideoEnded() {
        }

        @Override
        public void onVideoStarted() {
            displayCurrentTime();
        }
    };

    SeekBar.OnSeekBarChangeListener mVideoSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            long lengthPlayed = (mPlayer.getDurationMillis() * progress) / 100;
            mPlayer.seekToMillis((int) lengthPlayed);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void displayCurrentTime() {
        if (null == mPlayer) return;
        String formattedTime = formatTime(mPlayer.getDurationMillis() - mPlayer.getCurrentTimeMillis());
        mPlayTimeTextView.setText(formattedTime);
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        return (hours == 0 ? "--:" : hours + ":") + String.format("%02d:%02d", minutes % 60, seconds % 60);
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            displayCurrentTime();
            mHandler.postDelayed(this, 100);
        }
    };
}
