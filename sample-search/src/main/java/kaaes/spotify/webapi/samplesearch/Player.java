package kaaes.spotify.webapi.samplesearch;

import android.support.annotation.Nullable;

public interface Player {

    Boolean play(String url);

    void pause();

    void resume();

    boolean isPlaying();

    @Nullable
    String getCurrentTrack();

    void release();
}
