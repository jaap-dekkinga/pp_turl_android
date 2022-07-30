package com.tuneurl.podcastplayer.core.util.comparator;

import com.tuneurl.podcastplayer.model.feed.Chapter;

import java.util.Comparator;

public class ChapterStartTimeComparator implements Comparator<Chapter> {

	@Override
	public int compare(Chapter lhs, Chapter rhs) {
		return Long.compare(lhs.getStart(), rhs.getStart());
	}

}
