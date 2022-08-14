package com.tuneurl.podcastplayer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tuneurl.podcastplayer.activity.MainActivity;
import com.tuneurl.podcastplayer.adapter.EpisodeItemListAdapter;
import com.tuneurl.podcastplayer.adapter.QueueRecyclerAdapter;
import com.tuneurl.podcastplayer.core.event.DownloadEvent;
import com.tuneurl.podcastplayer.core.event.DownloaderUpdate;
import com.tuneurl.podcastplayer.core.feed.util.PlaybackSpeedUtils;
import com.tuneurl.podcastplayer.core.menuhandler.MenuItemUtils;
import com.tuneurl.podcastplayer.core.preferences.UserPreferences;
import com.tuneurl.podcastplayer.core.storage.DBReader;
import com.tuneurl.podcastplayer.core.storage.DBWriter;
import com.tuneurl.podcastplayer.core.util.Converter;
import com.tuneurl.podcastplayer.core.util.FeedItemUtil;
import com.tuneurl.podcastplayer.core.util.download.AutoUpdateManager;
import com.tuneurl.podcastplayer.event.FeedItemEvent;
import com.tuneurl.podcastplayer.event.PlayerStatusEvent;
import com.tuneurl.podcastplayer.event.QueueEvent;
import com.tuneurl.podcastplayer.event.UnreadItemsUpdateEvent;
import com.tuneurl.podcastplayer.event.playback.PlaybackPositionEvent;
import com.tuneurl.podcastplayer.fragment.actions.EpisodeMultiSelectActionHandler;
import com.tuneurl.podcastplayer.fragment.swipeactions.SwipeActions;
import com.tuneurl.podcastplayer.menuhandler.FeedItemMenuHandler;
import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedItemFilter;
import com.google.android.material.snackbar.Snackbar;
import com.leinardi.android.speeddial.SpeedDialView;

import com.tuneurl.podcastplayer.R;
import com.tuneurl.podcastplayer.view.EmptyViewHandler;
import com.tuneurl.podcastplayer.view.EpisodeItemListRecyclerView;
import com.tuneurl.podcastplayer.view.viewholder.EpisodeItemViewHolder;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

/**
 * Shows all items in the queue.
 */
public class QueueFragment extends Fragment implements EpisodeItemListAdapter.OnSelectModeListener {
    public static final String TAG = "QueueFragment";
    private static final String KEY_UP_ARROW = "up_arrow";

    private TextView infoBar;
    private EpisodeItemListRecyclerView recyclerView;
    private QueueRecyclerAdapter recyclerAdapter;
    private EmptyViewHandler emptyView;
    private ProgressBar progLoading;

    private List<FeedItem> queue;

    private boolean isUpdatingFeeds = false;

    private static final String PREFS = "QueueFragment";
    private static final String PREF_SHOW_LOCK_WARNING = "show_lock_warning";

    private Disposable disposable;
    private SwipeActions swipeActions;
    private SharedPreferences prefs;

    private SpeedDialView speedDialView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        prefs = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (queue != null) {
            onFragmentLoaded(true);
        }
        loadItems(true);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        recyclerView.saveScrollPosition(QueueFragment.TAG);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(QueueEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
        if (queue == null) {
            return;
        } else if (recyclerAdapter == null) {
            loadItems(true);
            return;
        }
        switch(event.action) {
            case ADDED:
                queue.add(event.position, event.item);
                recyclerAdapter.notifyItemInserted(event.position);
                break;
            case SET_QUEUE:
            case SORTED: //Deliberate fall-through
                queue = event.items;
                recyclerAdapter.notifyDataSetChanged();
                break;
            case REMOVED:
            case IRREVERSIBLE_REMOVED:
                int position = FeedItemUtil.indexOfItemWithId(queue, event.item.getId());
                queue.remove(position);
                recyclerAdapter.notifyItemRemoved(position);
                break;
            case CLEARED:
                queue.clear();
                recyclerAdapter.notifyDataSetChanged();
                break;
            case MOVED:
                return;
        }
        recyclerView.saveScrollPosition(QueueFragment.TAG);
        onFragmentLoaded(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FeedItemEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
        if (queue == null) {
            return;
        } else if (recyclerAdapter == null) {
            loadItems(true);
            return;
        }
        for (int i = 0, size = event.items.size(); i < size; i++) {
            FeedItem item = event.items.get(i);
            int pos = FeedItemUtil.indexOfItemWithId(queue, item.getId());
            if (pos >= 0) {
                queue.remove(pos);
                queue.add(pos, item);
                recyclerAdapter.notifyItemChangedCompat(pos);
                refreshInfoBar();
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DownloadEvent event) {
        Log.d(TAG, "onEventMainThread() called with DownloadEvent");
        DownloaderUpdate update = event.update;
        if (event.hasChangedFeedUpdateStatus(isUpdatingFeeds)) {
            
        }
        if (recyclerAdapter != null && update.mediaIds.length > 0) {
            for (long mediaId : update.mediaIds) {
                int pos = FeedItemUtil.indexOfItemWithMediaId(queue, mediaId);
                if (pos >= 0) {
                    recyclerAdapter.notifyItemChangedCompat(pos);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PlaybackPositionEvent event) {
        if (recyclerAdapter != null) {
            for (int i = 0; i < recyclerAdapter.getItemCount(); i++) {
                EpisodeItemViewHolder holder = (EpisodeItemViewHolder)
                        recyclerView.findViewHolderForAdapterPosition(i);
                if (holder != null && holder.isCurrentlyPlayingItem()) {
                    holder.notifyPlaybackPositionUpdated(event);
                    break;
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerStatusChanged(PlayerStatusEvent event) {
        
        loadItems(false);       
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadItemsChanged(UnreadItemsUpdateEvent event) {
        // Sent when playback position is reset
        loadItems(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onKeyUp(KeyEvent event) {
        if (!isAdded() || !isVisible() || !isMenuVisible()) {
            return;
        }
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_T:
                recyclerView.smoothScrollToPosition(0);
                break;
            case KeyEvent.KEYCODE_B:
                recyclerView.smoothScrollToPosition(recyclerAdapter.getItemCount() - 1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (recyclerAdapter != null) {
            recyclerAdapter.endSelectMode();
        }
        recyclerAdapter = null;
    }

    private void toggleQueueLock() {
        boolean isLocked = UserPreferences.isQueueLocked();
        if (isLocked) {
            setQueueLocked(false);
        } else {
            boolean shouldShowLockWarning = prefs.getBoolean(PREF_SHOW_LOCK_WARNING, true);
            if (!shouldShowLockWarning) {
                setQueueLocked(true);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.lock_queue);
                builder.setMessage(R.string.queue_lock_warning);

                View view = View.inflate(getContext(), R.layout.checkbox_do_not_show_again, null);
                CheckBox checkDoNotShowAgain = view.findViewById(R.id.checkbox_do_not_show_again);
                builder.setView(view);

                builder.setPositiveButton(R.string.lock_queue, (dialog, which) -> {
                    prefs.edit().putBoolean(PREF_SHOW_LOCK_WARNING, !checkDoNotShowAgain.isChecked()).apply();
                    setQueueLocked(true);
                });
                builder.setNegativeButton(R.string.cancel_label, null);
                builder.show();
            }
        }
    }

    private void setQueueLocked(boolean locked) {
        UserPreferences.setQueueLocked(locked);
        
        if (recyclerAdapter != null) {
            recyclerAdapter.updateDragDropEnabled();
        }
        if (queue.size() == 0) {
            if (locked) {
                ((MainActivity) getActivity()).showSnackbarAbovePlayer(R.string.queue_locked, Snackbar.LENGTH_SHORT);
            } else {
                ((MainActivity) getActivity()).showSnackbarAbovePlayer(R.string.queue_unlocked, Snackbar.LENGTH_SHORT);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d(TAG, "onContextItemSelected() called with: " + "item = [" + item + "]");
        if (!isVisible() || recyclerAdapter == null) {
            return false;
        }
        FeedItem selectedItem = recyclerAdapter.getLongPressedItem();
        if (selectedItem == null) {
            Log.i(TAG, "Selected item was null, ignoring selection");
            return super.onContextItemSelected(item);
        }

        int position = FeedItemUtil.indexOfItemWithId(queue, selectedItem.getId());
        if (position < 0) {
            Log.i(TAG, "Selected item no longer exist, ignoring selection");
            return super.onContextItemSelected(item);
        }
        if (recyclerAdapter.onContextItemSelected(item)) {
            return true;
        }

        final int itemId = item.getItemId();
        if (itemId == R.id.move_to_top_item) {
            queue.add(0, queue.remove(position));
            recyclerAdapter.notifyItemMoved(position, 0);
            DBWriter.moveQueueItemToTop(selectedItem.getId(), true);
            return true;
        } else if (itemId == R.id.move_to_bottom_item) {
            queue.add(queue.size() - 1, queue.remove(position));
            recyclerAdapter.notifyItemMoved(position, queue.size() - 1);
            DBWriter.moveQueueItemToBottom(selectedItem.getId(), true);
            return true;
        }
        return FeedItemMenuHandler.onMenuItemClicked(this, item.getItemId(), selectedItem);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.queue_fragment, container, false);
        
        ((MainActivity) getActivity()).setSelectedFragmentTitle(getString(R.string.bookmarks));

        infoBar = root.findViewById(R.id.info_bar);
        recyclerView = root.findViewById(R.id.recyclerView);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        recyclerView.setRecycledViewPool(((MainActivity) getActivity()).getRecycledViewPool());
        registerForContextMenu(recyclerView);

        SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setDistanceToTriggerSync(getResources().getInteger(R.integer.swipe_refresh_distance));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            AutoUpdateManager.runImmediate(requireContext());
            new Handler(Looper.getMainLooper()).postDelayed(() -> swipeRefreshLayout.setRefreshing(false),
                    getResources().getInteger(R.integer.swipe_to_refresh_duration_in_ms));
        });

        swipeActions = new QueueSwipeActions();
        swipeActions.setFilter(new FeedItemFilter(FeedItemFilter.QUEUED));
        swipeActions.attachTo(recyclerView);

        emptyView = new EmptyViewHandler(getContext());
        emptyView.attachToRecyclerView(recyclerView);
        emptyView.setIcon(R.drawable.ic_playlist);
        emptyView.setTitle(R.string.no_items_header_label);
        emptyView.setMessage(R.string.no_items_label);

        progLoading = root.findViewById(R.id.progLoading);
        progLoading.setVisibility(View.VISIBLE);

        speedDialView = root.findViewById(R.id.fabSD);
        speedDialView.setOverlayLayout(root.findViewById(R.id.fabSDOverlay));
        speedDialView.inflate(R.menu.episodes_apply_action_speeddial);
        speedDialView.removeActionItemById(R.id.mark_read_batch);
        speedDialView.removeActionItemById(R.id.mark_unread_batch);
        speedDialView.removeActionItemById(R.id.add_to_queue_batch);
        speedDialView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                return false;
            }

            @Override
            public void onToggleChanged(boolean open) {
                if (open && recyclerAdapter.getSelectedCount() == 0) {
                    ((MainActivity) getActivity()).showSnackbarAbovePlayer(R.string.no_items_selected,
                            Snackbar.LENGTH_SHORT);
                    speedDialView.close();
                }
            }
        });
        speedDialView.setOnActionSelectedListener(actionItem -> {
            new EpisodeMultiSelectActionHandler(((MainActivity) getActivity()), actionItem.getId())
                    .handleAction(recyclerAdapter.getSelectedItems());
            recyclerAdapter.endSelectMode();
            return true;
        });
        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    private void onFragmentLoaded(final boolean restoreScrollPosition) {

        if (queue != null) {

            if (recyclerAdapter == null) {

                MainActivity activity = (MainActivity) getActivity();

                recyclerAdapter = new QueueRecyclerAdapter(activity, swipeActions) {

                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        super.onCreateContextMenu(menu, v, menuInfo);
                        MenuItemUtils.setOnClickListeners(menu, QueueFragment.this::onContextItemSelected);
                    }
                };

                recyclerAdapter.setOnSelectModeListener(this);
                recyclerView.setAdapter(recyclerAdapter);
                emptyView.updateAdapter(recyclerAdapter);
            }
            recyclerAdapter.updateItems(queue);
        } else {
            recyclerAdapter = null;
            emptyView.updateAdapter(null);
        }

        if (restoreScrollPosition) {
            recyclerView.restoreScrollPosition(QueueFragment.TAG);
        }

        // we need to refresh the options menu because it sometimes
        // needs data that may have just been loaded.
        

        refreshInfoBar();
    }

    private void refreshInfoBar() {
        String info = String.format(Locale.getDefault(), "%d%s",
                queue.size(), getString(R.string.episodes_suffix));
        if (queue.size() > 0) {
            long timeLeft = 0;
            for (FeedItem item : queue) {
                float playbackSpeed = 1;
                if (UserPreferences.timeRespectsSpeed()) {
                    playbackSpeed = PlaybackSpeedUtils.getCurrentPlaybackSpeed(item.getMedia());
                }
                if (item.getMedia() != null) {
                    long itemTimeLeft = item.getMedia().getDuration() - item.getMedia().getPosition();
                    timeLeft += itemTimeLeft / playbackSpeed;
                }
            }
            info += " • ";
            info += getString(R.string.time_left_label);
            info += Converter.getDurationStringLocalized(getActivity(), timeLeft);
        }
        infoBar.setText(info);
    }

    private void loadItems(final boolean restoreScrollPosition) {
        Log.d(TAG, "loadItems()");
        if (disposable != null) {
            disposable.dispose();
        }
        if (queue == null) {
            emptyView.hide();
            progLoading.setVisibility(View.VISIBLE);
        }
        disposable = Observable.fromCallable(DBReader::getQueue)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    progLoading.setVisibility(View.GONE);
                    queue = items;
                    onFragmentLoaded(restoreScrollPosition);
                    if (recyclerAdapter != null) {
                        recyclerAdapter.notifyDataSetChanged();
                    }
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    @Override
    public void onStartSelectMode() {
        swipeActions.detach();
        speedDialView.setVisibility(View.VISIBLE);
        
        infoBar.setVisibility(View.GONE);
    }

    @Override
    public void onEndSelectMode() {
        speedDialView.close();
        speedDialView.setVisibility(View.GONE);
        infoBar.setVisibility(View.VISIBLE);
        swipeActions.attachTo(recyclerView);
    }

    private class QueueSwipeActions extends SwipeActions {

        // Position tracking whilst dragging
        int dragFrom = -1;
        int dragTo = -1;

        public QueueSwipeActions() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, QueueFragment.this, TAG);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getBindingAdapterPosition();
            int toPosition = target.getBindingAdapterPosition();

            // Update tracked position
            if (dragFrom == -1) {
                dragFrom =  fromPosition;
            }
            dragTo = toPosition;

            int from = viewHolder.getBindingAdapterPosition();
            int to = target.getBindingAdapterPosition();
            Log.d(TAG, "move(" + from + ", " + to + ") in memory");
            if (from >= queue.size() || to >= queue.size() || from < 0 || to < 0) {
                return false;
            }
            queue.add(to, queue.remove(from));
            recyclerAdapter.notifyItemMoved(from, to);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (disposable != null) {
                disposable.dispose();
            }

            //SwipeActions
            super.onSwiped(viewHolder, direction);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            // Check if drag finished
            if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                reallyMoved(dragFrom, dragTo);
            }

            dragFrom = dragTo = -1;
        }

        private void reallyMoved(int from, int to) {
            // Write drag operation to database
            Log.d(TAG, "Write to database move(" + from + ", " + to + ")");
            DBWriter.moveQueueItem(from, to, true);
        }

    }
}
