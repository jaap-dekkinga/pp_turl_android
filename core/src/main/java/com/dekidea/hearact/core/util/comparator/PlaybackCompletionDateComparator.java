package com.dekidea.hearact.core.util.comparator;

import com.dekidea.hearact.model.feed.FeedItem;

import java.util.Comparator;

public class PlaybackCompletionDateComparator implements Comparator<FeedItem> {
	
	public int compare(FeedItem lhs, FeedItem rhs) {
		if (lhs.getMedia() != null
				&& lhs.getMedia().getPlaybackCompletionDate() != null
				&& rhs.getMedia() != null
				&& rhs.getMedia().getPlaybackCompletionDate() != null) {
			return rhs.getMedia().getPlaybackCompletionDate()
					.compareTo(lhs.getMedia().getPlaybackCompletionDate());
		}
		return 0;
	}
}
