package com.dekidea.hearact.parser.feed;

import java.util.Map;

import com.dekidea.hearact.model.feed.Feed;

/**
 * Container for results returned by the Feed parser
 */
public class FeedHandlerResult {

    public final Feed feed;
    public final Map<String, String> alternateFeedUrls;

    public FeedHandlerResult(Feed feed, Map<String, String> alternateFeedUrls) {
        this.feed = feed;
        this.alternateFeedUrls = alternateFeedUrls;
    }
}
