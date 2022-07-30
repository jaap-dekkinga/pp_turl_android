package com.tuneurl.podcastplayer.event;

import androidx.annotation.Nullable;

import com.tuneurl.podcastplayer.model.feed.FeedItem;

import java.util.List;

public class QueueEvent {

    public enum Action {
        ADDED, ADDED_ITEMS, SET_QUEUE, REMOVED, IRREVERSIBLE_REMOVED, CLEARED, DELETED_MEDIA, SORTED, MOVED
    }

    public final Action action;
    public final FeedItem item;
    public final int position;
    public final List<FeedItem> items;


    private QueueEvent(Action action,
                       @Nullable FeedItem item,
                       @Nullable List<FeedItem> items,
                       int position) {
        this.action = action;
        this.item = item;
        this.items = items;
        this.position = position;
    }

    public static QueueEvent added(FeedItem item, int position) {
        return new QueueEvent(Action.ADDED, item, null, position);
    }

    public static QueueEvent setQueue(List<FeedItem> queue) {
        return new QueueEvent(Action.SET_QUEUE, null, queue, -1);
    }

    public static QueueEvent removed(FeedItem item) {
        return new QueueEvent(Action.REMOVED, item, null, -1);
    }

    public static QueueEvent irreversibleRemoved(FeedItem item) {
        return new QueueEvent(Action.IRREVERSIBLE_REMOVED, item, null, -1);
    }

    public static QueueEvent cleared() {
        return new QueueEvent(Action.CLEARED, null, null, -1);
    }

    public static QueueEvent sorted(List<FeedItem> sortedQueue) {
        return new QueueEvent(Action.SORTED, null, sortedQueue, -1);
    }

    public static QueueEvent moved(FeedItem item, int newPosition) {
        return new QueueEvent(Action.MOVED, item, null, newPosition);
    }
}
