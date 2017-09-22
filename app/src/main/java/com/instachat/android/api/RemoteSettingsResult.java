package com.instachat.android.api;

import com.google.gson.annotations.SerializedName;

public class RemoteSettingsResult {

    @SerializedName("status")
    public String status;

    @SerializedName("data")
    public Setting data;

    public static class Setting {
        @SerializedName("a")
        public String a;

        @SerializedName("s")
        public String s;
    }
}
