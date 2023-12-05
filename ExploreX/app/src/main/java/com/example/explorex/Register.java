package com.example.explorex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {


    TextView reg_email, reg_pass_1, reg_pass_2;
    MaterialButton reg_button;
    FirebaseAuth mAuth;
    TextView textView;


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        reg_email = findViewById(R.id.reg_email);
        reg_pass_1 = findViewById(R.id.reg_password_1);
        reg_pass_2 = findViewById(R.id.reg_password_2);
        reg_button = findViewById(R.id.reg_button);
        textView = findViewById(R.id.login_now);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        reg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, pass1, pass2;
                email = reg_email.getText().toString();
                pass1 = reg_pass_1.getText().toString();
                pass2 = reg_pass_2.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Wprowadź email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(pass1)){
                    Toast.makeText(Register.this, "Wprowadź hasło", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(pass2)){
                    Toast.makeText(Register.this, "Wprowadź potwierdzenie hasła", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(pass1.equals(pass2)){
                    mAuth.createUserWithEmailAndPassword(email, pass1)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Register.this, "Rejestracja zakończyła się sukcesem.",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), Login.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Register.this, "Rejestracja nie udała się.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(Register.this, "Hasła się nie zgadzają", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}