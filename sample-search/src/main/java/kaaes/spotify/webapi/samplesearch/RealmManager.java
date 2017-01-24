package kaaes.spotify.webapi.samplesearch;

import android.content.Context;
import io.realm.Realm;

public class RealmManager {

    private Realm realm;

    public RealmManager(Context context) {
        Realm.init(context);
        realm = Realm.getDefaultInstance();
    }

    public void createStudent(String playlistId, String username) {
        User user = new User();
        user.setPlaylistId(playlistId);
        user.setUsername(username);
        realm.beginTransaction();
        realm.copyToRealm(user);
        realm.commitTransaction();
    }

    public User getUser() {
        return realm.where(User.class).findFirst();
    }

}
