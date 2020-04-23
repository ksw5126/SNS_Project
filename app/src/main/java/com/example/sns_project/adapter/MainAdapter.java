package com.example.sns_project.adapter;

import android.app.Activity;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {
    private ArrayList<PostInfo> mDataset;
    private Activity activity;
    private FirebaseFirestore firebaseFirestore;

    static class MainViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
         CardView cardView;

        MainViewHolder(Activity activity , CardView v, PostInfo postInfo) {
            super(v);
            cardView = v;

             LinearLayout contentsLayout = cardView.findViewById(R.id.contentsLayout);
             ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams
                     (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
             ArrayList<String> contentsList = postInfo.getContents();

        if(contentsLayout.getChildCount() == 0) {
            for(int i = 0; i < contentsList.size(); i++) {
                String contents = contentsList.get(i);
                if( Patterns.WEB_URL.matcher(contents).matches() ) {
                    ImageView imageView = new ImageView(activity);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setAdjustViewBounds(true);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    contentsLayout.addView(imageView);
                } else {
                    TextView textView = new TextView(activity);
                    textView.setLayoutParams(layoutParams);
                    contentsLayout.addView(textView);
                }
            }
        }

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MainAdapter(Activity activity, ArrayList<PostInfo> myDataset) {
        this.activity = activity;
        mDataset = myDataset;
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);

        final MainViewHolder mainViewHolder = new MainViewHolder(activity, cardView, mDataset.get(viewType) );
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent resultIntent = new Intent();
//                resultIntent.putExtra("profilePath", mDataset.get(galleryViewHolder.getAdapterPosition()));
//                activity.setResult(Activity.RESULT_OK, resultIntent);
//                activity.finish();
            }
        });


        cardView.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, mainViewHolder.getAdapterPosition());
            }
        });

        return mainViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MainViewHolder holder, final int position) {

        CardView cardView = holder.cardView;
        TextView titleTextView = cardView.findViewById(R.id.titleTextView);
        titleTextView.setText(mDataset.get(position).getTitle());

        TextView createdTextView = cardView.findViewById(R.id.createAtTextView);
        createdTextView.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(mDataset.get(position).getCreatedAt()));

        LinearLayout contentsLayout = cardView.findViewById(R.id.contentsLayout);
        ArrayList<String> contentsList = mDataset.get(position).getContents();

        // contents가 url or image or text 확인
        for(int i = 0; i < contentsList.size(); i++) {
            String contents = contentsList.get(i);
            if( Patterns.WEB_URL.matcher(contents).matches() ) {
                Glide.with(activity).load(contents).override(1000).thumbnail(0.1f).into((ImageView)contentsLayout.getChildAt(i));
            } else {
                ((TextView)contentsLayout.getChildAt(i)).setText(contents);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void showPopup(View v, final int position) {
        PopupMenu popup = new PopupMenu(activity, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.post, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.modify :

                        break;
                    case R.id.delete :

                        firebaseFirestore.collection("posts").document(mDataset.get(position).getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(activity, "게시글 삭제완료", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(activity, "게시글 삭제완료하지 못했습니다.", Toast.LENGTH_SHORT).show();

                                    }
                                });
                        break;
                }
                return false;
            }
        });
        popup.show();
    }



}
