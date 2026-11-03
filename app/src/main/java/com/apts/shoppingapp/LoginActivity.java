package com.apts.shoppingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername;
    private EditText edtPassword;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        edtUsername = findViewById(R.id.edtLoginUsername);
        edtPassword = findViewById(R.id.edtLoginPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView txtSignupHere = findViewById(R.id.txtSignupHere);

        btnLogin.setOnClickListener(v -> attemptLogin());

        txtSignupHere.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void attemptLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (username.isEmpty()) {
            edtUsername.setError("Username is required");
            edtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Password is required");
            edtPassword.requestFocus();
            return;
        }

        String fullName = databaseHelper.loginUser(username, password);

        if (fullName == null) {
            Toast.makeText(this, "Invalid username or password.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.login(username, fullName);
        Toast.makeText(this, "Welcome, " + fullName + "!",
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}