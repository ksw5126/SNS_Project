package com.example.sns_project.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.sns_project.R;
import com.example.sns_project.PostInfo;
import com.example.sns_project.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

public class WritePostActivity extends BasicActivity {

    private static final String TAG = "WritePostActivity";
    private FirebaseUser user;
    private ArrayList<String> pathList = new ArrayList<>();
    private LinearLayout parent;
    private int pathCount;
    private int successCount;
    private RelativeLayout buttonsBackgroundLayout;
    private RelativeLayout loaderlayout;
    private ImageView selectedImageView;
    private EditText selectedEditText;
    private EditText contentsEditText, titleEditText;
    private PostInfo postInfo;
    private StorageReference storageRef;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        parent = findViewById(R.id.contentsLayout);
        buttonsBackgroundLayout = (RelativeLayout)findViewById(R.id.buttonsBackgroundLayout);
        loaderlayout = findViewById(R.id.loaderLayout);
        contentsEditText = findViewById(R.id.contentsEditText);
        titleEditText = findViewById(R.id.titleEditText);

        findViewById(R.id.check).setOnClickListener(onClickListener);
        findViewById(R.id.image).setOnClickListener(onClickListener);
        findViewById(R.id.video).setOnClickListener(onClickListener);

        findViewById(R.id.imageModify).setOnClickListener(onClickListener);
        findViewById(R.id.videoModify).setOnClickListener(onClickListener);
        findViewById(R.id.delete).setOnClickListener(onClickListener);

        buttonsBackgroundLayout.setOnClickListener(onClickListener);
        contentsEditText.setOnFocusChangeListener(onFocusChangeListener);
        titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    selectedEditText = null;
                }
            }
        });

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        util = new Util(this);

        postInfo = (PostInfo)getIntent().getSerializableExtra("postInfo");
        postInit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0: {
                if (resultCode == Activity.RESULT_OK) {
                    String profilePath = data.getStringExtra("profilePath");
                    pathList.add(profilePath);

                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams
                            (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        LinearLayout linearLayout = new LinearLayout(WritePostActivity.this);
                    linearLayout.setLayoutParams(layoutParams);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);

                    if(selectedEditText == null) {
                        parent.addView(linearLayout);
                    } else {
                        for (int i = 0; i < parent.getChildCount(); i++) {
                            if(parent.getChildAt(i) == selectedEditText.getParent()) {
                                parent.addView(linearLayout, i+1);
                                break;
                            }
                        }
                    }

                    ImageView imageView = new ImageView(WritePostActivity.this);
                    imageView.setLayoutParams(layoutParams);
                    // 이미지, 영상 올릴시 화면에 가득 차보이게 (아래2줄)
                    imageView.setAdjustViewBounds(true);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonsBackgroundLayout.setVisibility(View.VISIBLE);
                            selectedImageView = (ImageView)v;
                        }
                    });
                    Glide.with(this).load(profilePath).override(1000).into(imageView);
                    linearLayout.addView(imageView);

                    EditText editText = new EditText(WritePostActivity.this);
                    editText.setLayoutParams(layoutParams);
                    editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
                    editText.setHint("내용");
                    linearLayout.addView(editText);
                    editText.setOnFocusChangeListener(onFocusChangeListener);

                }
                break;
            }
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    String profilePath = data.getStringExtra("profilePath");
                    pathList.set(parent.indexOfChild((View)selectedImageView.getParent())-1, profilePath);
                    Glide.with(this).load(profilePath).override(1000).into(selectedImageView);
                }
                break;
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.check:
                    storageUpload();
                    break;
                case R.id.image:
                    MyStartActivity(GalleryActivity.class, "image" , 0);
                    break;
                case R.id.video:
                    MyStartActivity(GalleryActivity.class, "video", 0);
                    break;
                case R.id.buttonsBackgroundLayout:
                    if(buttonsBackgroundLayout.getVisibility() == View.VISIBLE) {
                        buttonsBackgroundLayout.setVisibility(View.GONE);
                    }
                    break;
                case R.id.imageModify:
                    MyStartActivity(GalleryActivity.class, "image", 1);
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                    break;
                case R.id.videoModify :
                    MyStartActivity(GalleryActivity.class, "video", 1);
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                    break;
                case R.id.delete :
                    // view로 형변환
                    View selectedView = (View)selectedImageView.getParent();

                    String[] list = pathList.get(parent.indexOfChild(selectedView)-1).split("\\?");
                    String[] list2 = list[0].split("%2F");
                    String name = list2[list2.length -1];

                    StorageReference desertRef = storageRef.child("posts/" + postInfo.getId() + "/" + name);
                    desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            util.showToast("파일삭제!");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            util.showToast("문제가 발생했습니다!");
                        }
                    });
                    pathList.remove(parent.indexOfChild(selectedView)-1);
                    parent.removeView(selectedView);
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                selectedEditText = (EditText)v;
            }
        }
    };

    private void storageUpload() {

        final String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();

        if (title.length() > 0) {

            final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);
            loaderLayout.setVisibility(View.VISIBLE);

            final ArrayList<String> contentsList = new ArrayList<>();
            user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

            final DocumentReference documentReference = postInfo == null ?
                    firebaseFirestore.collection("posts").document()
                    : firebaseFirestore.collection("posts").document(postInfo.getId());

            final Date date = postInfo == null ? new Date() : postInfo.getCreatedAt();

            for (int i = 0; i < parent.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout)parent.getChildAt(i);
                for(int ii = 0 ;ii < linearLayout.getChildCount(); ii++) {
                    View view = linearLayout.getChildAt(ii);
                    if (view instanceof EditText) {
                        String text = ((EditText) view).getText().toString();
                        if (text.length() > 0) {
                            contentsList.add(text);
                        }
                    } else if(!Patterns.WEB_URL.matcher(pathList.get(pathCount)).matches()){
                        String path = pathList.get(pathCount);
                        successCount++;
                        contentsList.add(path);
                        // 파일 확장자에 따른 설정 (.png, .mp4 ...)
                        String[] pathArray = path.split("\\.");

                        final StorageReference mountainImagesRef = storageRef.child
                                ("posts/" + documentReference.getId() + "/" + pathCount + "." + pathArray[pathArray.length-1]);

                        try {
                            InputStream stream = new FileInputStream(new File(pathList.get(pathCount)));
                            // meta - data add.
                            StorageMetadata metadata = new StorageMetadata.Builder()
                                    .setCustomMetadata("index", "" + (contentsList.size() - 1)).build();

                            UploadTask uploadTask = mountainImagesRef.putStream(stream, metadata);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    final int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));

                                    mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            successCount--;
                                            contentsList.set(index, uri.toString());
                                            if (successCount == 0) {
                                                PostInfo postInfo = new PostInfo(title, contentsList, user.getUid(), date);
                                                storeUpload(documentReference, postInfo);
                                            }
                                        }
                                    });
                                }
                            });
                        } catch (FileNotFoundException e) {
                            Log.e("로그", "에러 :" + e.toString());
                        }
                        pathCount++;
                }
                }
            }
            if(successCount == 0 ) {
                storeUpload(documentReference, new PostInfo(title, contentsList, user.getUid(), date));
            }

            }  else{
                    StartToast("제목!");
        }

    }


    private void storeUpload(DocumentReference documentReference , PostInfo postInfo) {
        documentReference.set(postInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        loaderlayout.setVisibility(View.GONE);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        loaderlayout.setVisibility(View.GONE);
                    }
                });
    }


    // 수정시 제목 내용이 수정전과 동일하게 나오게 하기위한
    private void postInit() {
        if(postInfo != null) {
            titleEditText.setText(postInfo.getTitle());
            ArrayList<String> contentsList = postInfo.getContents();
            for(int i = 0 ; i< contentsList.size(); i++) {
                String contents = contentsList.get(i);
                if (Patterns.WEB_URL.matcher(contents).matches() && contents.contains("https://firebasestorage.googleapis.com/v0/b/sns-project-4dab8.appspot.com/o/post")) {
                    pathList.add(contents);

                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams
                            (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    LinearLayout linearLayout = new LinearLayout(WritePostActivity.this);
                    linearLayout.setLayoutParams(layoutParams);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    parent.addView(linearLayout);

                    ImageView imageView = new ImageView(WritePostActivity.this);
                    imageView.setLayoutParams(layoutParams);
                    // 이미지, 영상 올릴시 화면에 가득 차보이게
                    imageView.setAdjustViewBounds(true);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonsBackgroundLayout.setVisibility(View.VISIBLE);
                            selectedImageView = (ImageView)v;
                        }
                    });
                    Glide.with(this).load(contents).override(1000).into(imageView);
                    linearLayout.addView(imageView);

                    EditText editText = new EditText(WritePostActivity.this);
                    editText.setLayoutParams(layoutParams);
                    editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
                    editText.setHint("내용");
                    if(i<contentsList.size()-1) {
                       String nextContents = contentsList.get(i+1);
                       if(Patterns.WEB_URL.matcher(nextContents).matches() || !nextContents.contains("https://firebasestorage.googleapis.com/v0/b/sns-project-4dab8.appspot.com/o/post")) {
                            editText.setText(nextContents);
                       }
                    }
                    linearLayout.addView(editText);
                    editText.setOnFocusChangeListener(onFocusChangeListener);
                } else if(i ==0) {
                    contentsEditText.setText(contents);
                }
            }
        }
    }

    private void StartToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void MyStartActivity(Class c, String media, int requestCode) {
        Intent intent = new Intent(this, c);
        intent.putExtra("media", media);
        startActivityForResult(intent, requestCode);
    }
}
