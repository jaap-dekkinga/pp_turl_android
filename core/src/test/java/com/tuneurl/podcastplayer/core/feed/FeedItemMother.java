package com.tuneurl.podcastplayer.core.feed;

import com.tuneurl.podcastplayer.model.feed.FeedItem;

import java.util.Date;

class FeedItemMother {
    private static final String IMAGE_URL = "http://example.com/image";

    static FeedItem anyFeedItemWithImage() {
        FeedItem item = new FeedItem(0, "Item", "Item", "url", new Date(), FeedItem.PLAYED, FeedMother.anyFeed());
        item.setImageUrl(IMAGE_URL);
        return item;
    }

}
