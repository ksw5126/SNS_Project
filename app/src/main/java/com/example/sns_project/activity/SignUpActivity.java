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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.sns_project.Util.showToast;

public class SignUpActivity extends BasicActivity {

    private FirebaseAuth mAuth; // 인스턴스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setToolbarTitle("회원가입");

        mAuth = FirebaseAuth.getInstance(); // 인스턴스 초기화

        findViewById(R.id.SignUpButton).setOnClickListener(onClickListener);
        findViewById(R.id.gotoLoginButton).setOnClickListener(onClickListener);

    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.SignUpButton:
                    signUp();
                    break;
                case R.id.gotoLoginButton :
                    MyStartActivity(LoginActivity.class);
            }
        }
    };

    private void signUp() {
        String email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
        String password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
        String passwordCheck = ((EditText)findViewById(R.id.passwordcheckEditText)).getText().toString();

        if(email.length() > 0 && password.length() > 0 && passwordCheck.length() > 0 ) {
            if (password.equals(passwordCheck)) {
                final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);
                loaderLayout.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                loaderLayout.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    showToast(SignUpActivity.this,"회원가입을 성공 했습니다.");
                                    MyStartActivity(MainActivity.class);
                                } else {
                                    if(task.getException() != null) {
                                        showToast(SignUpActivity.this,task.getException().toString());
                                    }
                                }
                            }
                        });
            } else {
                showToast(SignUpActivity.this,"비밀번호가 일치하지 않습니다.");
            }
        }
        else {
            showToast(SignUpActivity.this,"이메일 또는 비밀번호를 입력해주세요.");
        }
    }

    private void MyStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
