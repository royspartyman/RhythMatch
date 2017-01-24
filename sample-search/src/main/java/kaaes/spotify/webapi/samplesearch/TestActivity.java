package kaaes.spotify.webapi.samplesearch;

import com.daprlabs.aaron.swipedeck.SwipeDeck;
import com.github.clans.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.os.Handler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TestActivity extends AppCompatActivity {

    static final String EXTRA_TOKEN = "EXTRA_TOKEN";
    private static final String TAG = "MainActivity";
    private SwipeDeck cardStack;
    private Context context = this;
    private SwipeDeckAdapter adapter;
    private List<Track> trackList = null;
    private Handler handler = new Handler();
    private PreviewPlayer previewPlayer = new PreviewPlayer();
    private String currentTrack = "";
    private SpotifyService spotify;
    private Boolean notPlaying = true;
    private Integer swipeCounter = 0;
    private String token;
    private genre genre;
    String userId = "";
    String playlistId = "";
    Map<String, Object> options = new HashMap<>();

    Map<String, Object> playlist = new HashMap<>();


    private enum sampleState {PLAYING, PAUSED, DONE}

    private enum genre {POP, DANCE, COUNTRY, CLASSICAL}

    private sampleState currentState;

    @BindView(R.id.progressBar1)
    ProgressBar progressBar;

    @BindView(R.id.sample_state)
    ImageView samplePlayerState;

    @BindView(R.id.button_left)
    ImageView buttonLeft;

    @BindView(R.id.button_right)
    ImageView buttonRight;

    @BindView(R.id.button_center)
    Button buttonCenter;

    @BindView(R.id.profile)
    ImageView profile;

    @BindView(R.id.my_liked_songs)
    ImageView myLikedSongs;

    @OnClick(R.id.button_left)
    public void onButtonLeftClick() {
        cardStack.swipeTopCardLeft(500);
    }

    @OnClick(R.id.button_right)
    public void onButtonRightClick() {
        cardStack.swipeTopCardRight(180);
    }

    @OnClick(R.id.button_center)
    public void onButtonCenterClick() {
        cardStack.unSwipeCard();
    }

    @OnClick(R.id.my_liked_songs)
    public void onMyLikedSongsClicked() {
        Intent intent = new Intent(this, FavoriteSongsActivity.class);
        intent.putExtra(FavoriteSongsActivity.EXTRA_TOKEN, token);
        previewPlayer.release();
        startActivity(intent);
    }

    @OnClick(R.id.sample_state)
    public void onSampleStateChangeClick() {
        String uri = "";
        int imageResource = 0;

        switch (currentState) {
            case PLAYING:
                currentState = sampleState.PAUSED;
                previewPlayer.pause();
                uri = "@drawable/ic_play_sample";
                imageResource = getResources().getIdentifier(uri, null, getPackageName());
                samplePlayerState.setImageResource(imageResource);
                handler.removeCallbacks(runnable);
                break;
            case PAUSED:
                currentState = sampleState.PLAYING;
                previewPlayer.resume();
                uri = "@drawable/ic_pause_sample";
                imageResource = getResources().getIdentifier(uri, null, getPackageName());
                samplePlayerState.setImageResource(imageResource);
                handler.postDelayed(runnable, 1000);
                break;
            case DONE:
                previewPlayer.play(currentTrack);
                uri = "@drawable/ic_pause_sample";
                imageResource = getResources().getIdentifier(uri, null, getPackageName());
                samplePlayerState.setImageResource(imageResource);
                progressBar.setProgress(0);
                currentState = sampleState.PLAYING;
                handler.postDelayed(runnable, 1000);
        }
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, TestActivity.class);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            progressBar.setProgress(progressBar.getProgress() + 1);
            if (progressBar.getProgress() >= 30) {
                currentState = sampleState.DONE;
                String uri = "@drawable/ic_play_sample";
                int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                samplePlayerState.setImageResource(imageResource);
                handler.removeCallbacks(runnable);
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        cardStack = (SwipeDeck) findViewById(R.id.swipe_deck);
        final RealmManager realmManager = new RealmManager(this);
        ButterKnife.bind(this);

        genre = genre.POP;


        Intent intent = getIntent();
        token = intent.getStringExtra(EXTRA_TOKEN);

        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(token);
        spotify = api.getService();

        Log.i("token", token);

        User user = realmManager.getUser();

        if(Objects.equals(user, null)){
            spotify.getMe(new Callback<UserPrivate>() {
                @Override
                public void success(final UserPrivate userPrivate, Response response) {

                    playlist.put("name", "RhythmMix");
                    playlist.put("public", "true");

                    spotify.createPlaylist(userPrivate.id, playlist, new Callback<Playlist>() {
                        @Override
                        public void success(Playlist playlist, Response response) {
                            Log.i("response: ", playlist.id);
                            realmManager.createStudent(playlist.id, userPrivate.id);
                            userId = userPrivate.id;
                            playlistId = playlist.id;
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.i("response: ", error.toString());
                        }
                    });
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }else{
            userId = user.getUsername();
            playlistId = user.getPlaylistId();
        }

        options.put("seed_genres", "dance");
        options.put("limit", "100");

        spotify.getRecommendations(options, new Callback<Recommendations>() {

            @Override
            public void success(final Recommendations recommendations, Response response) {
                trackList = recommendations.tracks;
                adapter = new SwipeDeckAdapter(trackList, getApplicationContext());

                if (cardStack != null) {
                    cardStack.setAdapter(adapter);
                }

                try {
                    if (previewPlayer.play(trackList.get(0).preview_url)) {
                        currentTrack = trackList.get(swipeCounter).name;
                        progressBar.setProgress(0);
                        currentState = sampleState.PLAYING;
                        handler.postDelayed(runnable, 1000);
                    }
                } catch (Exception ex) {
                    cardStack.swipeTopCardLeft(50);
                }
                setupCardStack();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("User success", error.toString());
            }

        });

    }

    private void setupCardStack() {

        cardStack.setCallback(new SwipeDeck.SwipeDeckCallback() {
            @Override
            public void cardSwipedLeft(long stableId) {
                previewPlayer.release();
                if(currentState == sampleState.PLAYING){
                    previewPlayer.release();
                }else{
                    String uri = "@drawable/ic_pause_sample";
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    samplePlayerState.setImageResource(imageResource);
                }
                handler.removeCallbacks(runnable);
                try {
                    if (previewPlayer.play(trackList.get((int) stableId + 1).preview_url)) {
                        currentTrack = trackList.get((int) stableId + 1).name;
                        progressBar.setProgress(0);
                        currentState = sampleState.PLAYING;
                        handler.postDelayed(runnable, 1000);
                    }
                } catch (Exception ex) {
                    cardStack.swipeTopCardLeft(100);
                }
            }

            @Override
            public void cardSwipedRight(final long stableId) {

                Map<String, Object> blank = new HashMap<>();
                Map<String, Object> addTracks = new HashMap<>();
                addTracks.put("uris", trackList.get((int)stableId).uri);

                spotify.addTracksToPlaylist(userId, playlistId, addTracks, addTracks, new SpotifyCallback<Pager<PlaylistTrack>>() {
                    @Override
                    public void failure(SpotifyError error) {

                    }

                    @Override
                    public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                    }
                });

                previewPlayer.release();
                if(currentState == sampleState.PLAYING){
                    previewPlayer.release();
                }else{
                    String uri = "@drawable/ic_pause_sample";
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    samplePlayerState.setImageResource(imageResource);
                }
                handler.removeCallbacks(runnable);
                try {
                    if (previewPlayer.play(trackList.get((int) stableId + 1).preview_url)) {
                        currentTrack = trackList.get((int) stableId + 1).name;
                        progressBar.setProgress(0);
                        currentState = sampleState.PLAYING;
                        handler.postDelayed(runnable, 1000);
                    }
                } catch (Exception ex) {
                    cardStack.swipeTopCardLeft(100);
                }
            }
        });
        cardStack.setLeftImage(R.id.right_image);
        cardStack.setRightImage(R.id.left_image);
    }


    public class SwipeDeckAdapter extends BaseAdapter {

        private List<Track> data;
        private Context context;

        public SwipeDeckAdapter(List<Track> data, Context context) {
            this.data = data;
            this.context = context;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = getLayoutInflater();
                v = inflater.inflate(R.layout.test_card2, parent, false);
            }
            //((TextView) v.findViewById(R.id.textView2)).setText(data.get(position));
            ImageView imageView = (ImageView) v.findViewById(R.id.offer_image);
            Picasso.with(context).load(data.get(position).album.images.get(0).url).fit().into(imageView);
            TextView textView = (TextView) v.findViewById(R.id.sample_text);
            Track track = data.get(position);
            textView.setText(track.name);

            return v;
        }
    }
}