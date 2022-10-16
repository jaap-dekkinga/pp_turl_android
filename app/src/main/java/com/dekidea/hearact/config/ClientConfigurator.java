package com.dekidea.hearact.config;

import com.dekidea.hearact.BuildConfig;
import com.dekidea.hearact.core.ClientConfig;

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
