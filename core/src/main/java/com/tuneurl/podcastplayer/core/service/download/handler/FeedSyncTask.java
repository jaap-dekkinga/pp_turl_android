package com.tuneurl.podcastplayer.core.service.download.handler;

import android.content.Context;

import androidx.annotation.NonNull;

import com.tuneurl.podcastplayer.model.download.DownloadStatus;
import com.tuneurl.podcastplayer.model.feed.Feed;
import com.tuneurl.podcastplayer.parser.feed.FeedHandlerResult;
import com.tuneurl.podcastplayer.core.service.download.DownloadRequest;
import com.tuneurl.podcastplayer.core.storage.DBTasks;

public class FeedSyncTask {
    private final DownloadRequest request;
    private final Context context;
    private Feed savedFeed;
    private final FeedParserTask task;

    public FeedSyncTask(Context context, DownloadRequest request) {
        this.request = request;
        this.context = context;
        this.task = new FeedParserTask(request);
    }

    public boolean run() {
        FeedHandlerResult result = task.call();
        if (!task.isSuccessful()) {
            return false;
        }

        savedFeed = DBTasks.updateFeed(context, result.feed, false);
        // If loadAllPages=true, check if another page is available and queue it for download
        final boolean loadAllPages = request.getArguments().getBoolean(DownloadRequest.REQUEST_ARG_LOAD_ALL_PAGES);
        final Feed feed = result.feed;
        if (loadAllPages && feed.getNextPageLink() != null) {
            feed.setId(savedFeed.getId());
            DBTasks.loadNextPageOfFeed(context, feed, true);
        }
        return true;
    }

    @NonNull
    public DownloadStatus getDownloadStatus() {
        return task.getDownloadStatus();
    }

    public Feed getSavedFeed() {
        return savedFeed;
    }
}
