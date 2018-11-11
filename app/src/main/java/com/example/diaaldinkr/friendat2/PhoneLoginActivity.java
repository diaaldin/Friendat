package com.example.diaaldinkr.friendat2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton, verifyButton;
    private EditText inputPhoneNumber, inputVerificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        sendVerificationCodeButton = findViewById(R.id.send_ver_code_button);
        verifyButton = findViewById(R.id.verification_button);
        inputPhoneNumber = findViewById(R.id.phone_number_input);
        inputVerificationCode = findViewById(R.id.verification_code_input);

        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               sendVerificationCodeButton.setVisibility(View.INVISIBLE);
               inputPhoneNumber.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);
            }
        });
    }
}
