package kaaes.spotify.webapi.samplesearch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class NewSongAdapter extends RecyclerView.Adapter<NewSongAdapter.MyViewHolder> {

    private ArrayList<Track> trackList = new ArrayList<>();
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView photo;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.entity_title);
            photo = (ImageView) view.findViewById(R.id.entity_image);
        }
    }


    public NewSongAdapter(ArrayList<Track> trackList, Context context) {
        this.trackList = trackList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Track track = trackList.get(position);
        holder.title.setText(track.name);
        Picasso.with(context).load(track.album.images.get(0).url).fit().into(holder.photo);
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }
}