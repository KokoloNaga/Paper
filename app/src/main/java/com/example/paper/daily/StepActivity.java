package com.example.paper.daily;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.paper.R;

import java.util.List;

@SuppressLint("DefaultLocale")
public class StepActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorMgr;   // 声明一个传感管理器对象
    private TextView tv_sensor,tv_hint;
    private ProgressBar mPgbStep;

    private int mStepDetector = 0; // 累加的步行检测次数
    private int mStepCounter = 0; // 计步器统计的步伐数目
    private int yesterday = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = getSharedPreferences("stepping",Context.MODE_PRIVATE);
        yesterday = sp.getInt("yesterday",0);

        setContentView(R.layout.activity_step);
        tv_sensor = (TextView)findViewById(R.id.tv_step);
        tv_hint = (TextView) findViewById(R.id.tv_step_hint);
        mPgbStep = (ProgressBar)findViewById(R.id.pgb_step);
        initStepSensor();

    }


    //初始化
    private void initStepSensor(){
        int suitable = 0;
        // 1.从系统获取传感管理器对象
        mSensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 2.获取当前设备支持的传感器列表
        List<Sensor> sensorList = mSensorMgr.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList){
            if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR){// 找到步行检测传感器
                suitable += 1;
                // 给步行检测传感器注册传感监听器
                mSensorMgr.registerListener((SensorEventListener) this,
                        mSensorMgr.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                        SensorManager.SENSOR_DELAY_NORMAL);
            } else if(sensor.getType() == Sensor.TYPE_STEP_COUNTER){
                suitable += 10;
                // 给计步器注册传感监听器
                mSensorMgr.registerListener((SensorEventListener) this,
                        mSensorMgr.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
        if(suitable == 0){
            tv_sensor.setText("当前设备不支持计步器，请检查是否存在步行检测传感器和计步器");
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorMgr.unregisterListener(this);// 注销当前活动的传感监听器
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) { // 步行检测事件
            if (event.values[0] == 1.0f) {
                mStepDetector++; // 步行检测事件
            }
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) { // 计步器事件
            mStepCounter = (int) event.values[0]; // 计步器事件
        }
        String desc = String.format("设备检测到您当前走了%d步，总计数为%d步",
                mStepDetector, (mStepCounter + mStepCounter - yesterday) % mStepDetector);
        tv_sensor.setText(desc);

        int now = mStepCounter - yesterday;
        if(now > 12000){
            tv_hint.setText(String.format("您目前的步数为%d,请注意合理休息",now));
            now = 12000;
        }
        else if(now > 8000)
            tv_hint.setText(String.format("您目前的步数为%d步，目前已达标，真不错",now));
        else
            tv_hint.setText(String.format("您目前的步数为%d步，目前尚未达标，请继续努力哦",now));
        mPgbStep.setProgress(now);
    }

    //传感器精度改变时
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}