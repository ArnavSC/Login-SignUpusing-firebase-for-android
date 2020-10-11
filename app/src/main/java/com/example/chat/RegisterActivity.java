package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {
    MaterialEditText username,password,email;
    Button sign,sendImage;

    MaterialEditText phoneNumber,otp;
    Button getotp,m_sign;

    FirebaseAuth auth;
    DatabaseReference reference;

    ImageButton personImageButton;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    int gotProfileImage=0;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        email=findViewById(R.id.email);
        sign=findViewById(R.id.sign);

        phoneNumber=findViewById(R.id.phoneNumber);
        getotp=findViewById(R.id.getotp);
        m_sign=findViewById(R.id.m_sign);
        otp=findViewById(R.id.otp);

        personImageButton = findViewById(R.id.personImage);
        sendImage = findViewById(R.id.uploadImage);

        auth=FirebaseAuth.getInstance();

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String t_username=username.getText().toString();
                String t_password=password.getText().toString();
                String t_email= email.getText().toString();
                if (TextUtils.isEmpty(t_username)||TextUtils.isEmpty(t_password)||TextUtils.isEmpty(t_email))
                {
                    Toast.makeText(RegisterActivity.this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    register(t_username,t_password,t_email);
                    }
            }
        });
        getotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String t_phoneNumber = phoneNumber.getText().toString();
                String t_username=username.getText().toString();
                if (t_phoneNumber.isEmpty()||t_username.isEmpty()){
                    Toast.makeText(RegisterActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
                else{
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            "+91"+t_phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            RegisterActivity.this,
                            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                    register_user(phoneAuthCredential);
                                }

                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {
                                    Toast.makeText(RegisterActivity.this, "Verification Failed: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCodeSent(@NonNull final String verificationID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                    super.onCodeSent(verificationID, forceResendingToken);
                                    m_sign.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String verificationCode = otp.getText().toString();
                                            if (verificationCode.isEmpty()) {
                                                Toast.makeText(RegisterActivity.this, "Invalid Entry", Toast.LENGTH_SHORT).show();
                                            } else {
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

        personImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });
        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gotProfileImage==0){
                    Toast.makeText(RegisterActivity.this,"Please Add Image",Toast.LENGTH_SHORT).show();
                    return;
                }
                uploadImage();
            }
        });
    }

    void register(final String username, String password, String email){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userid=firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, String> hashMap=new HashMap<>();
                            hashMap.put("id",userid);
                            hashMap.put("username",username);
                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });

                        }else{
                            Toast.makeText(RegisterActivity.this,"Invalid email/password",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    void register_user(PhoneAuthCredential credential)
    {
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    {

                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    String userid=firebaseUser.getUid();
                    String t_username=username.getText().toString();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                    HashMap<String, String> hashMap=new HashMap<>();
                    hashMap.put("id",userid);
                    hashMap.put("username",t_username);
                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

                    }
                }
                else {
                    Toast.makeText(RegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    void uploadImage(){
        final StorageReference mStorageRef = firebaseStorage.child("personImage");
        mStorageRef.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DatabaseReference personDetails = firebaseDatabase.getReference("personImage");
                        personDetails.child("imageUrl").setValue(uri.toString());
                        Toast.makeText(RegisterActivity.this,"Performed Successfully",Toast.LENGTH_SHORT).show();
                    }private void SelectImage()
                    {

                        // Defining Implicit Intent to mobile gallery
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(
                                Intent.createChooser(
                                        intent,
                                        "Select Image from here..."),
                                PICK_IMAGE_REQUEST);
                    }
                });
            }
        });
    }

    private void SelectImage()
    {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(
                            intent,
                            "Select Image from here..."),
                    PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Toast.makeText(RegisterActivity.this, "Got Sucesfully", Toast.LENGTH_SHORT).show();
            personImageButton.setImageURI(mImageUri);
            gotProfileImage = 1;
        } else {
            gotProfileImage=0;
            mImageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.user_font)
                    + '/' + getResources().getResourceTypeName(R.drawable.user_font) + '/' + getResources().getResourceEntryName(R.drawable.user_font));
            Toast.makeText(RegisterActivity.this, "Not Got", Toast.LENGTH_SHORT).show();
        }
    }


}