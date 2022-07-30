package com.tuneurl.podcastplayer.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.tuneurl.podcastplayer.core.event.DownloadEvent;
import com.tuneurl.podcastplayer.core.event.DownloadLogEvent;
import com.tuneurl.podcastplayer.core.event.DownloaderUpdate;
import com.tuneurl.podcastplayer.core.service.download.Downloader;
import com.tuneurl.podcastplayer.core.storage.DBReader;
import com.tuneurl.podcastplayer.core.storage.DBWriter;
import com.tuneurl.podcastplayer.databinding.DownloadLogFragmentBinding;
import com.tuneurl.podcastplayer.model.download.DownloadStatus;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tuneurl.podcastplayer.R;
import com.tuneurl.podcastplayer.adapter.DownloadLogAdapter;
import com.tuneurl.podcastplayer.dialog.DownloadLogDetailsDialog;
import com.tuneurl.podcastplayer.view.EmptyViewHandler;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the download log
 */
public class DownloadLogFragment extends BottomSheetDialogFragment
        implements AdapterView.OnItemClickListener, Toolbar.OnMenuItemClickListener {
    private static final String TAG = "DownloadLogFragment";

    private List<DownloadStatus> downloadLog = new ArrayList<>();
    private List<Downloader> runningDownloads = new ArrayList<>();
    private DownloadLogAdapter adapter;
    private Disposable disposable;
    private DownloadLogFragmentBinding viewBinding;

    @Override
    public void onStart() {
        super.onStart();
        loadDownloadLog();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = DownloadLogFragmentBinding.inflate(inflater);
        viewBinding.toolbar.inflateMenu(R.menu.download_log);
        viewBinding.toolbar.setOnMenuItemClickListener(this);

        EmptyViewHandler emptyView = new EmptyViewHandler(getActivity());
        emptyView.setIcon(R.drawable.ic_download);
        emptyView.setTitle(R.string.no_log_downloads_head_label);
        emptyView.setMessage(R.string.no_log_downloads_label);
        emptyView.attachToListView(viewBinding.list);

        adapter = new DownloadLogAdapter(getActivity());
        viewBinding.list.setAdapter(adapter);
        viewBinding.list.setOnItemClickListener(this);
        EventBus.getDefault().register(this);
        return viewBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = adapter.getItem(position);
        if (item instanceof DownloadStatus) {
            new DownloadLogDetailsDialog(getContext(), (DownloadStatus) item).show();
        }
    }

    @Subscribe
    public void onDownloadLogChanged(DownloadLogEvent event) {
        loadDownloadLog();
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.clear_logs_item).setVisible(!downloadLog.isEmpty());
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == R.id.clear_logs_item) {
            DBWriter.clearDownloadLog();
            return true;
        }
        return false;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(DownloadEvent event) {
        Log.d(TAG, "onEvent() called with: " + "event = [" + event + "]");
        DownloaderUpdate update = event.update;
        runningDownloads = update.downloaders;
        adapter.setRunningDownloads(runningDownloads);
    }

    private void loadDownloadLog() {
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Observable.fromCallable(DBReader::getDownloadLog)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result != null) {
                        downloadLog = result;
                        adapter.setDownloadLog(downloadLog);
                    }
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }
}
