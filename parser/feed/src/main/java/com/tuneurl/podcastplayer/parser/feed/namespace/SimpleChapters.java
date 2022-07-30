package com.tuneurl.podcastplayer.parser.feed.namespace;

import android.util.Log;

import com.tuneurl.podcastplayer.model.feed.Chapter;
import com.tuneurl.podcastplayer.parser.feed.HandlerState;
import com.tuneurl.podcastplayer.parser.feed.element.SyndElement;
import com.tuneurl.podcastplayer.parser.feed.util.DateUtils;
import org.xml.sax.Attributes;

import java.util.ArrayList;

import com.tuneurl.podcastplayer.model.feed.FeedItem;

public class SimpleChapters extends Namespace {
    private static final String TAG = "NSSimpleChapters";

    public static final String NSTAG = "psc|sc";
    public static final String NSURI = "http://podlove.org/simple-chapters";

    private static final String CHAPTERS = "chapters";
    private static final String CHAPTER = "chapter";
    private static final String START = "start";
    private static final String TITLE = "title";
    private static final String HREF = "href";
    private static final String IMAGE = "image";

    @Override
    public SyndElement handleElementStart(String localName, HandlerState state, Attributes attributes) {
        FeedItem currentItem = state.getCurrentItem();
        if (currentItem != null) {
            if (localName.equals(CHAPTERS)) {
                currentItem.setChapters(new ArrayList<>());
            } else if (localName.equals(CHAPTER)) {
                try {
                    long start = DateUtils.parseTimeString(attributes.getValue(START));
                    String title = attributes.getValue(TITLE);
                    String link = attributes.getValue(HREF);
                    String imageUrl = attributes.getValue(IMAGE);
                    Chapter chapter = new Chapter(start, title, link, imageUrl);
                    currentItem.getChapters().add(chapter);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Unable to read chapter", e);
                }
            }
        }
        return new SyndElement(localName, this);
    }

    @Override
    public void handleElementEnd(String localName, HandlerState state) {
    }

}
