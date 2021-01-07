package com.example.restaurantlocationtrackeradmin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

public class RegisterActivity extends AppCompatActivity {
    EditText emailET;
    EditText passET;
    EditText conPassET;
    Button registerBT;
    TextView alRaedyTV;
    TextView forgetPassTV;
    TextView errorTV;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    Boolean registered;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        emailET=findViewById(R.id.emailET);
        passET=findViewById(R.id.passET);
        conPassET=findViewById(R.id.conPassET);
        registerBT=findViewById(R.id.enterBT);
        alRaedyTV=findViewById(R.id.alreadyRegisteredTV);
        forgetPassTV=findViewById(R.id.forgetTV);
        errorTV=findViewById(R.id.errorTV);
        progressBar=findViewById(R.id.progressBar);
        mAuth= FirebaseAuth.getInstance();
        registered=false;
    }

    public void forgotPassword(View view) {
        errorTV.setVisibility(View.GONE);
        email=emailET.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(RegisterActivity.this,"Enter Email",Toast.LENGTH_LONG).show();
            errorTV.setText("Enter Email");
            errorTV.setVisibility(View.VISIBLE);
        }else {

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this,"Check Your Email To Reset Password",Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(RegisterActivity.this,task.getException().getLocalizedMessage(),Toast.LENGTH_LONG).show();
                        errorTV.setText(task.getException().getLocalizedMessage());
                    }
                }
            });
        }
    }

    public void register(View view) {
        UIUtil.hideKeyboard(RegisterActivity.this);
        final String pass,conPass;
        email=emailET.getText().toString();
        pass=passET.getText().toString();
        conPass=conPassET.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)){
            errorTV.setText("Enter All The Fields");
            errorTV.setVisibility(View.VISIBLE);
        }else {
            if(!registered){
                if(TextUtils.isEmpty(conPass)){
                    errorTV.setText("Enter All The Fields");
                    errorTV.setVisibility(View.VISIBLE);
                }else if(!pass.equals(conPass)){
                    errorTV.setText("Passwords Don't Match");
                    errorTV.setVisibility(View.VISIBLE);
                } else {

                    registerBT.setEnabled(false);
                    emailET.setEnabled(false);
                    conPassET.setEnabled(false);
                    passET.setEnabled(false);
                    errorTV.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(RegisterActivity.this,"Verify Email",Toast.LENGTH_LONG).show();
                                                } else {
                                                    errorTV.setText(task.getException().getLocalizedMessage());
                                                    errorTV.setVisibility(View.VISIBLE);
                                                    progressBar.setVisibility(View.GONE);
                                                }
                                            }
                                        });
                                        registered=true;
                                        conPassET.setVisibility(View.GONE);
                                        forgetPassTV.setVisibility(View.VISIBLE);
                                        alRaedyTV.setText("Sign Up?");
                                        progressBar.setVisibility(View.GONE);
                                        registerBT.setText("Login");
                                        emailET.setEnabled(true);
                                        conPassET.setEnabled(true);
                                        passET.setEnabled(true);
                                        passET.setText("");
                                        registerBT.setEnabled(true);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        errorTV.setText(task.getException().getLocalizedMessage());
                                        errorTV.setVisibility(View.VISIBLE);

                                        emailET.setEnabled(true);
                                        conPassET.setEnabled(true);
                                        passET.setEnabled(true);
                                        conPassET.setText("");
                                        passET.setText("");
                                        emailET.setText("");
                                        registerBT.setEnabled(true);
                                        progressBar.setVisibility(View.GONE);

                                    }

                                    // ...
                                }
                            });
                }


            } else {

                registerBT.setEnabled(false);
                emailET.setEnabled(false);
                passET.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if(!user.isEmailVerified()){
                                        mAuth.signOut();
                                        errorTV.setText("Verify Email");
                                        errorTV.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(RegisterActivity.this,"Verify Email",Toast.LENGTH_LONG).show();
                                                } else {
                                                    errorTV.setText(task.getException().getLocalizedMessage());
                                                    errorTV.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        });
                                    } else {
                                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                    registerBT.setEnabled(true);
                                    emailET.setEnabled(true);
                                    passET.setEnabled(true);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    errorTV.setText(task.getException().getLocalizedMessage());
                                    errorTV.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);


                                    registerBT.setEnabled(true);
                                    emailET.setEnabled(true);
                                    passET.setEnabled(true);
                                    emailET.setText("");
                                    passET.setText("");
                                }

                                // ...
                            }
                        });
            }
        }

    }

    public void changeToLogin(View view) {
        if(!registered) {
            registered = true;
            conPassET.setVisibility(View.GONE);
            forgetPassTV.setVisibility(View.VISIBLE);
            alRaedyTV.setText("Sign Up?");
            registerBT.setText("Login");
            errorTV.setVisibility(View.GONE);
        }else {

            registered=false;
            conPassET.setVisibility(View.VISIBLE);
            forgetPassTV.setVisibility(View.GONE);
            alRaedyTV.setText("Registered?");
            registerBT.setText("Register");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        UIUtil.hideKeyboard(RegisterActivity.this);
        return super.dispatchTouchEvent(ev);
    }
}
