package com.instachat.android.model;

import com.google.firebase.database.DataSnapshot;
import com.instachat.android.util.MLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 9/24/2016.
 */
public class GroupChatSummary {

    private static final String TAG = "GroupChatSummary";

    /**
     * The 'id' of the PrivateChatSummary is the same as the id
     * of the person whom you are having the conversation with
     */
    private String id;

    /**
     * information of the user with whom you are chatting with
     */
    private String name;
    private String dpid;
    private String imageUrl;
    private long lastMessageTime;
    private String lastMessage;
    private int unreadMessageCount = -1;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDpid() {
        return dpid;
    }

    public void setDpid(String dpid) {
        this.dpid = dpid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    /**
     * @return returns only the fields you want
     * to update in cloud database
     */
    public Map<String, Object> getUpdateMap() {
        Map<String, Object> map = new HashMap<>(8);
        if (getName() != null)
            map.put("name", getName());
        if (getDpid() != null)
            map.put("dpid", getDpid());
        if (getImageUrl() != null)
            map.put("imageUrl", getImageUrl());
        if (getLastMessage() != null)
            map.put("lastMessage", getLastMessage());
        if (getLastMessageTime() != 0) {
            map.put("lastMessageTime", getLastMessageTime());
        }
        if (getUnreadMessageCount() >= 0) {
            map.put("unreadMessageCount", getUnreadMessageCount());
        }
        return map;
    }

    public static GroupChatSummary fromDataSnapshot(DataSnapshot dataSnapshot) {
        GroupChatSummary groupChatSummary = new GroupChatSummary();
        try {
            groupChatSummary.setId(dataSnapshot.getKey());

            if (dataSnapshot.hasChild("name")) {
                groupChatSummary.setName((String) dataSnapshot.child("name").getValue());
            }
            if (dataSnapshot.hasChild("dpid")) {
                groupChatSummary.setDpid((String) dataSnapshot.child("dpid").getValue());
            }
            if (dataSnapshot.hasChild("imageUrl")) {
                groupChatSummary.setImageUrl((String) dataSnapshot.child("imageUrl").getValue());
            }
            if (dataSnapshot.hasChild("lastMessageTime")) {
                groupChatSummary.setLastMessageTime((Long) dataSnapshot.child("lastMessageTime").getValue());
            }
            if (dataSnapshot.hasChild("lastMessage")) {
                groupChatSummary.setLastMessage((String) dataSnapshot.child("lastMessage").getValue());
            }
            if (dataSnapshot.hasChild("unreadMessageCount")) {
                groupChatSummary.setUnreadMessageCount((Integer) dataSnapshot.child("unreadMessageCount").getValue());
            }
        } catch (Exception e) {
            MLog.e(TAG, "", e);
        }
        return groupChatSummary;
    }
}
