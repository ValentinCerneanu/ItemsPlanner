package com.example.itemsplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.itemsplanner.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mName;
    private EditText mEmail;
    private EditText mPhone;
    private EditText mPassword;
    private EditText mConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mName = (EditText) findViewById(R.id.input_name_register);
        mEmail = (EditText) findViewById(R.id.input_email_register);
        mPhone = (EditText) findViewById(R.id.input_phone_register);
        mPhone.setTransformationMethod(null);
        mPassword = (EditText) findViewById(R.id.input_password1_register);
        mConfirmPassword = (EditText) findViewById(R.id.input_password2_register);
    }

    public void createAccount(View v) throws ExecutionException, InterruptedException, JSONException {
        String name = mName.getText().toString();
        String email = mEmail.getText().toString();
        String phone = mPhone.getText().toString();
        String password = mPassword.getText().toString();
        String passwordConfirmed = mConfirmPassword.getText().toString();

        View focusView = null;
        boolean cancel = false;
        //name
        if (TextUtils.isEmpty(name)) {
            mName.setError(getString(R.string.error_field_required));
            focusView = mName;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmail;
            cancel = true;
        }
        //phone
        if (TextUtils.isEmpty(phone)) {
            mPhone.setError(getString(R.string.error_field_required));
            focusView = mPhone;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }
        if(!password.equals(passwordConfirmed)) {
            mConfirmPassword.setError(getString(R.string.error_password_not_match));
            focusView = mConfirmPassword;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                Intent nextActivity;
                                nextActivity = new Intent(getBaseContext(), MainActivity.class);
                                startActivity(nextActivity);
                                finish();
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("", "Email sent.");
                                                }
                                            }
                                        });
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public void goBackToLogin(View v){
        finish();
    }

}

