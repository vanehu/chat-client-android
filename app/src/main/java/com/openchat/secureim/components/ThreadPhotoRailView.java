package com.openchat.secureim.components;


import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.openchat.secureim.R;
import com.openchat.secureim.crypto.MasterSecret;
import com.openchat.secureim.database.CursorRecyclerViewAdapter;
import com.openchat.secureim.database.MediaDatabase;
import com.openchat.secureim.mms.GlideRequests;
import com.openchat.secureim.mms.Slide;
import com.openchat.secureim.util.MediaUtil;
import com.openchat.secureim.util.ViewUtil;

public class ThreadPhotoRailView extends FrameLayout {

  @NonNull  private final RecyclerView          recyclerView;
  @Nullable private       OnItemClickedListener listener;

  public ThreadPhotoRailView(Context context) {
    this(context, null);
  }

  public ThreadPhotoRailView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ThreadPhotoRailView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    inflate(context, R.layout.recipient_preference_photo_rail, this);

    this.recyclerView = ViewUtil.findById(this, R.id.photo_list);
    this.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
    this.recyclerView.setItemAnimator(new DefaultItemAnimator());
    this.recyclerView.setNestedScrollingEnabled(false);
  }

  public void setListener(@Nullable OnItemClickedListener listener) {
    this.listener = listener;

    if (this.recyclerView.getAdapter() != null) {
      ((ThreadPhotoRailAdapter)this.recyclerView.getAdapter()).setListener(listener);
    }
  }

  public void setCursor(@NonNull MasterSecret masterSecret, @NonNull GlideRequests glideRequests, @Nullable Cursor cursor) {
    this.recyclerView.setAdapter(new ThreadPhotoRailAdapter(getContext(), masterSecret, glideRequests, cursor, this.listener));
  }

  private static class ThreadPhotoRailAdapter extends CursorRecyclerViewAdapter<ThreadPhotoRailAdapter.ThreadPhotoViewHolder> {

    private static final String TAG = ThreadPhotoRailAdapter.class.getName();

    @NonNull  private final MasterSecret  masterSecret;
    @NonNull  private final GlideRequests glideRequests;

    @Nullable private OnItemClickedListener clickedListener;

    private ThreadPhotoRailAdapter(@NonNull Context context,
                                   @NonNull MasterSecret masterSecret,
                                   @NonNull GlideRequests glideRequests,
                                   @Nullable Cursor cursor,
                                   @Nullable OnItemClickedListener listener)
    {
      super(context, cursor);
      this.masterSecret    = masterSecret;
      this.glideRequests   = glideRequests;
      this.clickedListener = listener;
    }

    @Override
    public ThreadPhotoViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
      View itemView = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.recipient_preference_photo_rail_item, parent, false);

      return new ThreadPhotoViewHolder(itemView);
    }

    @Override
    public void onBindItemViewHolder(ThreadPhotoViewHolder viewHolder, @NonNull Cursor cursor) {
      ThumbnailView             imageView   = viewHolder.imageView;
      MediaDatabase.MediaRecord mediaRecord = MediaDatabase.MediaRecord.from(getContext(), masterSecret, cursor);
      Slide                     slide       = MediaUtil.getSlideForAttachment(getContext(), mediaRecord.getAttachment());

      if (slide != null) {
        imageView.setImageResource(masterSecret, glideRequests, slide, false, false);
      }

      imageView.setOnClickListener(v -> {
        if (clickedListener != null) clickedListener.onItemClicked(mediaRecord);
      });
    }

    public void setListener(@Nullable OnItemClickedListener listener) {
      this.clickedListener = listener;
    }

    static class ThreadPhotoViewHolder extends RecyclerView.ViewHolder {

      ThumbnailView imageView;

      ThreadPhotoViewHolder(View itemView) {
        super(itemView);

        this.imageView = ViewUtil.findById(itemView, R.id.thumbnail);
      }
    }
  }

  public interface OnItemClickedListener {
    public void onItemClicked(MediaDatabase.MediaRecord mediaRecord);
  }
}
