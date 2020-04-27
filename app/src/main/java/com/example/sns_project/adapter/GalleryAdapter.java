package com.example.sns_project.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sns_project.R;

import java.util.ArrayList;

import static com.example.sns_project.Util.INTENT_PATH;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    private ArrayList<String> mDataset;
    private Activity activity;

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
         CardView cardView;
         GalleryViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public GalleryAdapter(Activity activity, ArrayList<String> myDataset) {
        this.activity = activity;
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GalleryAdapter.GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery, parent, false);

        final GalleryViewHolder galleryViewHolder = new GalleryViewHolder(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(INTENT_PATH, mDataset.get(galleryViewHolder.getAdapterPosition()));
                activity.setResult(Activity.RESULT_OK, resultIntent);
                activity.finish();
            }
        });

        return galleryViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryViewHolder holder, final int position) {
        final CardView cardView = holder.cardView;

        ImageView imageView = cardView.findViewById(R.id.galleryImageView);
        Glide.with(activity).load(mDataset.get(position)).centerCrop().override(500).into(imageView);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
