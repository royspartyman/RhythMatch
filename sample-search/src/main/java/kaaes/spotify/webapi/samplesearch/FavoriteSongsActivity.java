package kaaes.spotify.webapi.samplesearch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class FavoriteSongsActivity extends AppCompatActivity {

    static final String EXTRA_TOKEN = "EXTRA_TOKEN";
    private List<PlaylistTrack> playlistTrackList = null;
    private ArrayList<Track> trackList = new ArrayList<>();
    private List<Track> topTracks = new ArrayList<>();
    private RecyclerView recyclerView;
    private NewSongAdapter mAdapter;
    private SpotifyService spotify;
    private String token;

    /*@BindViews({R.id.newSongName1,R.id.newSongName2,R.id.newSongName3,R.id.newSongName4,R.id.newSongName5,R.id.newSongName6, R.id.newSongName7})
    List<TextView> textViews;

    @BindViews({R.id.newSongPicture1,R.id.newSongPicture2,R.id.newSongPicture3,R.id.newSongPicture4,R.id.newSongPicture5,R.id.newSongPicture6, R.id.newSongPicture7})
    List<ImageView> imageViews;*/

    @OnClick(R.id.back)
    public void onBackPressed(){
        Intent intent = TestActivity.createIntent(this);
        intent.putExtra(TestActivity.EXTRA_TOKEN, token);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_songs);
        ButterKnife.bind(this);

        RealmManager realmManager = new RealmManager(this);
        User user = realmManager.getUser();

        Intent intent = getIntent();
        token = intent.getStringExtra(EXTRA_TOKEN);

        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(token);
        spotify = api.getService();

        spotify.getPlaylistTracks(user.getUsername(), user.getPlaylistId(), new Callback<Pager<PlaylistTrack>>() {
            @Override
            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                playlistTrackList = playlistTrackPager.items;
                for(PlaylistTrack playlistTrack : playlistTrackList){
                    Track track = playlistTrack.track;
                    trackList.add(track);
                }

                mAdapter = new NewSongAdapter(trackList, getApplicationContext());
                recyclerView = (RecyclerView) findViewById(R.id.otherSongs);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });


    }
}
