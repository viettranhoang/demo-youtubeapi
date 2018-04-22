package com.vit.demoyoutubeapi.adpater;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vit.demoyoutubeapi.R;
import com.vit.demoyoutubeapi.model.VideoYoutube;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoYoutubeAdapter extends RecyclerView.Adapter<VideoYoutubeAdapter.ViewHolder>{

    private List<VideoYoutube> mVideoList;

    public VideoYoutubeAdapter(List<VideoYoutube> mVideoList) {
        this.mVideoList = mVideoList;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
    private static OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }



    @NonNull
    @Override //khoi tao view voi va Gan Item
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_video_youtube, parent, false);

        return new ViewHolder(view);
    }

    @Override //gan data vao view
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        VideoYoutube videoYoutube = mVideoList.get(position);

        holder.title.setText(videoYoutube.getTitle());
        Picasso.get().load(videoYoutube.getThumbnail()).into(holder.thumbnail);

    }

    @Override
    public int getItemCount() {
        return mVideoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.image_thumbnail) ImageView thumbnail;
        @BindView(R.id.text_title) TextView title;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(itemView, getLayoutPosition());
                    }
                }
            });
        }
    }
}
