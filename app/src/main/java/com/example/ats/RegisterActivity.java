package com.example.ats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText username,fullname,email,password,YOP,c,deg;
    Button register;
    TextView txt_lo;
    FirebaseAuth auth;
    DatabaseReference reference;
    ProgressDialog pd;
    Spinner sp;
    String[] i = {"","Student","Directorate","College","Professors"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        deg = (EditText)findViewById(R.id.Degree);
        username = (EditText)findViewById(R.id.username);
        YOP = (EditText)findViewById(R.id.YOP);
        YOP.setEnabled(false);
        sp = (Spinner)findViewById(R.id.Designation);
        fullname = (EditText)findViewById(R.id.fullname);
        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.pass);
        c = (EditText)findViewById(R.id.clg);
        register = (Button)findViewById(R.id.Register);
        txt_lo = (TextView)findViewById(R.id.txt_log);
        auth = FirebaseAuth.getInstance();
        ArrayAdapter<String> ar = new ArrayAdapter<String>(RegisterActivity.this,android.R.layout.simple_spinner_dropdown_item,i);
        ar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(ar);
        if(sp.getSelectedItem().toString().contentEquals("Directorate") && sp.getSelectedItem().toString().contentEquals("College") && sp.getSelectedItem().toString().contentEquals("")){
            YOP.setEnabled(false);
        }else{
            YOP.setEnabled(true);
        }
        txt_lo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));

            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = new ProgressDialog(RegisterActivity.this);
                pd.setMessage("Please Wait...");
                pd.show();
                String str_username = username.getText().toString();
                String str_fullname = fullname.getText().toString();
                String str_email = email.getText().toString();
                String str_pass = password.getText().toString();
                String str_deg = deg.getText().toString();
                String str_des = sp.getSelectedItem().toString();
                String str_YOP = YOP.getText().toString();
                String str_clg = c.getText().toString();
                if(TextUtils.isEmpty(str_username)|| TextUtils.isEmpty(str_fullname) || TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_pass)){
                    Toast.makeText(RegisterActivity.this,"Please fill all fields",Toast.LENGTH_SHORT).show();

                }else if(str_pass.length() < 8){
                    Toast.makeText(RegisterActivity.this,"Password must have 8 characters",Toast.LENGTH_SHORT).show();

                }else{
                    register(str_username,str_fullname,str_email,str_pass,str_des,str_YOP,str_clg,str_deg);

                }
            }
        });

    }
    private void register(final String username, final String fullname, String email, String Password, final String Designation, final String YOP, final String Clg , final String deg){
        auth.createUserWithEmailAndPassword(email,Password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String userid = firebaseUser.getUid();
                    reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("id",userid);
                    hashMap.put("username",username.toLowerCase());
                    hashMap.put("fullname",fullname);
                    hashMap.put("bio","");
                    hashMap.put("search",username.toLowerCase());
                    hashMap.put("degree",deg);
                    hashMap.put("imageurl","https://firebasestorage.googleapis.com/v0/b/alumnitrackingsystem-4f0d4.appspot.com/o/placeholder.png?alt=media&token=46c874f6-a08a-4341-b8a2-251521f89a2e");
                    hashMap.put("designation",Designation);
                    hashMap.put("YOP",YOP);
                    hashMap.put("College",Clg);
                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                pd.dismiss();
                                Intent intent = new Intent(RegisterActivity.this,Check.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    });
                }else{
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this,"You Cant Register with this email or password",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
