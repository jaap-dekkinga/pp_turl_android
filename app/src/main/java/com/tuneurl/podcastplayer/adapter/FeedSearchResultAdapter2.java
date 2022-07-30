package com.tuneurl.podcastplayer.adapter;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tuneurl.podcastplayer.activity.MainActivity;
import com.tuneurl.podcastplayer.activity.MainActivity2;
import com.tuneurl.podcastplayer.fragment.FeedItemlistFragment;
import com.tuneurl.podcastplayer.model.feed.Feed;
import com.tuneurl.podcastplayer.ui.common.SquareImageView;

import com.tuneurl.podcastplayer.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FeedSearchResultAdapter2 extends RecyclerView.Adapter<FeedSearchResultAdapter2.Holder> {

    private final WeakReference<MainActivity2> mainActivityRef;
    private final List<Feed> data = new ArrayList<>();

    public FeedSearchResultAdapter2(MainActivity2 mainActivity) {
        this.mainActivityRef = new WeakReference<>(mainActivity);
    }

    public void updateData(List<Feed> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = View.inflate(mainActivityRef.get(), R.layout.searchlist_item_feed, null);
        return new Holder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final Feed podcast = data.get(position);
        holder.imageView.setContentDescription(podcast.getTitle());
        holder.imageView.setOnClickListener(v ->
                mainActivityRef.get().loadChildFragment(FeedItemlistFragment.newInstance(podcast.getId())));

        Glide.with(mainActivityRef.get())
                .load(podcast.getImageUrl())
                .apply(new RequestOptions()
                        .placeholder(R.color.light_gray)
                        .fitCenter()
                        .dontAnimate())
                .into(holder.imageView);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        SquareImageView imageView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.discovery_cover);
            imageView.setDirection(SquareImageView.DIRECTION_HEIGHT);
        }
    }
}
