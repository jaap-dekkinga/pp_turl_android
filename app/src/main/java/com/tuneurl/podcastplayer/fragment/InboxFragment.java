package com.tuneurl.podcastplayer.fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tuneurl.podcastplayer.activity.MainActivity;
import com.tuneurl.podcastplayer.core.event.DownloadEvent;
import com.tuneurl.podcastplayer.core.menuhandler.MenuItemUtils;
import com.tuneurl.podcastplayer.core.service.download.DownloadService;
import com.tuneurl.podcastplayer.core.storage.DBReader;
import com.tuneurl.podcastplayer.fragment.swipeactions.SwipeActions;
import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedItemFilter;

import java.util.List;

import com.tuneurl.podcastplayer.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Like 'EpisodesFragment' except that it only shows new episodes and
 * supports swiping to mark as read.
 */
public class InboxFragment extends EpisodesListFragment implements Toolbar.OnMenuItemClickListener {
    public static final String TAG = "NewEpisodesFragment";
    private static final String PREF_NAME = "PrefNewEpisodesFragment";
    private static final String KEY_UP_ARROW = "up_arrow";

    private Toolbar toolbar;
    private boolean displayUpArrow;
    private volatile boolean isUpdatingFeeds;

    @Override
    protected String getPrefName() {
        return PREF_NAME;
    }

    @Override
    protected boolean shouldUpdatedItemRemainInList(FeedItem item) {
        return item.isNew();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inboxContainer = View.inflate(getContext(), R.layout.list_container_fragment, null);
        View root = super.onCreateView(inflater, container, savedInstanceState);
        ((FrameLayout) inboxContainer.findViewById(R.id.listContent)).addView(root);
        emptyView.setTitle(R.string.no_inbox_head_label);
        emptyView.setMessage(R.string.no_inbox_label);

        toolbar = inboxContainer.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.inflateMenu(R.menu.inbox);
        toolbar.setOnLongClickListener(v -> {
            recyclerView.scrollToPosition(5);
            recyclerView.post(() -> recyclerView.smoothScrollToPosition(0));
            return false;
        });
        displayUpArrow = getParentFragmentManager().getBackStackEntryCount() != 0;
        if (savedInstanceState != null) {
            displayUpArrow = savedInstanceState.getBoolean(KEY_UP_ARROW);
        }
        //((MainActivity) getActivity()).setupToolbarToggle(toolbar, displayUpArrow);

        SwipeActions swipeActions = new SwipeActions(this, TAG).attachTo(recyclerView);
        swipeActions.setFilter(new FeedItemFilter(FeedItemFilter.NEW));

        speedDialView.removeActionItemById(R.id.mark_unread_batch);
        speedDialView.removeActionItemById(R.id.remove_from_queue_batch);
        speedDialView.removeActionItemById(R.id.delete_batch);
        return inboxContainer;
    }

    private final MenuItemUtils.UpdateRefreshMenuItemChecker updateRefreshMenuItemChecker =
            () -> DownloadService.isRunning && DownloadService.isDownloadingFeeds();

    private void updateToolbar() {
        isUpdatingFeeds = MenuItemUtils.updateRefreshMenuItem(toolbar.getMenu(),
                R.id.refresh_item, updateRefreshMenuItemChecker);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isUpdatingFeeds != updateRefreshMenuItemChecker.isRefreshing()) {
            updateToolbar();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DownloadEvent event) {
        super.onEventMainThread(event);
        if (event.hasChangedFeedUpdateStatus(isUpdatingFeeds)) {
            updateToolbar();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(KEY_UP_ARROW, displayUpArrow);
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    protected List<FeedItem> loadData() {
        return DBReader.getNewItemsList(0, page * EPISODES_PER_PAGE);
    }

    @NonNull
    @Override
    protected List<FeedItem> loadMoreData(int page) {
        return DBReader.getNewItemsList((page - 1) * EPISODES_PER_PAGE, EPISODES_PER_PAGE);
    }

    @Override
    protected int loadTotalItemCount() {
        return DBReader.getTotalEpisodeCount(new FeedItemFilter(FeedItemFilter.NEW));
    }
}
