package com.tuneurl.podcastplayer.parser.feed.namespace;

import com.tuneurl.podcastplayer.parser.feed.HandlerState;
import com.tuneurl.podcastplayer.parser.feed.element.SyndElement;
import com.tuneurl.podcastplayer.parser.feed.util.DateUtils;
import org.xml.sax.Attributes;

import com.tuneurl.podcastplayer.model.feed.FeedItem;

public class DublinCore extends Namespace {
    public static final String NSTAG = "dc";
    public static final String NSURI = "http://purl.org/dc/elements/1.1/";

    private static final String ITEM = "item";
    private static final String DATE = "date";

    @Override
    public SyndElement handleElementStart(String localName, HandlerState state,
                                          Attributes attributes) {
        return new SyndElement(localName, this);
    }

    @Override
    public void handleElementEnd(String localName, HandlerState state) {
        if (state.getCurrentItem() != null && state.getContentBuf() != null &&
            state.getTagstack() != null && state.getTagstack().size() >= 2) {
            FeedItem currentItem = state.getCurrentItem();
            String top = state.getTagstack().peek().getName();
            String second = state.getSecondTag().getName();
            if (DATE.equals(top) && ITEM.equals(second)) {
                String content = state.getContentBuf().toString();
                currentItem.setPubDate(DateUtils.parseOrNullIfFuture(content));
            }
        }
    }

}
