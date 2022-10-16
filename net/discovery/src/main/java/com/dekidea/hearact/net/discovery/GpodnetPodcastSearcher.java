package com.dekidea.hearact.net.discovery;

import com.dekidea.hearact.net.sync.gpoddernet.GpodnetService;
import com.dekidea.hearact.net.sync.gpoddernet.GpodnetServiceException;
import com.dekidea.hearact.net.sync.gpoddernet.model.GpodnetPodcast;
import com.dekidea.hearact.core.sync.SynchronizationCredentials;
import com.dekidea.hearact.core.service.download.AntennapodHttpClient;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

public class GpodnetPodcastSearcher implements PodcastSearcher {
    public Single<List<PodcastSearchResult>> search(String query) {
        return Single.create((SingleOnSubscribe<List<PodcastSearchResult>>) subscriber -> {
            try {
                GpodnetService service = new GpodnetService(AntennapodHttpClient.getHttpClient(),
                        SynchronizationCredentials.getHosturl(), SynchronizationCredentials.getDeviceID(),
                        SynchronizationCredentials.getUsername(), SynchronizationCredentials.getPassword());
                List<GpodnetPodcast> gpodnetPodcasts = service.searchPodcasts(query, 0);
                List<PodcastSearchResult> results = new ArrayList<>();
                for (GpodnetPodcast podcast : gpodnetPodcasts) {
                    results.add(PodcastSearchResult.fromGpodder(podcast));
                }
                subscriber.onSuccess(results);
            } catch (GpodnetServiceException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<String> lookupUrl(String url) {
        return Single.just(url);
    }

    @Override
    public boolean urlNeedsLookup(String url) {
        return false;
    }

    @Override
    public String getName() {
        return "Gpodder.net";
    }
}
