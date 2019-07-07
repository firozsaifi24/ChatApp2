package example.firoz.chatapp2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity {

    MaterialEditText email, password;
    Button btn_login;

    FirebaseAuth auth;

    TextView forgot_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar= findViewById(R.id.toolbar);
        toolbar.setTitle("Login");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        email= findViewById(R.id.email);
        password= findViewById(R.id.password);
        btn_login= findViewById(R.id.btn_login);
        forgot_password= findViewById(R.id.forgot_password);

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

        auth= FirebaseAuth.getInstance();

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_email= email.getText().toString();
                String txt_password= password.getText().toString();

                if(TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password))
                {
                    Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_LONG).show();
                }
                else
                {
                    auth.signInWithEmailAndPassword(txt_email, txt_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                Intent intent= new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

    }
}