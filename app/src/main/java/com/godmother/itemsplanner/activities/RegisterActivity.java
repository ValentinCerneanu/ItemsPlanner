package com.godmother.itemsplanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.godmother.itemsplanner.R;
import com.godmother.itemsplanner.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mName;
    private EditText mEmail;
    private EditText mPhone;
    private EditText mPassword;
    private EditText mConfirmPassword;

    private FirebaseDatabase database;
    private DatabaseReference myRefToDatabase;

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
        mConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    createAccount();
                    return true;
                }
                return false;
            }
        });
    }

    public void createAccount() {
        try {
            String name = mName.getText().toString();
            String email = mEmail.getText().toString();
            String phone = mPhone.getText().toString();
            String password = mPassword.getText().toString();
            String passwordConfirmed = mConfirmPassword.getText().toString();

            final User user = new User(name, phone);

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
            if (!password.equals(passwordConfirmed)) {
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
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    writeToUsersTable(firebaseUser, user);
                                    Intent nextActivity;
                                    nextActivity = new Intent(getBaseContext(), MainActivity.class);
                                    startActivity(nextActivity);
                                    finish();
                                    firebaseUser.sendEmailVerification()
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
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void writeToUsersTable(final FirebaseUser firebaseUser, final User user){
        user.setIsAdmin("false");
        database = FirebaseDatabase.getInstance();
        myRefToDatabase = database.getReference("Users");
        myRefToDatabase.child(firebaseUser.getUid()).setValue(user);
        SharedPreferences sharedPreferences = getSharedPreferences("FirebaseUser", MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString("id", firebaseUser.getUid());
        ed.putString("name", user.getName());
        ed.putString("email", firebaseUser.getEmail());
        ed.putString("phoneNumber",  user.getPhoneNumber());
        ed.putString("isAdmin",  "false");
        ed.commit();
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public void goBackToLogin(View v){
        finish();
    }

}

