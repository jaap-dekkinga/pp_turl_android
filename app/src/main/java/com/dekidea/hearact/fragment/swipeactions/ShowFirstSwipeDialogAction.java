package com.dekidea.hearact.fragment.swipeactions;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.dekidea.hearact.model.feed.FeedItem;
import com.dekidea.hearact.model.feed.FeedItemFilter;

import com.dekidea.hearact.R;

public class ShowFirstSwipeDialogAction implements SwipeAction {

    @Override
    public String getId() {
        return "SHOW_FIRST_SWIPE_DIALOG";
    }

    @Override
    public int getActionIcon() {
        return R.drawable.ic_settings;
    }

    @Override
    public int getActionColor() {
        return R.attr.icon_gray;
    }

    @Override
    public String getTitle(Context context) {
        return "";
    }

    @Override
    public void performAction(FeedItem item, Fragment fragment, FeedItemFilter filter) {
        //handled in SwipeActions
    }

    @Override
    public boolean willRemove(FeedItemFilter filter, FeedItem item) {
        return false;
    }
}
