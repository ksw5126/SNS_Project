package com.example.sns_project.listener;

import com.example.sns_project.PostInfo;

public interface OnPostListener {

    void onModify();
    void onDelete(PostInfo postInfo);

}
