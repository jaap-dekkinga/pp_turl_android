package com.dekidea.hearact.net.sync.nextcloud;

import com.dekidea.hearact.net.sync.model.SyncServiceException;

public class NextcloudSynchronizationServiceException extends SyncServiceException {
    public NextcloudSynchronizationServiceException(Throwable e) {
        super(e);
    }
}
