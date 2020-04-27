/*
    2020.04.27
 */
package com.example.sns_project.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.example.sns_project.FirebaseHelper;
import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.example.sns_project.listener.OnPostListener;
import com.example.sns_project.view.ContentsItemView;
import com.example.sns_project.view.ReadContentsView;

import static com.example.sns_project.Util.INTENT_PATH;

public class PostActivity extends BasicActivity {

    private PostInfo postInfo;
    private FirebaseHelper firebaseHelper;
    private ReadContentsView readContentsView;
    private LinearLayout contentsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postInfo = (PostInfo)getIntent().getSerializableExtra("postInfo");
        setToolbarTitle(postInfo.getTitle());

        contentsLayout = findViewById(R.id.contentsLayout);
        readContentsView = findViewById(R.id.readContentsView);

        firebaseHelper = new FirebaseHelper(this);
        firebaseHelper.setOnPostListener(onPostListener);
        uiUpdate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    postInfo = (PostInfo)data.getSerializableExtra("postInfo");
                    contentsLayout.removeAllViews();
                    uiUpdate();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.modify :
                MyStartActivity(WritePostActivity.class, postInfo);
                return true;
            case R.id.delete :
                firebaseHelper.storageDelete(postInfo);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    OnPostListener onPostListener = new OnPostListener() {
        @Override
        public void onModify() {
            Log.e("로그", "수정 성공");
        }

        @Override
        public void onDelete() {
            Log.e("로그", "삭제 성공");
        }
    };

    private void uiUpdate() {
        setToolbarTitle(postInfo.getTitle());
        readContentsView.setPostInfo(postInfo);
    }

    private void MyStartActivity(Class c, PostInfo postInfo) {
        Intent intent = new Intent(this, c);
        intent.putExtra("postInfo", postInfo);
        startActivityForResult(intent, 0);
    }
}
