package com.tuneurl.podcastplayer.core.service.download.handler;

import com.tuneurl.podcastplayer.core.event.DownloadEvent;
import com.tuneurl.podcastplayer.core.service.download.Downloader;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostDownloaderTask implements Runnable {
    private List<Downloader> downloads;

    public PostDownloaderTask(List<Downloader> downloads) {
        this.downloads = downloads;
    }

    @Override
    public void run() {
        List<Downloader> runningDownloads = new ArrayList<>();
        for (Downloader downloader : downloads) {
            if (!downloader.cancelled) {
                runningDownloads.add(downloader);
            }
        }
        List<Downloader> list = Collections.unmodifiableList(runningDownloads);
        EventBus.getDefault().postSticky(DownloadEvent.refresh(list));
    }
}
