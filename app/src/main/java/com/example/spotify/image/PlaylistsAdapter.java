package com.example.spotify.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.spotify.R;
import com.example.spotify.dto.SongDTO;

import java.util.List;

public class PlaylistsAdapter extends BaseAdapter {

    private List<SongDTO> songs;
    private Context context;

    public PlaylistsAdapter(List<SongDTO> songs, Context context) {
        this.songs = songs;
        this.context = context;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SongViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_playlist, parent, false);

            viewHolder = createSongViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (SongViewHolder) convertView.getTag();
        }

        final SongDTO currentItem = (SongDTO) getItem(position);

        if (currentItem != null) {
            bindSongToViewHolder(viewHolder, currentItem, position);
        }
        return convertView;
    }

    private SongViewHolder createSongViewHolder(View view){
        SongViewHolder viewHolder = new SongViewHolder();
        //viewHolder.mPlaylistBackgroundBanner = (ImageView) view.findViewById(R.id.)
        viewHolder.mPlaylistDetailSongImage = (ImageView) view.findViewById(R.id.playlist_detail_song_image);
        viewHolder.mPlaylistDetailSongName = (TextView) view.findViewById(R.id.playlist_detail_song_name);
        viewHolder.mPlaylistDetailSongArtist = (TextView) view.findViewById(R.id.playlist_detail_song_artist);
        return viewHolder;
    }

//    private void bindSongToViewHolder(final SongViewHolder viewHolder, SongDTO track, int indexInList)
//    {
//        viewHolder.mPlaylistDetailSongName.setText(track.getSongName());
//        viewHolder.mPlaylistDetailSongArtist.setText(track.getArtistName());
//        if(track.getTrackId() != null && !TextUtils.isEmpty(track.getTrackId())) {
//            viewHolder.mPlaylistDetailSongImage.setBackgroundResource(track.getTrackId());
//        }
//    }

    private void bindSongToViewHolder(final SongViewHolder viewHolder, SongDTO track, int indexInList) {
        viewHolder.mPlaylistDetailSongName.setText(track.getSongName());
        viewHolder.mPlaylistDetailSongArtist.setText(track.getArtistName());
        if (track.getTrackId() != null && !TextUtils.isEmpty(track.getTrackId())) {
            String imageUrl = track.getTrackId(); // 获取图片的URL
            Glide.with(context)
                    .load(imageUrl)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            viewHolder.mPlaylistDetailSongImage.setBackground(resource); // 设置背景图片
                        }
                    });
        }
    }

    class SongViewHolder {
//        ImageView mPlaylistBackgroundBanner;
//        TextView mPlaylistNameBanner;
//        ListView mPlaylistListview;
        ImageView mPlaylistDetailSongImage;
        TextView mPlaylistDetailSongName;
        TextView mPlaylistDetailSongArtist;
    }
}
