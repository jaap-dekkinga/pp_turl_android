package com.dekidea.hearact.dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.dekidea.hearact.core.storage.DBReader;
import com.dekidea.hearact.event.MessageEvent;
import com.dekidea.hearact.model.download.DownloadStatus;
import com.dekidea.hearact.model.feed.Feed;
import com.dekidea.hearact.model.feed.FeedMedia;

import com.dekidea.hearact.R;

import org.greenrobot.eventbus.EventBus;

public class DownloadLogDetailsDialog extends AlertDialog.Builder {

    public DownloadLogDetailsDialog(@NonNull Context context, DownloadStatus status) {
        super(context);

        String url = "unknown";
        String message = context.getString(R.string.download_successful);
        if (status.getFeedfileType() == FeedMedia.FEEDFILETYPE_FEEDMEDIA) {
            FeedMedia media = DBReader.getFeedMedia(status.getFeedfileId());
            if (media != null) {
                url = media.getDownload_url();
            }
        } else if (status.getFeedfileType() == Feed.FEEDFILETYPE_FEED) {
            Feed feed = DBReader.getFeed(status.getFeedfileId());
            if (feed != null) {
                url = feed.getDownload_url();
            }
        }

        if (!status.isSuccessful()) {
            message = status.getReasonDetailed();
        }

        String messageFull = context.getString(R.string.download_error_details_message, message, url);
        setTitle(R.string.download_error_details);
        setMessage(messageFull);
        setPositiveButton(android.R.string.ok, null);
        setNeutralButton(R.string.copy_to_clipboard, (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(context.getString(R.string.download_error_details), messageFull);
            clipboard.setPrimaryClip(clip);
            EventBus.getDefault().post(new MessageEvent(context.getString(R.string.copied_to_clipboard)));
        });
    }

    @Override
    public AlertDialog show() {
        AlertDialog dialog = super.show();
        ((TextView) dialog.findViewById(android.R.id.message)).setTextIsSelectable(true);
        return dialog;
    }
}
