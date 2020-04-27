package com.example.sns_project.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.example.sns_project.Util;
import com.example.sns_project.adapter.MainAdapter;
import com.example.sns_project.listener.OnPostListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.Util.isStorageUrl;
import static com.example.sns_project.Util.showToast;
import static com.example.sns_project.Util.storageUrlToName;

public class MainActivity extends BasicActivity {

    private static final String TAG = "MainActivity";
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private MainAdapter mainAdapter;
    private ArrayList<PostInfo> postList;
    private StorageReference storageRef;
    private int successCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser == null) {
            MyStartActivity(SignUpActivity.class);
        } else {
            firebaseFirestore = FirebaseFirestore.getInstance();
            DocumentReference docRef = firebaseFirestore.collection("users").document(firebaseUser.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document != null) {
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                                MyStartActivity(MemberInitActivity.class);
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }

        postList = new ArrayList<>();
        mainAdapter = new MainAdapter(MainActivity.this, postList);
        mainAdapter.setOnPostListener(onPostListener);

//        findViewById(R.id.logoutButton).setOnClickListener(onClickListener);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        findViewById(R.id.floatingAddButton).setOnClickListener(onClickListener);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(mainAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PostUpdate();
    }

    OnPostListener onPostListener = new OnPostListener() {
        @Override
        public void onDelete(int position) {
            final String id = postList.get(position).getId();
            ArrayList<String> contentsList = postList.get(position).getContents();
            // 실행시 이미지 3개이상 올려도 보이는건 2개로 제한.
            for(int i = 0 ; i< contentsList.size(); i++) {
                String contents = contentsList.get(i);
                if (isStorageUrl(contents)) {
                    successCount++;

                    // Create a reference to the file to delete
                    StorageReference desertRef = storageRef.child("posts/" + id + "/" + storageUrlToName(contents));

                    // Delete the file
                    desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            successCount--;
                            storeUploader(id);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            showToast(MainActivity.this,"문제가 발생했습니다!");
                        }
                    });
                }
            }
            storeUploader(id);
        }

        @Override
        public void onModify(int position) {
            MyStartActivity(WritePostActivity.class, postList.get(position));
        }
    };

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
//                case R.id.logoutButton :
//                    FirebaseAuth.getInstance().signOut();
//                    MyStartActivity(SignUpActivity.class);
//                    break;
                case R.id.floatingAddButton:
                    MyStartActivity(WritePostActivity.class);
                    break;
            }
        }
    };

    // 삭제나 수정시 바로바로 업데이트 !
    private void PostUpdate() {

        if(firebaseUser != null) {
//            firebaseFirestore = FirebaseFirestore.getInstance();
            CollectionReference collectionReference = firebaseFirestore.collection("posts");

            collectionReference
                    .orderBy("createdAt", Query.Direction.DESCENDING).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                postList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    postList.add(new PostInfo(
                                            document.getData().get("title").toString(),
                                            (ArrayList<String>)document.getData().get("contents"),
                                            document.getData().get("publisher").toString(),
                                            new Date(document.getDate("createdAt").getTime()),
                                            document.getId()
                                    ));
                                }
                                mainAdapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }

    private void storeUploader(String id) {
        if (successCount == 0) {
            firebaseFirestore.collection("posts").document(id)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showToast(MainActivity.this,"게시글 삭제 완료!");
                            PostUpdate();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showToast(MainActivity.this,"게시글 삭제 실패!");
                        }
                    });
        }
    }

    private void MyStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    private void MyStartActivity(Class c, PostInfo postInfo) {
        Intent intent = new Intent(this, c);
        intent.putExtra("postInfo", postInfo);
        startActivity(intent);
    }
}
