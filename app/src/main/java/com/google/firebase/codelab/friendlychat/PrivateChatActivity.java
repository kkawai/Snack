package com.google.firebase.codelab.friendlychat;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.codelab.friendlychat.model.FriendlyMessage;
import com.initech.Constants;
import com.initech.api.NetworkApi;
import com.initech.model.User;
import com.initech.util.MLog;

import org.json.JSONObject;

/**
 * Created by kevin on 9/16/2016.
 * The difference between Private and Group Chat:
 * 1) Title - Name of person you are talking to you
 * 2) Realtime database reference
 * 3) Every message you post gets sent to the person you are talking to
 * so if they are not in the chat room they will receive an notification
 * 4) we need to fetch the user that we are talking to from network api
 */
public class PrivateChatActivity extends GroupChatActivity {

    private static final String TAG = "PrivateChatActivity";
    private User mToUser;
    private static boolean sIsActive;
    private static int sToUserid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MLog.d(TAG, "onCreate() ");
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MLog.d(TAG, "onNewIntent() ");
        getSupportActionBar().setTitle("");
        final int toUserid = getIntent().getIntExtra(Constants.KEY_USERID, 0);

        MLog.d(TAG, "onNewIntent() toUserid : " + toUserid);

        sToUserid = toUserid;
        NetworkApi.getUserById(this, toUserid, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    mToUser = User.fromResponse(response);
                    getSupportActionBar().setTitle(mToUser.getUsername());
                } catch (Exception e) {
                    Toast.makeText(PrivateChatActivity.this, getString(R.string.general_api_error, "1"), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MLog.e(TAG, "NetworkApi.getUserById(" + toUserid + ") failed in onCreate()", error);
                Toast.makeText(PrivateChatActivity.this, getString(R.string.general_api_error, "2"), Toast.LENGTH_SHORT).show();
            }
        });
        final NotificationManager notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        notificationManager.cancel(toUserid);
        MLog.d(TAG, "Cancelled notification " + toUserid);
    }

    @Override
    public void onResume() {
        sIsActive = true;
        super.onResume();
    }

    @Override
    public void onPause() {
        sIsActive = false;
        super.onPause();
    }

    @Override
    void initDatabaseRef() {
        int toUserid = getIntent().getIntExtra(Constants.KEY_USERID, 0);
        setDatabaseRef(Constants.PRIVATE_CHAT_REF(toUserid, myUserid()));
    }

    @Override
    void onFriendlyMessageSent(FriendlyMessage friendlyMessage) {
        try {
            JSONObject o = friendlyMessage.toJSONObject();
            NetworkApi.gcmsend("" + mToUser.getId(), o);
        } catch (Exception e) {
            MLog.e(TAG, "onFriendlyMessageSent() failed", e);
            Toast.makeText(PrivateChatActivity.this, getString(R.string.general_api_error, "3"), Toast.LENGTH_SHORT).show();
        }
    }

    public static void startPrivateChatActivity(Context context, int userid) {
        Intent intent = newIntent(context, userid);
        context.startActivity(intent);
    }

    public static Intent newIntent(Context context, int userid) {
        Intent intent = new Intent(context, PrivateChatActivity.class);
        intent.putExtra(Constants.KEY_USERID, userid);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        MLog.d(TAG, "instantiated intent with userid: " + userid);
        return intent;
    }

    public static boolean isActive() {
        return sIsActive;
    }

    public static int getActiveUserid() {
        return sToUserid;
    }
}
