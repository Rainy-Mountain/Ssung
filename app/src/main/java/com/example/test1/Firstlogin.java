package com.example.test1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.test1.databinding.ActivityFirstloginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Firstlogin extends AppCompatActivity implements View.OnClickListener {
    ActivityFirstloginBinding binding;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();    //파이어베이스 초기화, authentication 연결
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFirstloginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseApp.initializeApp(this);

        binding.btnCheckNickname.setOnClickListener(this);
        binding.btnSignup.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view==binding.btnCheckNickname){
            checkNickname();
        }
        else if(view==binding.btnSignup) {
            signUp();
        }
    }

    private void signUp() {
        String Email=binding.etEmail.getText().toString();
        String Nickname=binding.etNickname.getText().toString();
        String Password=binding.etPassword.getText().toString();
        String ConfirmPassword=binding.etConfirmPassword.getText().toString();

        if(Email.length()>0&&Nickname.length()>0&&Password.length()>0){
            if(Password.equals(ConfirmPassword)){
                mAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(this, task-> {
                    if(task.isSuccessful()) {
                        Log.d("Signup", "회원가입 성공");
                        // 회원가입 성공
                        Toast.makeText(Firstlogin.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        // Firebase Auth에서 생성된 사용자 ID 가져오기
                        String userNickname = mAuth.getCurrentUser().getUid();

                        // Firestore에 닉네임 저장
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("nickname", Nickname);

                        db.collection("users").document(userNickname)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore","닉네임 저장에 성공했습니다.");
                                    Intent intent=new Intent(Firstlogin.this, Login.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore","닉네임 저장에 실패했습니다.");
                                });
                    }
                    else {
                        // 예외가 발생한 경우 예외 메시지를 로깅하거나 사용자에게 보여주기
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e("SignupError", "failed signup: " + exception.getMessage());
                            Toast.makeText(Firstlogin.this, "회원가입에 실패했습니다: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("SignupError", "failed signup too bad");
                            Toast.makeText(Firstlogin.this, "알 수 없는 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else{
                Toast.makeText(Firstlogin.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(Firstlogin.this, "아이디, 닉네임, 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkNickname() {
        String Nickname=binding.etNickname.getText().toString();
        CollectionReference userDB = db.collection("users");
        // 닉네임 중복 확인
        userDB.whereEqualTo("nickname", Nickname)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // 아이디가 존재하지 않으면 중복되지 않음
                            Log.d("checkId", "데이터 중복 안 됨, 가입 진행 가능");
                            Toast.makeText(getApplicationContext(), "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            // 아이디가 존재하면 중복됨
                            Log.d("checkId", "데이터 중복 됨, 가입 진행 불가");
                            Toast.makeText(getApplicationContext(), "이미 사용중인 아이디입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Firestore 쿼리 오류 처리
                        Log.e("checkId", "아이디 중복 확인 오류", task.getException());
                        Toast.makeText(getApplicationContext(), "nickname error.", Toast.LENGTH_SHORT).show();

                    }
                });
    }
}