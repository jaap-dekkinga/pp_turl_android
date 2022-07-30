package com.tuneurl.podcastplayer.net.sync.nextcloud;

import com.tuneurl.podcastplayer.net.sync.model.SyncServiceException;

public class NextcloudSynchronizationServiceException extends SyncServiceException {
    public NextcloudSynchronizationServiceException(Throwable e) {
        super(e);
    }
}
