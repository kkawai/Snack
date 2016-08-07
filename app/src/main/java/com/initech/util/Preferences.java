package com.initech.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.initech.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public final class Preferences {

    private static final Random RANDOM = new Random();

    private Preferences() {
    }

    private static final String PREFERENCE_USER_ID = "id";
    private static final String PREFERENCE_USER_NAME = "user_name";
    private static final String PREFERENCE_EMAIL = "email";
    private static final String PREFERENCE_USER = "user";
    private static final String PREFERENCE_LAST_SIGN_IN = "last_sign_in";
    private static final String INSTAGRAM_ACCESS_TOKEN = "accessToken";
    private static final String INSTAGRAM_STORE = "INSTAGRAM_STORE";

    private static final String PREFERENCE_ENCRYPTION = "msg_encryption";
    private static final String PREFERENCE_VIBRATE = "vibrate";
    private static final String PREFERENCE_SOUND = "sound";

    private static final int MAX_SAVED_TAGS = 15;
    private static final int MAX_SAVED_USERS = 5;
    public static final String PREFERENCES_SERVICE = "com.instachat.android.Preferences";
    private static final String PREFERENCE_ADVANCED_CAMERA_ENABLED = "advanced_camera_enabled";
    private static final String PREFERENCE_BORDERS_ENABLED = "borders_enabled";
    private static final String PREFERENCE_DOUBLE_TAP_TO_LIKE_HINT_IMPRESSIONS = "used_double_tap_hint_impressions";
    private static final String PREFERENCE_GEOTAG_ENABLED = "geotag_enabled";
    private static final String PREFERENCE_HAS_USED_DOUBLE_TAP_TO_LIKE = "used_double_tap";
    private static final String PREFERENCE_NEEDS_PHOTO_MAP_EDUCATION = "needs_photo_map_education";
    private static final String PREFERENCE_PUSH_REGISTRATION_DATE = "push_reg_date";
    private static final String PREFERENCE_RECENT_HASHTAG_SEARCHES = "recent_hashtag_searches";
    private static final String PREFERENCE_RECENT_USER_SEARCHES = "recent_user_searches";
    private static final String PREFERENCE_SYSTEM_MESSAGES = "system_message_";
    private static final String PREFERENCE_UNIQUE_ID = "unique_id";
    private static final String PREFERENCE_SHOW_VC_COUNT_MSG = "show_vc_count_msg";

    private static final String TAG = "Preferences";
    private static final long TWO_DAYS = 172800000L;
    private static final long HALF_DAY = 43200000L;
    private static String sUniqueID = null;
    private Context mContext;
    // private final ObjectMapper mObjectMapper;
    private SharedPreferences mPrefs;
    private static Preferences instance;

    public Preferences(Context context) {
        this.mContext = context;
        this.mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        // this.mObjectMapper = new ObjectMapper();
    }

    public static Preferences getInstance(final Context context) {
        if (instance == null)
            instance = new Preferences(context);
        return instance;
    }

    public boolean isEncryptionEnabled() {
        return this.mPrefs.getBoolean(PREFERENCE_ENCRYPTION, true);
    }

    public void setEncryptionEnabled(final boolean enable) {
        final Editor editor = mPrefs.edit();
        editor.putBoolean(PREFERENCE_ENCRYPTION, enable);
        ThreadWrapper.executeInWorkerThread(new Runnable() {
            @Override
            public void run() {
                editor.commit();
            }
        });
    }

    public boolean isVibrateEnabled() {
        return this.mPrefs.getBoolean(PREFERENCE_VIBRATE, true);
    }

    public void setVibrateEnabled(final boolean enable) {
        final Editor editor = mPrefs.edit();
        editor.putBoolean(PREFERENCE_VIBRATE, enable);
        ThreadWrapper.executeInWorkerThread(new Runnable() {
            @Override
            public void run() {
                editor.commit();
            }
        });
    }

    public boolean isSoundEnabled() {
        return this.mPrefs.getBoolean(PREFERENCE_SOUND, true);
    }

    public void setSoundEnabled(final boolean enable) {
        final Editor editor = mPrefs.edit();
        editor.putBoolean(PREFERENCE_SOUND, enable);
        ThreadWrapper.executeInWorkerThread(new Runnable() {
            @Override
            public void run() {
                editor.commit();
            }
        });
    }

    public ArrayList getRecentHashtagSearches() {
        return null; //TODO
    }

    public ArrayList getRecentUserSearches() {
        return null; //TODO
    }

    public void saveRecentHashtag(String hashtag) {
        //TODO
    }

    public void saveRecentUser(User user) {
        //TODO
    }

    public void clearUser(Editor editor) {
        editor.remove(PREFERENCE_USER).apply();
    }

    public String getUserId() {
        return this.mPrefs.getString(PREFERENCE_USER_ID, null);
    }

    public String getUsername() {
        return this.mPrefs.getString(PREFERENCE_USER_NAME, null);
    }

    public String getEmail() {
        return this.mPrefs.getString(PREFERENCE_EMAIL, null);
    }

    public User getUser() {
        if (!mPrefs.contains(PREFERENCE_USER)) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(mPrefs.getString(
                    PREFERENCE_USER, null));
            final User user = new User();
            user.copyFrom(json,null);
            return user;
        } catch (JSONException e) {
            // e.printStackTrace();
        }
        return null;
    }

    public void saveLastSignIn(final String lastSignIn) {
        MLog.i(TAG,"lastSignIn "+lastSignIn);
        mPrefs.edit().putString(PREFERENCE_LAST_SIGN_IN, lastSignIn).apply();
    }

    public String getLastSignIn() {
        return mPrefs.getString(PREFERENCE_LAST_SIGN_IN,null);
    }

    public void saveUser(final User user) {
        final Editor editor = mPrefs.edit();
        if (user == null) {
            clearUser(editor);
        } else {
            editor.putString(PREFERENCE_USER, user.toJSON().toString());
            editor.putInt(PREFERENCE_USER_ID, user.getId());
            editor.putString(PREFERENCE_USER_NAME, user.getUsername());
            editor.putString(PREFERENCE_EMAIL, user.getEmail());
        }
        editor.commit();
    }

    public String getAccessToken() {
        final String accessToken = this.mPrefs.getString(
                INSTAGRAM_ACCESS_TOKEN, null);
        MLog.i(Preferences.class.getSimpleName(), "Prefences.getAccessToken()="
                + accessToken);
        return this.mPrefs.getString(INSTAGRAM_ACCESS_TOKEN, null);
    }

    public void saveAccessToken(final String accessToken) {
        final Editor editor = this.mPrefs.edit();
        if (accessToken == null) {
            editor.remove(INSTAGRAM_ACCESS_TOKEN);
            MLog.i(Preferences.class.getSimpleName(),
                    "Prefences.saveAccessToken() removed access token because it's null..");
        } else {
            editor.putString(INSTAGRAM_ACCESS_TOKEN, accessToken);
            MLog.i(Preferences.class.getSimpleName(),
                    "Prefences.saveAccessToken() saved.." + accessToken);
        }
        editor.commit();
    }

    public void setVcCountShown() {
        final Editor editor = this.mPrefs.edit();
        editor.putBoolean(PREFERENCE_SHOW_VC_COUNT_MSG, true);
        editor.commit();
    }

    public boolean isVcCountShown() {
        return this.mPrefs.getBoolean(PREFERENCE_SHOW_VC_COUNT_MSG, false);
    }

    public boolean isNotifyEnabled() {
        return this.mPrefs.getBoolean("notify", true);
    }

    public void setNotifyEnabled(final Boolean newValue) {
        final Editor editor = this.mPrefs.edit();
        editor.putBoolean("notify", newValue);
        editor.commit();
    }

    public void incrementStarts() {
        final int starts = mPrefs.getInt("starts", 0);
        mPrefs.edit().putInt("starts", starts + 1).commit();
    }

    public int getStarts() {
        return mPrefs.getInt("starts", 1);
    }

    public void setRated(final boolean isRated) {
        mPrefs.edit().putBoolean("is_rated", isRated).commit();
    }

    public boolean isRated() {
        return mPrefs.getBoolean("is_rated", false);
    }

    public void locationUpdated() {
        final Editor editor = this.mPrefs.edit();
        editor.putLong("location_update", new Date().getTime());
        editor.commit();
    }

    public boolean locationNeedsUpdate() {
        final long last = mPrefs.getLong("location_update", 0);
        final long now = new Date().getTime();
        return now - last > HALF_DAY;
    }

    public String getGender() {
        return mPrefs.getString("gender", "");
    }

    public void setGender(final String gender) {
        mPrefs.edit().putString("gender", gender).commit();
    }

    public int getBannerAdCounter() {
        final int cur = mPrefs.getInt("bannerAdCounter", RANDOM.nextInt(9));
        int next = cur + 1;
        next = next > 100000 ? 1 : next; // reset after 100k requests
        mPrefs.edit().putInt("bannerAdCounter", next).commit();
        return cur;
    }

    public int getInterstitialAdCounter() {
        final int cur = mPrefs.getInt("interstitialAdCounter",
                RANDOM.nextInt(9));
        int next = cur + 1;
        next = next > 100000 ? 1 : next; // reset after 100k requests
        mPrefs.edit().putInt("interstitialAdCounter", next).commit();
        return cur;
    }

}
