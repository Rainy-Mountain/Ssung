package com.example.test1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class Login extends AppCompatActivity {
    Button loginButton;
    ImageButton googleLoginButton;
    TextView registerButton;
    TextView findPassword;
    EditText editId, editPassword;
    private FirebaseAuth firebaseAuth;
    private static final int RC_SIGN_IN = 9001;

    // Google api 클라이언트
    private GoogleSignInClient mGoogleSignInClient;

    // 구글계정
    private GoogleSignInAccount gsa;
    // 파이어베이스 인증 객체 생성


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        firebaseAuth =  FirebaseAuth.getInstance();
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);
        editId = findViewById(R.id.editId);
        editPassword = findViewById(R.id.editPassword);
        findPassword = findViewById(R.id.findPassword);

        //가입 버튼이 눌리면
        registerButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //intent함수를 통해 register액티비티 함수를 호출한다.
                startActivity(new Intent(Login.this, Firstlogin.class));
            }
        });

        //비밀번호찾기 버튼이 눌리면
        findPassword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //intent함수를 통해 register액티비티 함수를 호출한다.
                startActivity(new Intent(Login.this,FindPasswordActivity.class));
            }
        });

        //로그인 버튼이 눌리면
        loginButton.setOnClickListener(new View.OnClickListener(){

            // @Override
            public void onClick(View v) {
                String email = editId.getText().toString().trim();
                String pwd = editPassword.getText().toString().trim();
                firebaseAuth.signInWithEmailAndPassword(email,pwd)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Intent intent = new Intent(Login.this, MainActivity.class); //fragment1(map)으로 넘어가야함
                                    startActivity(intent);

                                }else{
                                    Toast.makeText(Login.this,"로그인 오류",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // google 로그인을 앱에 통합
        // GoogleSignInOptions객체를 구성할 때 requestIdToken을 호출

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        googleLoginButton = findViewById(R.id.googleLogin);
        googleLoginButton.setOnClickListener(view -> {
            signIn();
        });
    }
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                        Log.d("GoogleSignIn", "firebaseAuthWithGoogle: " + account.getDisplayName());
                    } catch (ApiException e) {
                        Log.e("GoogleSignIn", "Sign-in failed", e);
                    }
                } else {
                    Log.e("GoogleSignIn", "Sign-in canceled");
                }
            }
    );

    private void signIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // 로그아웃 완료 후 다시 로그인
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                signInLauncher.launch(signInIntent);
            });
        } else {
            // 로그인되지 않은 상태라면 바로 로그인 진행
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("SignInDebug", "RequestCode: " + requestCode);
        Log.d("SignInDebug", "ResultCode: " + resultCode);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Log.d("SignInDebug", "Sign-In Result: SUCCESS");
            } else {
                Log.d("SignInDebug", "Sign-In Result: CANCELED or FAILED");
            }

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("SignInDebug", "Account Display Name: " + account.getDisplayName());
                Log.d("SignInDebug", "Account Email: " + account.getEmail());
            } catch (ApiException e) {
                Log.e("SignInDebug", "Sign-In Failed: ", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Firebase 인증 성공 후 Firestore에 유저 데이터 저장
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserDataToFirestore(user);
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Log.w("GoogleSignIn", "signInWithCredential:failure", task.getException());
                    }
                });
    }

    private void saveUserDataToFirestore(FirebaseUser user) {
        // Firestore에 저장할 사용자 정보
        String userId = user.getUid();
        String userNickname = user.getDisplayName();
        String userEmail = user.getEmail();

        // Firestore 컬렉션 참조
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        // 사용자 데이터 객체
        User userData = new User(userNickname, userEmail);

        // Firestore에 데이터 저장
        usersRef.document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User data saved successfully"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving user data", e));
    }

    // 사용자 데이터 모델 클래스
    public static class User {
        private String nickname;
        private String email;

        public User(String nickname, String email) {
            this.nickname = nickname;
            this.email = email;
        }

        public String getNickname() {
            return nickname;
        }

        public String getEmail() {
            return email;
        }
    }
}