package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    MaterialEditText password,email;
    Button login;

    MaterialEditText phoneNumber,otp;
    Button getotp,m_sign;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        password=findViewById(R.id.password);
        email=findViewById(R.id.email);
        login=findViewById(R.id.login);

        phoneNumber=findViewById(R.id.phoneNumber);
        getotp=findViewById(R.id.getotp);
        m_sign=findViewById(R.id.mlogin);
        otp=findViewById(R.id.otp);

        auth=FirebaseAuth.getInstance();

        getotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String t_phoneNumber = phoneNumber.getText().toString();

                if (t_phoneNumber.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
                else{
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            "+91"+t_phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            LoginActivity.this,
                            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                    Log.d("reached"," onVerificationCompleted");
                                    register_user(phoneAuthCredential);
                                }

                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {
                                    Toast.makeText(LoginActivity.this, "Verification Failed: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCodeSent(@NonNull final String verificationID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                    m_sign=findViewById(R.id.mlogin);
                                    m_sign.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            String verificationCode = otp.getText().toString();
                                            if (verificationCode.isEmpty()) {
                                                Toast.makeText(LoginActivity.this, "Invalid Entry", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Log.d("reached"," onCodeSent");
                                                PhoneAuthCredential cred;
                                                cred = PhoneAuthProvider.getCredential(verificationID, verificationCode);
                                                register_user(cred);
                                            }
                                        }
                                    });
                                }
                            });
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String t_email=email.getText().toString();
                String t_password=password.getText().toString();

                if (TextUtils.isEmpty(t_email)||TextUtils.isEmpty(t_password))
                {
                    Toast.makeText(LoginActivity.this, "Cant be Empty", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    auth.signInWithEmailAndPassword(t_email, t_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                Toast.makeText(LoginActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
    }


    void register_user(PhoneAuthCredential credential)
    {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Log.d("reached"," Login");
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }

                    }
        });

    }

}