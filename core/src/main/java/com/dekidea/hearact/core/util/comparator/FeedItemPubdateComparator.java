package com.dekidea.hearact.core.util.comparator;

import com.dekidea.hearact.model.feed.FeedItem;

import java.util.Comparator;

/**
 * Compares the pubDate of two FeedItems for sorting.
 */
public class FeedItemPubdateComparator implements Comparator<FeedItem> {

    /**
     * Returns a new instance of this comparator in reverse order.
     */
    @Override
    public int compare(FeedItem lhs, FeedItem rhs) {
        if (rhs.getPubDate() == null && lhs.getPubDate() == null) {
            return 0;
        } else if (rhs.getPubDate() == null) {
            return 1;
        } else if (lhs.getPubDate() == null) {
            return -1;
        }
        return rhs.getPubDate().compareTo(lhs.getPubDate());
    }

}
