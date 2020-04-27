package com.example.sns_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sns_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import static com.example.sns_project.Util.showToast;

public class PasswordResetActivity extends BasicActivity {

    private FirebaseAuth mAuth; // 인스턴스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        setToolbarTitle("비밀번호 재설정");

        mAuth = FirebaseAuth.getInstance(); // 인스턴스 초기화

        findViewById(R.id.sendButton).setOnClickListener(onClickListener);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sendButton:
                    send();
//                    Log.e("클릭", "클릭");
                    break;
            }
        }
    };

    private void send() {

        String email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();

        if(email.length() > 0 ) {

            final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);
            loaderLayout.setVisibility(View.VISIBLE);

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loaderLayout.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                showToast(PasswordResetActivity.this,"이메일을 보냈습니다.");
//                                Log.d(TAG, "Email sent.");
                            }
                        }
                    });

            }
        else {
            showToast(PasswordResetActivity.this,"이메일을 입력해 주세요.");
        }


    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
