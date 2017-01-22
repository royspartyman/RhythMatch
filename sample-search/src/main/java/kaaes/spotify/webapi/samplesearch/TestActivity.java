package kaaes.spotify.webapi.samplesearch;

import com.daprlabs.aaron.swipedeck.SwipeDeck;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.os.Handler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
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
    private ArrayList<String> testData;
    private String userId = "";
    private List<Track> trackList = null;
    private Handler handler = new Handler();
    private PreviewPlayer previewPlayer = new PreviewPlayer();
    private String currentTrack = "";
    private SpotifyService spotify;

    private enum sampleState{PLAYING, PAUSED, DONE}
    private sampleState currentState;

    @BindView(R.id.progressBar1)
    ProgressBar progressBar;

    @BindView(R.id.sample_state)
    ImageView samplePlayerState;

    @OnClick(R.id.sample_state)
    public void onSampleStateChangeClick(){
        String uri = "";
        int imageResource = 0;

        switch (currentState){
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
      /* do what you need to do */
            progressBar.setProgress(progressBar.getProgress()+1);
      /* and here comes the "trick" */
            if(progressBar.getProgress() >= 30){
                currentState = sampleState.DONE;
                String uri = "@drawable/ic_play_sample";
                int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                samplePlayerState.setImageResource(imageResource);
                handler.removeCallbacks(runnable);
            }else{
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        cardStack = (SwipeDeck) findViewById(R.id.swipe_deck);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        final String token = intent.getStringExtra(EXTRA_TOKEN);

        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(token);
        spotify = api.getService();

        final Map<String, Object> options = new HashMap<>();
        options.put("seed_genres", "house");
        options.put("limit", "20");

        spotify.getRecommendations(options, new Callback<Recommendations>() {
            @Override
            public void success(Recommendations recommendations, Response response) {
                trackList = recommendations.tracks;
                adapter = new SwipeDeckAdapter(trackList, getApplicationContext());
                adapter.notifyDataSetChanged();

                for(Track track : trackList){
                    try{
                        previewPlayer.play(track.preview_url);
                        currentState = sampleState.PLAYING;
                        currentTrack = trackList.get(0).preview_url;
                        handler.postDelayed(runnable, 1000);
                        break;
                    }catch (Exception ex){
                        trackList.remove(0);
                        adapter.data = trackList;
                        adapter.notifyDataSetChanged();
                    }
                }


                if(cardStack != null){
                    cardStack.setAdapter(adapter);
                }
                cardStack.setCallback(new SwipeDeck.SwipeDeckCallback() {
                    @Override
                    public void cardSwipedLeft(long stableId) {
                        trackList.remove(0);
                        adapter.data.remove((int)stableId);
                        adapter.notifyDataSetChanged();
                        try{
                            if(trackList.size() <= 5) {
                                spotify.getRecommendations(options, new Callback<Recommendations>() {
                                    @Override
                                    public void success(Recommendations recommendations, Response response) {
                                        trackList.addAll(recommendations.tracks);
                                        adapter.data = trackList;
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {

                                    }
                                });
                            }
                            previewPlayer.play(adapter.data.get((int)stableId+1).preview_url);
                        }catch (Exception ex){
                        }
                    }

                    @Override
                    public void cardSwipedRight(long stableId) {
                        Log.i("MainActivity", "card was swiped right, position in adapter: " + stableId);

                    }

                });

                cardStack.setLeftImage(R.id.left_image);
                cardStack.setRightImage(R.id.right_image);

                ImageView btn = (ImageView) findViewById(R.id.button_left);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cardStack.swipeTopCardLeft(500);

                    }
                });
                ImageView btn2 = (ImageView) findViewById(R.id.button_right);
                btn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cardStack.swipeTopCardRight(180);
                    }
                });

                Button btn3 = (Button) findViewById(R.id.button_center);
                btn3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                testData.add("a sample string.");
//                adapter.notifyDataSetChanged();
                        cardStack.unSwipeCard();
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("User success", error.toString());
            }

        });

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
                // normally use a viewholder
                v = inflater.inflate(R.layout.test_card2, parent, false);
            }
            //((TextView) v.findViewById(R.id.textView2)).setText(data.get(position));
            ImageView imageView = (ImageView) v.findViewById(R.id.offer_image);
            Picasso.with(context).load(R.drawable.ic_hate_music).fit().centerCrop().into(imageView);
            TextView textView = (TextView) v.findViewById(R.id.sample_text);
            Track track = data.get(position);
            textView.setText(track.name);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Layer type: ", Integer.toString(v.getLayerType()));
                    Log.i("Hardware Accel type:", Integer.toString(View.LAYER_TYPE_HARDWARE));
                    /*Intent i = new Intent(v.getContext(), BlankActivity.class);
                    v.getContext().startActivity(i);*/
                }
            });
            return v;
        }
    }
}