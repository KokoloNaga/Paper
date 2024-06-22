package com.example.paper.daily;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paper.R;

public class TimeSumActivity extends AppCompatActivity {
    private TextView mTvSum,mTvHint;
    private Button mBtnStart;
    private ProgressBar mPgbLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);

        mTvSum = findViewById(R.id.tv_sumTime);
        mBtnStart = findViewById(R.id.btn_startService);
        mPgbLight = findViewById(R.id.pgb_light);
        mTvHint = findViewById(R.id.tv_hint);

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Intent ser = new Intent(TimeSumActivity.this, LocalService.class);
                startService(ser);

                 */
            }
        });

        SharedPreferences sp = getSharedPreferences("act", Context.MODE_PRIVATE);
        long sum = sp.getLong("sum",0);
        if(sum != 0)
            sum = sum/1000;
        String sumText = String.valueOf(sum);
        mTvSum.setText(sumText);
        mTvHint.setText(String.valueOf(sum));

        String hint;
        int hour = (int) (sum/3600);
        int minute = (int)(sum/60 % 60);
        if(sum > 14400){
            hint = String.format("您今日手机已使用了%d时%d分%d秒，继续使用手机将对眼睛造成巨大伤害，为了您的健康着想请不要继续使用手机",hour,minute,(int)(sum % 60));
            sum = 14400;
        }
        else if(sum > 7200)
            hint = String.format("您今日手机已使用了%d时%d分%d秒，请避免长时间使用手机",hour,minute,(int)(sum % 60));
        else
            hint = String.format("您今日手机已使用了%d时%d分%d秒，请注意适当休息",hour,minute,(int)(sum % 60));

        mTvHint.setText(hint);

        mPgbLight.setProgress((int) sum);



    }

    private void setHint(long sum){

        if(sum > 14400){
            mTvHint.setText(String.format("您今日手机已使用了%ld秒，继续使用手机将对眼睛造成巨大伤害，为了您的健康着想请不要继续使用手机",sum));
            sum = 14400;
        }
        else if(sum > 7200)
            mTvHint.setText(String.format("您今日手机已使用了%ld秒，请避免长时间使用手机",sum));
        else
            mTvHint.setText(String.format("您今日手机已使用了%ld秒，请注意适当休息",sum));
    }
}