package com.instachat.android;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.instachat.android.api.NetworkApi;
import com.instachat.android.font.FontUtil;
import com.instachat.android.likes.UserLikedUserListener;
import com.instachat.android.model.User;
import com.instachat.android.util.AnimationUtil;
import com.instachat.android.util.MLog;
import com.instachat.android.util.Preferences;
import com.instachat.android.util.ScreenUtil;
import com.instachat.android.util.StringUtil;
import com.tooltip.Tooltip;

import org.json.JSONObject;

import java.util.concurrent.RejectedExecutionException;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by kevin on 9/4/2016.
 */
public class LeftDrawerHelper {
    private static final String TAG = "LeftDrawerHelper";
    private Activity mActivity;
    private DrawerLayout mDrawerLayout;
    private View mHeaderLayout;
    private MyProfilePicListener mMyProfilePicListener;
    private Tooltip mUsernameTooltip, mBioTooltip, mProfilePicTooltip;
    private ActivityState mActivityState;

    private EditText mBioEditText, mUsernameEditText;
    private ImageView mProfilePic;
    private View mSaveButton;
    private View mHelpButton;
    private View mDrawerLikesParent;
    private TextView mDrawerLikesCountView;
    private boolean mIsVirgin = true;
    private DatabaseReference mTotalLikesRef;
    private ValueEventListener mTotalLikesEventListener;
    private UserLikedUserListener mUserLikedUserListener;

    public LeftDrawerHelper(@NonNull Activity activity, @NonNull ActivityState activityState, @NonNull DrawerLayout drawerLayout, @NonNull MyProfilePicListener listener) {
        mActivity = activity;
        mActivityState = activityState;
        mDrawerLayout = drawerLayout;
        mMyProfilePicListener = listener;
    }

    public void updateProfilePic(final int userid, final String dpid) {
        if (mActivityState == null || mActivityState.isActivityDestroyed())
            return;
        Glide.clear(mProfilePic);
        try {
            Constants.DP_URL(userid, dpid, new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (mActivityState == null || mActivityState.isActivityDestroyed()) {
                        return;
                    }
                    try {
                        if (!task.isSuccessful()) {
                            mProfilePic.setImageResource(R.drawable.ic_anon_person_36dp);
                            return;
                        }
                        Glide.with(mActivity).load(task.getResult().toString()).error(R.drawable.ic_anon_person_36dp).crossFade().into(mProfilePic);
                    } catch (Exception e) {
                        MLog.e(TAG, "DP_URL failed", e);
                        mProfilePic.setImageResource(R.drawable.ic_anon_person_36dp);
                    }
                }
            });
        } catch (RejectedExecutionException e) {
        }
    }

    private void setupProfilePic(int userid, String profilePicUrl) {

        try {
            Constants.DP_URL(userid, profilePicUrl, new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (mActivityState == null || mActivityState.isActivityDestroyed())
                        return;
                    if (!task.isSuccessful()) {
                        mProfilePic.setImageResource(R.drawable.ic_anon_person_36dp);
                        return;
                    }
                    try {
                        Glide.with(mActivity).load(task.getResult().toString()).error(R.drawable.ic_anon_person_36dp).crossFade().into(mProfilePic);
                    } catch (Exception e) {
                        MLog.e(TAG, "onDrawerOpened() could not find user photo in google cloud storage", e);
                        mProfilePic.setImageResource(R.drawable.ic_anon_person_36dp);
                    }
                    checkForRemoteUpdatesToMyDP();
                }
            });
        } catch (RejectedExecutionException e) {
        }
    }

    private void hideTooltips() {
        if (mUsernameTooltip != null && mUsernameTooltip.isShowing())
            mUsernameTooltip.dismiss();
        if (mBioTooltip != null && mBioTooltip.isShowing())
            mBioTooltip.dismiss();
        if (mProfilePicTooltip != null && mProfilePicTooltip.isShowing())
            mProfilePicTooltip.dismiss();
    }

    private void showTooltips() {
        mUsernameTooltip = new Tooltip.Builder(mUsernameEditText, R.style.drawer_tooltip).setText(mActivity.getString(R.string.change_username_tooltip)).show();
        mBioTooltip = new Tooltip.Builder(mBioEditText, R.style.drawer_tooltip).setText(mActivity.getString(R.string.change_bio_tooltip)).show();
    }

    private int mWhichDrawerLastOpened;

    public void setup(NavigationView navigationView) {
        final User user = Preferences.getInstance().getUser();
        mHeaderLayout = navigationView.getHeaderView(0); // 0-index header
        mBioEditText = (EditText) mHeaderLayout.findViewById(R.id.input_bio);
        mUsernameEditText = (EditText) mHeaderLayout.findViewById(R.id.nav_username);
        mSaveButton = mHeaderLayout.findViewById(R.id.save_username);
        mDrawerLikesParent = mHeaderLayout.findViewById(R.id.drawerLikesParent);
        mDrawerLikesCountView = (TextView) mHeaderLayout.findViewById(R.id.drawerLikes);
        FontUtil.setTextViewFont(mDrawerLikesCountView);
        mHelpButton = mHeaderLayout.findViewById(R.id.help);
        setupUsernameAndBio();
        mProfilePic = (ImageView) mHeaderLayout.findViewById(R.id.nav_pic);
        mDrawerLikesParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUserLikedUserListener != null) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    mUserLikedUserListener.onMyLikersClicked();
                }
            }
        });
        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScreenUtil.hideKeyboard(mActivity);
                showChooseDialog();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTooltips();
                ScreenUtil.hideKeyboard(mActivity);
            }
        });
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

                if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                    mWhichDrawerLastOpened = GravityCompat.START;
                else
                    mWhichDrawerLastOpened = GravityCompat.END;

                if (mWhichDrawerLastOpened != GravityCompat.START)
                    return; //only handle left drawer logic

                MLog.d(TAG, "onDrawerOpened() LEFT drawer");
                ScreenUtil.hideKeyboard(mActivity);
                if (TextUtils.isEmpty(user.getProfilePicUrl())) {
                    mProfilePicTooltip = new Tooltip.Builder(mProfilePic, R.style.drawer_tooltip).setText(mActivity.getString(R.string.display_photo_tooltip)).show();
                }
                showViews(true);
                mSaveButton.setVisibility(View.GONE);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

                if (mWhichDrawerLastOpened != GravityCompat.START)
                    return; //only handle left drawer stuff in this module

                if (mActivityState == null || mActivityState.isActivityDestroyed())
                    return;

                MLog.d(TAG, "onDrawerClosed() LEFT drawer");

                ScreenUtil.hideKeyboard(mActivity);
                mSaveButton.setVisibility(View.GONE);

                showViews(false);
                hideTooltips();

                final String existingUsername = user.getUsername();
                final String newUsername = mUsernameEditText.getText().toString();

                final String existingBio = user.getBio();
                final String newBio = mBioEditText.getText().toString();

                /**
                 * check if username or bio has changed.  save if necessary.
                 */
                boolean usernameChanged = false, bioChanged = false;

                if (!existingUsername.equals(newUsername))
                    usernameChanged = true;
                if (!existingBio.equals(newBio))
                    bioChanged = true;

                if (usernameChanged) {
                    if (!StringUtil.isValidUsername(newUsername)) {
                        mUsernameEditText.setText(existingUsername);
                        new SweetAlertDialog(mActivity, SweetAlertDialog.ERROR_TYPE).setContentText(mActivity.getString(R.string.invalid_username)).show();
                        return;
                    }
                }

                saveUser(user, newUsername, newBio, bioChanged, usernameChanged);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        mUsernameEditText.setText(user.getUsername());
        if (TextUtils.isEmpty(user.getBio())) {
            mBioEditText.setHint(R.string.hint_write_something_about_yourself);
        } else {
            mBioEditText.setText(user.getBio());
        }
        mHeaderLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ScreenUtil.hideKeyboard(mActivity);
                clearEditableFocus();
                return true;
            }
        });
        setupProfilePic(user.getId(), user.getProfilePicUrl());
        listenForUpdatedLikeCount(user.getId());
    }

    private void checkForRemoteUpdatesToMyDP() {
        NetworkApi.getUserById(null, Preferences.getInstance().getUserId(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject response) {
                try {
                    if (mActivityState == null || mActivityState.isActivityDestroyed())
                        return;
                    final String status = response.getString(NetworkApi.KEY_RESPONSE_STATUS);
                    if (status.equalsIgnoreCase(NetworkApi.RESPONSE_OK)) {
                        final User remote = User.fromResponse(response);
                        if (!TextUtils.isEmpty(remote.getProfilePicUrl())) {
                            User local = Preferences.getInstance().getUser();
                            if (TextUtils.isEmpty(local.getProfilePicUrl()) || !remote.getProfilePicUrl().equals(local.getProfilePicUrl())) {
                                try {
                                    Constants.DP_URL(remote.getId(), remote.getProfilePicUrl(), new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (!task.isSuccessful() || mActivityState.isActivityDestroyed()) {
                                                return;
                                            }
                                            User user = Preferences.getInstance().getUser();
                                            user.setProfilePicUrl(remote.getProfilePicUrl());
                                            Preferences.getInstance().saveUser(user);

                                            MLog.i(TAG, "checkForRemoteUpdatesToMyDP() my pic changed remotely. attempt to update");
                                            try {
                                                Glide.with(mActivity).load(task.getResult().toString()).error(R.drawable.ic_anon_person_36dp).crossFade().into(mProfilePic);
                                            } catch (Exception e) {
                                                MLog.e(TAG, "onDrawerOpened() could not find my photo in google cloud storage", e);
                                            }
                                        }
                                    });
                                } catch (RejectedExecutionException e) {
                                }
                            } else {
                                MLog.i(TAG, "checkForRemoteUpdatesToMyDP() my pic did not change remotely. do not update.");
                            }
                        }
                    }
                } catch (final Exception e) {
                    MLog.e(TAG, "checkIfOtherDeviceUpdatedProfilePic(1) failed", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                MLog.e(TAG, "checkForRemoteUpdatesToMyDP(2) failed: " + error);
            }
        });
    }

    public void cleanup() {
        mActivityState = null;
        mActivity = null;
        mDrawerLayout = null;
        mMyProfilePicListener = null;
        mUserLikedUserListener = null;
        if (mTotalLikesRef != null && mTotalLikesEventListener != null)
            mTotalLikesRef.removeEventListener(mTotalLikesEventListener);
    }

    private void showChooseDialog() {
        if (mActivityState == null || mActivityState.isActivityDestroyed())
            return;
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        // Get the layout inflater
        final LayoutInflater inflater = mActivity.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.dialog_picture_choose, null);
        builder.setView(view);
        builder.setCancelable(true);
        final AlertDialog dialog = builder.create();

        view.findViewById(R.id.menu_choose_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                mMyProfilePicListener.onProfilePicChangeRequest(true);
            }
        });
        view.findViewById(R.id.menu_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                mMyProfilePicListener.onProfilePicChangeRequest(false);
            }
        });
        dialog.show();
    }

    private void showProfileUpdatedDialog() {
        new SweetAlertDialog(mActivity, SweetAlertDialog.SUCCESS_TYPE).setTitleText(mActivity.getString(R.string.your_profile_has_been_updated_title)).setContentText(mActivity.getString(R.string.your_profile_has_been_updated_msg)).show();
        mSaveButton.setVisibility(View.GONE);
    }

    private void saveUser(final User user, final String newUsername, final String newBio, final boolean needToSaveBio, final boolean needToSaveUsername) {
        if (needToSaveBio && !needToSaveUsername) {
            user.setBio(newBio);
            NetworkApi.saveUser(mActivity, user, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    MLog.d(TAG, "response: ", response);
                    if (mActivityState == null || mActivityState.isActivityDestroyed())
                        return;
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getString(NetworkApi.KEY_RESPONSE_STATUS).equals(NetworkApi.RESPONSE_OK)) {
                            Preferences.getInstance().saveUser(user);
                            showProfileUpdatedDialog();
                        }
                    } catch (Exception e) {
                        MLog.e(TAG, "", e);
                        Toast.makeText(mActivity, mActivity.getString(R.string.general_api_error, "(user1)"), Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    MLog.d(TAG, "error response: ", error);
                    Toast.makeText(mActivity, mActivity.getString(R.string.general_api_error, "(user2)"), Toast.LENGTH_SHORT).show();
                }
            });

        } else if (needToSaveUsername) {
            if (needToSaveBio)
                user.setBio(newBio);
            NetworkApi.isExistsUsername(mActivity, newUsername, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    if (mActivityState == null || mActivityState.isActivityDestroyed())
                        return;
                    try {
                        if (!response.getJSONObject("data").getBoolean("exists")) {
                            user.setUsername(newUsername);
                            NetworkApi.saveUser(mActivity, user, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    MLog.d(TAG, "response: ", response);
                                    if (mActivityState == null || mActivityState.isActivityDestroyed())
                                        return;
                                    try {
                                        JSONObject object = new JSONObject(response);
                                        if (object.getString(NetworkApi.KEY_RESPONSE_STATUS).equals(NetworkApi.RESPONSE_OK)) {
                                            user.setUsername(newUsername);
                                            Preferences.getInstance().saveUser(user);
                                            Preferences.getInstance().saveLastSignIn(newUsername);
                                            showProfileUpdatedDialog();
                                        }
                                    } catch (Exception e) {
                                        mUsernameEditText.setText(Preferences.getInstance().getUsername());
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    MLog.d(TAG, "error response: ", error);
                                    mUsernameEditText.setText(Preferences.getInstance().getUsername());
                                }
                            });

                        } else {
                            new SweetAlertDialog(mActivity, SweetAlertDialog.ERROR_TYPE).setContentText(mActivity.getString(R.string.username_exists, newUsername)).show();
                            mUsernameEditText.setText(Preferences.getInstance().getUsername());
                        }

                    } catch (Exception e) {
                        mUsernameEditText.setText(Preferences.getInstance().getUsername());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (mActivityState == null || mActivityState.isActivityDestroyed())
                        return;
                    mUsernameEditText.setText(Preferences.getInstance().getUsername());
                }
            });
        }

    }

    private void clearEditableFocus() {
        mUsernameEditText.setFocusableInTouchMode(true);
        mUsernameEditText.clearFocus();
        mBioEditText.setFocusableInTouchMode(true);
        mBioEditText.clearFocus();
    }

    private boolean mIsUsernameChanged, mIsBioChanged;

    private void setupUsernameAndBio() {

        //clearEditableFocus();
        FontUtil.setTextViewFont(mUsernameEditText);
        FontUtil.setTextViewFont(mBioEditText);
        mUsernameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_USERNAME_LENGTH)});
        mBioEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_BIO_LENGTH)});

        mUsernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!Preferences.getInstance().getUsername().equals(charSequence.toString())) {
                    mIsUsernameChanged = true;
                    setSaveButtonVisibility(mIsUsernameChanged, mIsBioChanged);
                } else {
                    mIsUsernameChanged = false;
                    setSaveButtonVisibility(mIsUsernameChanged, mIsBioChanged);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mBioEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                User user = Preferences.getInstance().getUser();
                if (!user.getBio().equals(charSequence.toString())) {
                    mIsBioChanged = true;
                    setSaveButtonVisibility(mIsUsernameChanged, mIsBioChanged);
                } else {
                    mIsBioChanged = false;
                    setSaveButtonVisibility(mIsUsernameChanged, mIsBioChanged);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setSaveButtonVisibility(boolean isUsernameChanged, boolean isBioChanged) {
        if (isUsernameChanged || isBioChanged) {
            if (mSaveButton.getVisibility() != View.VISIBLE) {
                mSaveButton.setVisibility(View.VISIBLE);
                AnimationUtil.scaleInFromCenter(mSaveButton);
            }
        } else {
            if (mSaveButton.getVisibility() != View.GONE) {
                mSaveButton.setVisibility(View.GONE);
                AnimationUtil.scaleInToCenter(mSaveButton);
            }
        }
    }

    private void showViews(boolean isShow) {
        if (isShow) {
            mBioEditText.setVisibility(View.VISIBLE);
            mUsernameEditText.setVisibility(View.VISIBLE);
            mHelpButton.setVisibility(View.VISIBLE);
            if (mIsVirgin) {
                mIsVirgin = false;
                AnimationUtil.scaleInFromCenter(mUsernameEditText);
                AnimationUtil.scaleInFromCenter(mBioEditText);
                AnimationUtil.scaleInFromCenter(mDrawerLikesParent);
                AnimationUtil.scaleInFromCenter(mHelpButton);
            }
        }
    }

    private void listenForUpdatedLikeCount(int userid) {
        mTotalLikesRef = FirebaseDatabase.getInstance().getReference(Constants.USER_TOTAL_LIKES_RECEIVED_REF(userid));
        mTotalLikesEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (mDrawerLikesParent.getVisibility() != View.VISIBLE) {
                        mDrawerLikesParent.setVisibility(View.VISIBLE);
                    }
                    long count = dataSnapshot.getValue(Long.class);
                    if (count == 1) {
                        mDrawerLikesCountView.setText(mActivity.getString(R.string.like_singular));
                    } else {
                        mDrawerLikesCountView.setText(mActivity.getString(R.string.likes_plural, count + ""));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mTotalLikesRef.addValueEventListener(mTotalLikesEventListener);
    }

    public void setUserLikedUserListener(UserLikedUserListener listener) {
        mUserLikedUserListener = listener;
    }

}
