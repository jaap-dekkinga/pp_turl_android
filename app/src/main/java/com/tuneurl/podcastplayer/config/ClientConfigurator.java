package com.tuneurl.podcastplayer.config;

import com.tuneurl.podcastplayer.BuildConfig;
import com.tuneurl.podcastplayer.core.ClientConfig;

/**
 * Configures the ClientConfig class of the core package.
 */
class ClientConfigurator {

    private ClientConfigurator() {
    }

    static {
        ClientConfig.USER_AGENT = "HearAct/" + BuildConfig.VERSION_NAME;
        ClientConfig.applicationCallbacks = new ApplicationCallbacksImpl();
        ClientConfig.downloadServiceCallbacks = new DownloadServiceCallbacksImpl();
    }
}
