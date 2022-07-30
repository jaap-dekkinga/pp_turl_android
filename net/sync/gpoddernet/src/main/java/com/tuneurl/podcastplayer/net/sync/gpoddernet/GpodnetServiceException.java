package com.tuneurl.podcastplayer.net.sync.gpoddernet;

import com.tuneurl.podcastplayer.net.sync.model.SyncServiceException;

public class GpodnetServiceException extends SyncServiceException {
    private static final long serialVersionUID = 1L;

    public GpodnetServiceException(String message) {
        super(message);
    }

    public GpodnetServiceException(Throwable e) {
        super(e);
    }
}
