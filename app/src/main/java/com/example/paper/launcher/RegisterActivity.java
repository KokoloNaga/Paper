package com.example.paper.launcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.paper.Permanent;
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

public class RegisterActivity extends AppCompatActivity {
    private EditText mEtId,mEtPwd,mEtReset,mEtAge,mEtName;
    private Button mBtnRegister;
    private OkHttpClient client = new OkHttpClient();
    private Handler handler;
    private final String url = Permanent.IP + "register/";  //注册逻辑url
    private int sex = 1;
    private RadioGroup mRgSex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEtId = (EditText)findViewById(R.id.et_id_register);
        mEtPwd = (EditText)findViewById(R.id.et_pwd_register);
        mEtReset = (EditText)findViewById(R.id.et_pwd_reset);
        mEtAge = (EditText)findViewById(R.id.et_age_register);

        mEtName = (EditText)findViewById(R.id.et_name);
        mBtnRegister = (Button)findViewById(R.id.btn_register);
        mRgSex = (RadioGroup)findViewById(R.id.rg_sex);

        mRgSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton mRbtnNan = (RadioButton)findViewById(R.id.rg_nan);
                RadioButton mRbtnNv = (RadioButton)findViewById(R.id.rg_nv);
                if(mRbtnNan.isChecked()){
                    sex = 1;
                }else if(mRbtnNv.isChecked()){
                    sex = 2;
                }
            }
        });






        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String pwd = mEtPwd.getText().toString();
                String reset = mEtReset.getText().toString();

                if(check()==-1)
                    return;

                if(mEtId.getText().toString().equals("")|mEtPwd.getText().toString().equals("")|mEtAge.getText().toString().equals("")|mEtName.getText().toString().equals("")){
                    Toast.makeText(RegisterActivity.this,"信息不得为空",Toast.LENGTH_LONG).show();
                    return;
                }


                if(pwd.equals(reset)){
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            String id = mEtId.getText().toString();
                            String pwd = mEtPwd.getText().toString();
                            String age = mEtAge.getText().toString();
                            String sex_string = String.valueOf(sex);
                            String name = mEtName.getText().toString();


                            FormBody.Builder formBodyBuilder = new FormBody.Builder();
                            formBodyBuilder.add("id",id)
                                    .add("password",pwd)
                                    .add("age",age)
                                    .add("sex",sex_string)
                                    .add("name",name);
                            RequestBody requestBody = formBodyBuilder.build();

                            Request.Builder builder = new Request.Builder().url(url).post(requestBody);

                            execute(builder);
                        }
                    }.start();

                }else {
                    Toast.makeText(RegisterActivity.this,"两次输入密码不一致",Toast.LENGTH_LONG).show();
                }
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1:
                        String s = (String)msg.obj;
                        if (s.equals("ok")){
                            Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                            startActivity(intent);
                            finish();

                        }else if(s.equals("error")){
                            Toast.makeText(RegisterActivity.this,"注册失败",Toast.LENGTH_LONG).show();
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

    private int check(){
        String id_new = mEtId.getText().toString();
        if(id_new.length()!= 11){
            Toast.makeText(RegisterActivity.this,"id请使用11位手机号码注册",Toast.LENGTH_LONG).show();
            mEtId.setText("");
            return -1;
        }
        try {
            Long.valueOf(id_new);
        }catch (Exception ex){
            Toast.makeText(RegisterActivity.this,"id必须为数字！",Toast.LENGTH_LONG).show();
            mEtId.setText("");
            return -1;
        }

        String pwd_new = mEtPwd.getText().toString();
        if(pwd_new.length()> 10){
            Toast.makeText(RegisterActivity.this,"密码不能超过10位",Toast.LENGTH_LONG).show();
            mEtPwd.setText("");
            return -1;
        }

        try {
            int age_new = Integer.valueOf(mEtAge.getText().toString());
            if(age_new<0 | age_new>200){
                Toast.makeText(RegisterActivity.this,"年龄非法！",Toast.LENGTH_LONG).show();
                mEtAge.setText("");
                return -1;
            }
        }catch (Exception ex){
            Toast.makeText(RegisterActivity.this,"年龄非法！",Toast.LENGTH_LONG).show();
            mEtAge.setText("");
            return -1;
        }

        return 0;

    }
}