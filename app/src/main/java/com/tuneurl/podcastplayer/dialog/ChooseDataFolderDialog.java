package com.tuneurl.podcastplayer.dialog;

import android.content.Context;

import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuneurl.podcastplayer.adapter.DataFolderAdapter;

import com.tuneurl.podcastplayer.R;

public class ChooseDataFolderDialog {

    public static void showDialog(final Context context, Consumer<String> handlerFunc) {

        View content = View.inflate(context, R.layout.choose_data_folder_dialog, null);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(content)
                .setTitle(R.string.choose_data_directory)
                .setMessage(R.string.choose_data_directory_message)
                .setNegativeButton(R.string.cancel_label, null)
                .create();
        ((RecyclerView) content.findViewById(R.id.recyclerView)).setLayoutManager(new LinearLayoutManager(context));

        DataFolderAdapter adapter = new DataFolderAdapter(context, path -> {
            dialog.dismiss();
            handlerFunc.accept(path);
        });
        ((RecyclerView) content.findViewById(R.id.recyclerView)).setAdapter(adapter);

        if (adapter.getItemCount() > 0) {
            dialog.show();
        } else {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.error_label)
                    .setMessage(R.string.external_storage_error_msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

}