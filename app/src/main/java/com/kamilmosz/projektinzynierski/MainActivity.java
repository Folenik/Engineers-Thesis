package com.kamilmosz.projektinzynierski;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;

import static com.kamilmosz.projektinzynierski.Utility.Constants.DOESNT_EXIST;
import static com.kamilmosz.projektinzynierski.Utility.Constants.PREFS_NAME;
import static com.kamilmosz.projektinzynierski.Utility.Constants.PREF_VERSION_CODE_KEY;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    AppCompatEditText emailEditText, passwordEditText;
    ProgressDialog pDialog;
    private AppCompatTextView welcomeTextView;
    private AppCompatButton loginButton;
    private String emailString, passwordString;
    private int currentVersionCode, savedVersionCode;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        checkFirstRun();
    }

    public void init() {
        FirebaseApp.initializeApp(this);

        welcomeTextView = findViewById(R.id.welcomeLabel);
        emailEditText = findViewById(R.id.loginEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        pDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        loginButton.setOnClickListener(this);
    }

    public void checkFirstRun() {
        currentVersionCode = BuildConfig.VERSION_CODE;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        if (currentVersionCode == savedVersionCode) {
            welcomeTextView.setText(R.string.app_welcome_back_label);
            return;
        } else if (savedVersionCode == DOESNT_EXIST) {
            welcomeTextView.setText(R.string.app_welcome_label);
        } else if (currentVersionCode > savedVersionCode) {
            welcomeTextView.setText(R.string.app_welcome_label);
        }

        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    public void userLogIn() {
        try {
            emailString = emailEditText.getText().toString();
            passwordString = passwordEditText.getText().toString();
        } catch (NullPointerException e) {
            Log.e("", e.getMessage());
        }

        if (TextUtils.isEmpty(emailString)) {
            Toast.makeText(this, R.string.login_enter_login, Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(passwordString)) {
            Toast.makeText(this, R.string.login_enter_password, Toast.LENGTH_LONG).show();
            return;
        }

        pDialog.setMessage(getString(R.string.login_user_logging_in));
        pDialog.show();

        mAuth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    pDialog.dismiss();
                    Toast.makeText(MainActivity.this, R.string.login_user_logged_in, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, ChooseActionActivity.class);
                    startActivity(intent);
                } else {
                    pDialog.dismiss();
                    if (task.getException() instanceof FirebaseAuthEmailException) {
                        Toast.makeText(MainActivity.this, R.string.login_wrong_email, Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(MainActivity.this, R.string.login_wrong_values, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == loginButton) {
            userLogIn();
        }
    }
}