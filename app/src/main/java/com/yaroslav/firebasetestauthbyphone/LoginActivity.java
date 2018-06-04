package com.yaroslav.firebasetestauthbyphone;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private LinearLayout mPhoneLayout;
    private LinearLayout mVerificationCodeLayout;
    private EditText mPhoneField;
    private EditText mVerificationCodeField;
    private ProgressBar mPhoneProgress;
    private ProgressBar mVireficationProgress;
    private Button mVerifyButton;

    private int buttonType = 0;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    private void initView() {
        mPhoneLayout = (LinearLayout) findViewById(R.id.layoutPhone);
        mVerificationCodeLayout = (LinearLayout) findViewById(R.id.layoutVerCode);
        mPhoneField = (EditText) findViewById(R.id.phoneNumber);
        mVerificationCodeField = (EditText) findViewById(R.id.verifCode);
        mPhoneProgress = (ProgressBar) findViewById(R.id.phoneProgress);
        mVireficationProgress = (ProgressBar) findViewById(R.id.verifCodeProgress);
        mVerifyButton =(Button) findViewById(R.id.sendVerification);

        mAuth = FirebaseAuth.getInstance();

        mPhoneProgress.setVisibility(View.INVISIBLE);
        mVireficationProgress.setVisibility(View.INVISIBLE);

        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonType == 0) {
                    mPhoneProgress.setVisibility(View.VISIBLE);
                    mPhoneField.setEnabled(false);
                    mVerifyButton.setEnabled(false);
                    String phoneNumber = mPhoneField.getText().toString();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, LoginActivity.this, mCallbacks);
                } else if (buttonType == 1) {
                    mVerifyButton.setEnabled(false);
                    mVireficationProgress.setVisibility(View.VISIBLE);

                    String verificationCode = mVerificationCodeField.getText().toString();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(getApplicationContext(), "Invalid request. Phone number is not valid", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(getApplicationContext(), "The SMS quota has been exceeded for this project", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                Log.d("TAG", "onCodeSent:" + verificationId);

                mVerificationId = verificationId;
                mResendToken = token;
                buttonType = 1;
                mPhoneProgress.setVisibility(View.INVISIBLE);
                mVerificationCodeLayout.setVisibility(View.VISIBLE);
                mVerifyButton.setText("Verify Code");
                mVerifyButton.setEnabled(true);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }
}










































