package com.vit.demoyoutubeapi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoYoutubeAdapter extends BaseAdapter{

    private Context mContext;
    private int mLayout;
    private List<VideoYoutube> mVideoList;

    public VideoYoutubeAdapter(Context mContext, int mLayout, List<VideoYoutube> mVideoList) {
        this.mContext = mContext;
        this.mLayout = mLayout;
        this.mVideoList = mVideoList;
    }

    @Override
    public int getCount() {
        return mVideoList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder{
        ImageView imageThumbnail;
        TextView textTitle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mLayout, null);
            holder.textTitle = convertView.findViewById(R.id.text_title);
            holder.imageThumbnail = convertView.findViewById(R.id.image_thumbnail);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        VideoYoutube video = mVideoList.get(position);

        holder.textTitle.setText(video.getTitle());
        Picasso.get().load(video.getThumbnail()).into(holder.imageThumbnail);

        return convertView;
    }
}
