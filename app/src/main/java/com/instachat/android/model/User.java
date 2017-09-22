package com.instachat.android.model;

import android.content.ContentValues;

import com.google.firebase.database.ServerValue;
import com.google.gson.annotations.SerializedName;
import com.instachat.android.Constants;
import com.instachat.android.model.UserManager.UserColumns;
import com.instachat.android.util.MLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class User {

    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("email")
    private String email;

    @SerializedName("profilePicUrl")
    private String profilePicUrl;

    @SerializedName("bio")
    private String bio;

    private String instagramId;
    private long lastOnline;
    private String currentGroupName;
    private long currentGroupId;
    private int likes;

    public User() {
        super();
    }

    public User(ContentValues contentValues) {
        Integer idVal = contentValues.getAsInteger(UserColumns.ID);
        if (idVal != null)
            id = idVal.intValue();
        instagramId = contentValues.getAsString(UserColumns.INSTAGRAM_ID);
        username = contentValues.getAsString(UserColumns.USER_NAME);
        password = contentValues.getAsString(UserColumns.PASSWORD);
        email = contentValues.getAsString(UserColumns.EMAIL);
        profilePicUrl = contentValues.getAsString(UserColumns.PROFILE_PIC_URL);
    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();
        if (id != 0)
            values.put(UserColumns.ID, id);
        values.put(UserColumns.INSTAGRAM_ID, instagramId);
        values.put(UserColumns.USER_NAME, username);
        values.put(UserColumns.BIO, bio);
        values.put(UserColumns.EMAIL, email);
        values.put(UserColumns.PASSWORD, password);
        values.put(UserColumns.PROFILE_PIC_URL, profilePicUrl);
        return values;
    }

    /**
     * Return a map of only the attributes necessary for
     * indicating user presence in a group chat context
     *
     * @return
     */
    public Map<String, Object> toMap(boolean includeTimestamp) {
        Map<String, Object> map = new HashMap<>(10);
        map.put("username", username);
        if (profilePicUrl != null)
            map.put("profilePicUrl", profilePicUrl);
        map.put("id", id);
        if (bio != null)
            map.put("bio", bio);
        if (includeTimestamp)
            map.put(Constants.CHILD_LAST_ONLINE, ServerValue.TIMESTAMP);
        if (currentGroupName != null) {
            map.put(Constants.FIELD_CURRENT_GROUP_NAME, currentGroupName);
            map.put(Constants.FIELD_CURRENT_GROUP_ID, currentGroupId);
        }
        if (likes != 0) {
            map.put(Constants.CHILD_LIKES, likes);
        }
        return map;
    }

    public Map<String, Object> toMapForLikes() {
        Map<String, Object> map = new HashMap<>(1);
        map.put("username", username);
        if (profilePicUrl != null)
            map.put("profilePicUrl", profilePicUrl);
        map.put("id", id);
        if (likes != 0) {
            map.put(Constants.CHILD_LIKES, likes);
        }
        return map;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInstagramId() {
        return instagramId;
    }

    public String getUsername() {
        return username + "";
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio + "";
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        User other = (User) o;
        return other.id == id;
    }

    @Override
    public String toString() {
        return id + ", " + username + ", " + email;
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            if (username != null)
                object.put("username", username);
            if (password != null)
                object.put("password", password);
            if (email != null)
                object.put("email", email);
            if (bio != null)
                object.put("bio", bio);
            if (lastOnline != 0)
                object.put("lastOnline", lastOnline);
            if (currentGroupName != null)
                object.put("currentGroupName", currentGroupName);
            if (currentGroupId != 0)
                object.put("currentGroupId", currentGroupId);
        } catch (final JSONException e) {
            MLog.e("User", "", e);
        }
        return object;
    }

    public void copyFrom(JSONObject object) {
        id = object.optInt("id");
        username = object.optString("username");
        password = object.optString("password");
        email = object.optString("email");
        bio = object.optString("bio");
        profilePicUrl = object.optString("profilePicUrl");
        lastOnline = object.optLong("lastOnline");
        currentGroupName = object.optString("currentGroupName");
        currentGroupId = object.optLong("currentGroupId");
    }

    public static User fromResponse(JSONObject response) throws Exception {
        final JSONObject data = response.getJSONObject("data");
        final User remote = new User();
        remote.copyFrom(data);
        return remote;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public long getCurrentGroupId() {
        return currentGroupId;
    }

    public void setCurrentGroupId(long currentGroupId) {
        this.currentGroupId = currentGroupId;
    }

    public String getCurrentGroupName() {
        return currentGroupName;
    }

    public void setCurrentGroupName(String currentGroupName) {
        this.currentGroupName = currentGroupName;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void incrementLikes() {
        likes++;
    }

}
