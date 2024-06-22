package com.example.paper.launcher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.example.paper.MainActivity;
import com.example.paper.R;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        start();
    }

    public void start(){
        long WAIT_TIME = 1000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;

                if(isFirstLogin()) {
                    intent = new Intent(LauncherActivity.this, LoginActivity.class);
                } else {
                    intent = new Intent(LauncherActivity.this, MainActivity.class);
                }

                startActivity(intent);
                finish();
            }
        }, WAIT_TIME);
    }

    // 用户初次使用（方便自动登录）
    public boolean isFirstLogin(){
        SharedPreferences sp = getSharedPreferences("now", Context.MODE_PRIVATE);

        String saveId = sp.getString("id","");

        return saveId.equals("");
    }
}