package com.example.paper.launcher;

import static com.example.paper.Permanent.IP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paper.MainActivity;
import com.example.paper.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText mEtID,mEtPassword;
    private OkHttpClient client = new OkHttpClient();
    private Button mBtnLogin;
    private final String url = IP + "login/";   //登录逻辑url
    private  String id;
    private  String password;
    private Handler handler;
    private TextView mTvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEtID = (EditText)findViewById(R.id.et_id);
        mEtPassword = (EditText)findViewById(R.id.et_pwd);
        mBtnLogin = (Button)findViewById(R.id.btn_login);
        mTvRegister = (TextView)findViewById(R.id.tv_register);

        mTvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });


        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        id = mEtID.getText().toString();
                        password = mEtPassword.getText().toString();
                        FormBody.Builder formBodyBuilder = new FormBody.Builder();
                        formBodyBuilder.add("id",id)
                                .add("password",password);
                        RequestBody requestBody = formBodyBuilder.build();

                        Request.Builder builder = new Request.Builder().url(url).post(requestBody);

                        execute(builder);
                    }
                }.start();

            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 1:
                        String string = (String)msg.obj;
                        if(string.equals("ok")){
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                            SharedPreferences sp = getSharedPreferences("now", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();

                            editor.putString("id",id) ;
                            editor.apply();

                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(LoginActivity.this,"用户名或密码错误",Toast.LENGTH_LONG).show();
                        }

                }

            }
        };
    }

    private void execute(Request.Builder builder){
        Call call = client.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("error","no");
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String str = new String(response.body().bytes(),"utf-8");

                Message message = handler.obtainMessage();
                message.what = 1;
                message.obj = str;
                message.sendToTarget();
            }
        });
    }
}