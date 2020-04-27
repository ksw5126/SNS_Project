/*
 2020.04.27
 */
package com.example.sns_project.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.sns_project.PostInfo;
import com.example.sns_project.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.example.sns_project.Util.isStorageUrl;

public class ReadContentsView extends LinearLayout {

    private Context context;
    private int moreIndex = -1;

    public ReadContentsView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public ReadContentsView(Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        initView();
    }

    private void initView() {

        setLayoutParams(new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_post, this, true);

    }

    public void setMoreIndex(int moreIndex) {
        this.moreIndex = moreIndex;
    }

    public void setPostInfo(PostInfo postInfo) {
        TextView createdTextView = findViewById(R.id.createAtTextView);
        createdTextView.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(postInfo.getCreatedAt()));

        LinearLayout contentsLayout = findViewById(R.id.contentsLayout);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ArrayList<String> contentsList = postInfo.getContents();

        // 실행시 이미지 3개이상 올려도 보이는건 2개로 제한.
        for(int i = 0 ; i< contentsList.size(); i++) {
            if (i == moreIndex) {
                TextView textView = new TextView(context);
                textView.setLayoutParams(layoutParams);
                textView.setText("더보기...");
                contentsLayout.addView(textView);
                break;
            }
            String contents = contentsList.get(i);
            if (isStorageUrl(contents)) {
                ImageView imageView = new ImageView(context);
                imageView.setLayoutParams(layoutParams);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                contentsLayout.addView(imageView);
                Glide.with(this).load(contents).override(1000).thumbnail(0.1f).into(imageView);
            } else {
                TextView textView = new TextView(context);
                textView.setLayoutParams(layoutParams);
                textView.setText(contents);
                textView.setTextColor(Color.rgb(0,0,0));
                contentsLayout.addView(textView);
            }
        }
    }
}


