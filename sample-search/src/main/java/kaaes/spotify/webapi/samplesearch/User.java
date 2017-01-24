package kaaes.spotify.webapi.samplesearch;

import io.realm.RealmObject;

public class User extends RealmObject {

    private String username;
    private String playlistId;

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPlaylistId() {
        return playlistId;
    }
}
