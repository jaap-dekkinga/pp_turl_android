package com.tuneurl.podcastplayer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tuneurl.podcastplayer.core.feed.util.ImageResourceUtils;
import com.tuneurl.podcastplayer.core.glide.ApGlideSettings;
import com.tuneurl.podcastplayer.core.service.playback.PlaybackService;
import com.tuneurl.podcastplayer.core.util.playback.PlaybackController;
import com.tuneurl.podcastplayer.event.playback.PlaybackPositionEvent;
import com.tuneurl.podcastplayer.event.playback.PlaybackServiceEvent;
import com.tuneurl.podcastplayer.model.playback.MediaType;
import com.tuneurl.podcastplayer.model.playback.Playable;
import com.tuneurl.podcastplayer.playback.base.PlayerStatus;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.tuneurl.podcastplayer.R;
import com.tuneurl.podcastplayer.activity.MainActivity2;
import com.tuneurl.podcastplayer.view.PlayButton;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Fragment which is supposed to be displayed outside of the MediaplayerActivity.
 */
public class ExternalPlayerFragment2 extends Fragment {
    public static final String TAG = "ExternalPlayerFragment";

    private ImageView imgvCover;
    private TextView txtvTitle;
    private PlayButton butPlay;
    private TextView feedName;
    private ProgressBar progressBar;
    private PlaybackController controller;
    private Disposable disposable;

    public ExternalPlayerFragment2() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.external_player_fragment, container, false);
        imgvCover = root.findViewById(R.id.imgvCover);
        txtvTitle = root.findViewById(R.id.txtvTitle);
        butPlay = root.findViewById(R.id.butPlay);
        feedName = root.findViewById(R.id.txtvAuthor);
        progressBar = root.findViewById(R.id.episodeProgress);

        root.findViewById(R.id.fragmentLayout).setOnClickListener(v -> {
            Log.d(TAG, "layoutInfo was clicked");

            if (controller != null && controller.getMedia() != null) {
                if (controller.getMedia().getMediaType() == MediaType.AUDIO) {
                    ((MainActivity2) getActivity()).getBottomSheet().setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    Intent intent = PlaybackService.getPlayerActivityIntent(getActivity(), controller.getMedia());
                    startActivity(intent);
                }
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        butPlay.setOnClickListener(v -> {
            if (controller == null) {
                return;
            }
            if (controller.getMedia() != null && controller.getMedia().getMediaType() == MediaType.VIDEO
                    && controller.getStatus() != PlayerStatus.PLAYING) {
                controller.playPause();
                getContext().startActivity(PlaybackService
                        .getPlayerActivityIntent(getContext(), controller.getMedia()));
            } else {
                controller.playPause();
            }
        });
        loadMediaInfo();
    }

    private PlaybackController setupPlaybackController() {
        return new PlaybackController(getActivity()) {
            @Override
            protected void updatePlayButtonShowsPlay(boolean showPlay) {
                butPlay.setIsShowPlay(showPlay);
            }

            @Override
            public void loadMediaInfo() {
                ExternalPlayerFragment2.this.loadMediaInfo();
            }

            @Override
            public void onPlaybackEnd() {
                ((MainActivity2) getActivity()).setPlayerVisible(false);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        controller = setupPlaybackController();
        controller.init();
        loadMediaInfo();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (controller != null) {
            controller.release();
            controller = null;
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPositionObserverUpdate(PlaybackPositionEvent event) {
        if (controller == null) {
            return;
        } else if (controller.getPosition() == PlaybackService.INVALID_TIME
                || controller.getDuration() == PlaybackService.INVALID_TIME) {
            return;
        }
        progressBar.setProgress((int)
                ((double) controller.getPosition() / controller.getDuration() * 100));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaybackServiceChanged(PlaybackServiceEvent event) {
        if (event.action == PlaybackServiceEvent.Action.SERVICE_SHUT_DOWN) {
            ((MainActivity2) getActivity()).setPlayerVisible(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Fragment is about to be destroyed");
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (controller != null) {
            controller.pause();
        }
    }

    private void loadMediaInfo() {
        Log.d(TAG, "Loading media info");
        if (controller == null) {
            Log.w(TAG, "loadMediaInfo was called while PlaybackController was null!");
            return;
        }

        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Maybe.fromCallable(() -> controller.getMedia())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateUi,
                        error -> Log.e(TAG, Log.getStackTraceString(error)),
                        () -> ((MainActivity2) getActivity()).setPlayerVisible(false));
    }

    private void updateUi(Playable media) {
        if (media == null) {
            return;
        }
        ((MainActivity2) getActivity()).setPlayerVisible(true);
        txtvTitle.setText(media.getEpisodeTitle());
        feedName.setText(media.getFeedTitle());
        onPositionObserverUpdate(new PlaybackPositionEvent(media.getPosition(), media.getDuration()));

        RequestOptions options = new RequestOptions()
                .placeholder(R.color.light_gray)
                .error(R.color.light_gray)
                .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                .fitCenter()
                .dontAnimate();

        Glide.with(getActivity())
                .load(ImageResourceUtils.getEpisodeListImageLocation(media))
                .error(Glide.with(getActivity())
                        .load(ImageResourceUtils.getFallbackImageLocation(media))
                        .apply(options))
                .apply(options)
                .into(imgvCover);

        if (controller != null && controller.isPlayingVideoLocally()) {
            ((MainActivity2) getActivity()).getBottomSheet().setLocked(true);
            ((MainActivity2) getActivity()).getBottomSheet().setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            butPlay.setVisibility(View.VISIBLE);
            ((MainActivity2) getActivity()).getBottomSheet().setLocked(false);
        }
    }
}
