package com.tuneurl.podcastplayer.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;

import com.tuneurl.podcastplayer.activity.MainActivity2;
import com.tuneurl.podcastplayer.core.preferences.UserPreferences;
import com.tuneurl.podcastplayer.fragment.swipeactions.SwipeActions;

import com.tuneurl.podcastplayer.R;
import com.tuneurl.podcastplayer.fragment.swipeactions.SwipeActions2;
import com.tuneurl.podcastplayer.view.viewholder.EpisodeItemViewHolder2;

/**
 * List adapter for the queue.
 */
public class QueueRecyclerAdapter2 extends EpisodeItemListAdapter2 {
    private static final String TAG = "QueueRecyclerAdapter";

    private final SwipeActions2 swipeActions;
    private boolean dragDropEnabled;


    public QueueRecyclerAdapter2(MainActivity2 mainActivity, SwipeActions2 swipeActions) {
        super(mainActivity);
        this.swipeActions = swipeActions;
        dragDropEnabled = ! (UserPreferences.isQueueKeepSorted() || UserPreferences.isQueueLocked());
    }

    public void updateDragDropEnabled() {
        dragDropEnabled = ! (UserPreferences.isQueueKeepSorted() || UserPreferences.isQueueLocked());
        notifyDataSetChanged();
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void afterBindViewHolder(EpisodeItemViewHolder2 holder, int pos) {
        if (!dragDropEnabled || inActionMode()) {
            holder.dragHandle.setVisibility(View.GONE);
            holder.dragHandle.setOnTouchListener(null);
            holder.coverHolder.setOnTouchListener(null);
        } else {
            holder.dragHandle.setVisibility(View.VISIBLE);
            holder.dragHandle.setOnTouchListener((v1, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, "startDrag()");
                    swipeActions.startDrag(holder);
                }
                return false;
            });
            holder.coverHolder.setOnTouchListener((v1, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    boolean isLtr = holder.itemView.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR;
                    float factor = isLtr ? 1 : -1;
                    if (factor * event.getX() < factor * 0.5 * v1.getWidth()) {
                        Log.d(TAG, "startDrag()");
                        swipeActions.startDrag(holder);
                    } else {
                        Log.d(TAG, "Ignoring drag in right half of the image");
                    }
                }
                return false;
            });
        }

        holder.isInQueue.setVisibility(View.GONE);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.queue_context, menu);
        super.onCreateContextMenu(menu, v, menuInfo);

        if (!inActionMode()) {
            menu.findItem(R.id.multi_select).setVisible(true);
            final boolean keepSorted = UserPreferences.isQueueKeepSorted();
            if (getItem(0).getId() == getLongPressedItem().getId() || keepSorted) {
                menu.findItem(R.id.move_to_top_item).setVisible(false);
            }
            if (getItem(getItemCount() - 1).getId() == getLongPressedItem().getId() || keepSorted) {
                menu.findItem(R.id.move_to_bottom_item).setVisible(false);
            }
        } else {
            menu.findItem(R.id.move_to_top_item).setVisible(false);
            menu.findItem(R.id.move_to_bottom_item).setVisible(false);
        }
    }
}
