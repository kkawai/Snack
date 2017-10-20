package com.instachat.android.app.activity.pm;

import android.arch.lifecycle.ViewModelProvider;
import android.support.v7.widget.LinearLayoutManager;

import com.instachat.android.app.activity.PresenceHelper;
import com.instachat.android.app.activity.group.GroupChatActivity;
import com.instachat.android.data.DataManager;
import com.instachat.android.di.ViewModelProviderFactory;
import com.instachat.android.util.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class PrivateChatModule {

    @Provides
    PrivateChatViewModel provideGroupChatViewModel(DataManager dataManager,
                                                   SchedulerProvider schedulerProvider) {
        return new PrivateChatViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory privateChatViewModelProvider(PrivateChatViewModel privateChatViewModel) {
        return new ViewModelProviderFactory<>(privateChatViewModel);
    }

    @Provides
    LinearLayoutManager provideGridLayoutManager(PrivateChatActivity activity) {
        return new LinearLayoutManager(activity);
    }
}
