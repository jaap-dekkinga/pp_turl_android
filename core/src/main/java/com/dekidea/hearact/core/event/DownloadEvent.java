package com.dekidea.hearact.core.event;

import androidx.annotation.NonNull;

import com.dekidea.hearact.core.service.download.Downloader;

import java.util.ArrayList;
import java.util.List;

public class DownloadEvent {

    public final DownloaderUpdate update;

    private DownloadEvent(DownloaderUpdate downloader) {
        this.update = downloader;
    }

    public static DownloadEvent refresh(List<Downloader> list) {
        list = new ArrayList<>(list);
        DownloaderUpdate update = new DownloaderUpdate(list);
        return new DownloadEvent(update);
    }

    @NonNull
    @Override
    public String toString() {
        return "DownloadEvent{" +
                "update=" + update +
                '}';
    }

    public boolean hasChangedFeedUpdateStatus(boolean oldStatus) {
        return oldStatus != update.feedIds.length > 0;
    }
}
