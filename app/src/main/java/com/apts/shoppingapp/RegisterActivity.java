package com.apts.shoppingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtAddress, edtPhone, edtEmail, edtUsername, edtPassword;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);

        edtFullName = findViewById(R.id.edtFullName);
        edtAddress = findViewById(R.id.edtAddress);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtRegUsername);
        edtPassword = findViewById(R.id.edtRegPassword);

        Button btnSignup = findViewById(R.id.btnSignup);
        TextView txtLoginHere = findViewById(R.id.txtLoginHere);

        btnSignup.setOnClickListener(v -> attemptRegister());

        txtLoginHere.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (isEmpty(edtFullName, fullName, "Full name is required")) return;
        if (isEmpty(edtAddress, address, "Address is required")) return;
        if (isEmpty(edtPhone, phone, "Phone number is required")) return;

        if (!phone.matches("[0-9+\\-\\s]{7,15}")) {
            edtPhone.setError("Enter a valid phone number");
            edtPhone.requestFocus();
            return;
        }

        if (isEmpty(edtEmail, email, "Email is required")) return;

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Enter a valid email address");
            edtEmail.requestFocus();
            return;
        }

        if (isEmpty(edtUsername, username, "Username is required")) return;

        if (username.length() < 4) {
            edtUsername.setError("Username must be at least 4 characters");
            edtUsername.requestFocus();
            return;
        }

        if (isEmpty(edtPassword, password, "Password is required")) return;

        if (password.length() < 6) {
            edtPassword.setError("Password must be at least 6 characters");
            edtPassword.requestFocus();
            return;
        }

        if (databaseHelper.usernameExists(username)) {
            edtUsername.setError("Username is already taken");
            edtUsername.requestFocus();
            return;
        }

        boolean created = databaseHelper.registerUser(
                fullName, address, phone, email, username, password);

        if (created) {
            Toast.makeText(this, "Account created! Please log in.",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Could not create account. Try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Shows an error on the field and returns true if the value is empty. */
    private boolean isEmpty(EditText field, String value, String message) {
        if (value.isEmpty()) {
            field.setError(message);
            field.requestFocus();
            return true;
        }
        return false;
    }
}