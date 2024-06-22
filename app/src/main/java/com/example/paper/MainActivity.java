package com.example.paper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.paper.ball.BallActivity;
import com.example.paper.daily.DailyActivity;

import com.example.paper.daily.StepActivity;
import com.example.paper.daily.SubmitService;
import com.example.paper.daily.TimeSumActivity;
import com.example.paper.discuss.DiscussActivity;
import com.example.paper.launcher.LoginActivity;
import com.example.paper.recommend.RecommendActivity;
import com.example.paper.search.SearchActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LinearLayout step,timeSum,ball,discuss,search,recommend,report,loginOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*localService*/

        if(!isServiceRunning(this,".daily.SubmitService")){
            startService(new Intent(MainActivity.this, SubmitService.class));
            Log.d("start","service");
        }



        step = findViewById(R.id.ll_step);
        timeSum = findViewById(R.id.ll_light);
        ball = findViewById(R.id.ll_ball);
        discuss = findViewById(R.id.ll_discuss);
        search = findViewById(R.id.ll_search);
        recommend = findViewById(R.id.ll_recommend);
        report = findViewById(R.id.ll_report);
        loginOut = findViewById(R.id.ll_login_out);


        step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, StepActivity.class);
                startActivity(intent);
            }
        });

        timeSum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TimeSumActivity.class);
                startActivity(intent);
            }
        });

        ball.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BallActivity.class);
                startActivity(intent);
            }
        });
        discuss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DiscussActivity.class);
                startActivity(intent);
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        recommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecommendActivity.class);
                startActivity(intent);
            }
        });
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DailyActivity.class);
                startActivity(intent);
            }
        });
        loginOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("now", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("id","");
                editor.apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

    //判断服务是否已启动
    public static boolean isServiceRunning(Context mContext,String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(30);
        if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
}