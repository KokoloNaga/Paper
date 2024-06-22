package com.example.paper.daily;

import static com.example.paper.Permanent.IP;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.paper.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SubmitService extends Service implements SensorEventListener {
    private int mStepDetector = 0;
    private int mStepCounter = 0;

    private TimeCount timeCount;
    private SensorManager mSensorMgr;
    private OkHttpClient client = new OkHttpClient().newBuilder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60,TimeUnit.SECONDS)
            .readTimeout(60,TimeUnit.SECONDS)
            .build();;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1:
                    String answer  = (String) msg.obj;
                    Log.d("answer",answer);

            }
        }
    };

    //开锁屏侦测广播
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences sp = getSharedPreferences("act",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            //收到开机动作
            if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                //暂存此时的毫秒数
                editor.putLong("lasttime",new Date().getTime());
                editor.commit();
                Log.d("on","++++++");
            }
            //收到关机动作
            else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                //获取开机时间
                long lasttime = sp.getLong("lasttime",new Date().getTime());
                //获取当前使用手机的总时间
                long sum = sp.getLong("sum",0);
                sum += new Date().getTime() - lasttime; //更新总时间
                editor.putLong("sum",sum);
                editor.commit();
                Log.d("off","-------");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver,filter);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //alarm();

        startTimeCount();
        initStepSensor();

        //设为前台服务
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("小艾医生")
                .setContentText("小艾医生正在监测您的日常生活")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.ic_launcher));
        //8.0以上notification报错解决
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            String CHANNEL_ONE_NAME  = "实时监控渠道";
            String CHANNEL_ONE_ID = "SubmitChannelId";
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false);//是否显示角标

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
            Log.d("take","notification");
        }

        Notification notification = builder.build();
        startForeground(110, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    /*
    private void alarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int minutes = 60*1000;  //每分钟
        long triggerAtTime = SystemClock.elapsedRealtime() + minutes;
        Intent i = new Intent(SubmitService.this,SubmitReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
    }
    */

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) { // 步行检测事件
            if (event.values[0] == 1.0f) {
                mStepDetector++; // 步行检测事件
            }
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) { // 计步器事件
            mStepCounter = (int) event.values[0]; // 计步器事件
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

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
            Toast.makeText(SubmitService.this,"当前设备不支持计步器，请检查是否存在步行检测传感器和计步器",Toast.LENGTH_LONG).show();
        }


    }

    // 每秒计时逻辑
    private void startTimeCount(){
        timeCount = new TimeCount(1000,1000);
        timeCount.start();
    }

    private class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {

        }

        @Override
        public void onFinish() {
            SharedPreferences sp = getSharedPreferences("stepping",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            // 零点上传逻辑
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR);
            int minute = c.get(Calendar.MINUTE);
            int second = c.get(Calendar.SECOND);
            int am = c.get(Calendar.AM_PM);

            if( hour == 0 && minute == 0 && second == 1 && am == 0){
                int today = sp.getInt("sum",0);
                int yesterday = sp.getInt("yesterday",0);
                int realToday = today - yesterday; //最终上传步数

                editor.putInt("yesterday",today);
                editor.commit();

                //获取显示时间
                SharedPreferences sp_light = getSharedPreferences("act",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor_light  = sp_light.edit();
                long light = sp_light.getLong("sum",0);
                editor_light.putLong("sum",0);
                editor_light.commit();

                String url = IP + "daily/update/";

                // 上传步数和屏幕使用
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        SharedPreferences sp_id = getSharedPreferences("now", Context.MODE_PRIVATE);
                        String saveId = sp_id.getString("id","");
                        int minute = (int)(light/1000/60);
                        FormBody.Builder formBodyBuilder = new FormBody.Builder();
                        formBodyBuilder.add("id",saveId)
                                .add("step", String.valueOf(realToday))
                                .add("light", String.valueOf(minute));
                        RequestBody requestBody = formBodyBuilder.build();
                        Request.Builder builder = new Request.Builder().url(url).post(requestBody);

                        execute(builder);
                    }
                }.start();
            }



            editor.putInt("sum",mStepCounter);
            editor.commit();



            timeCount.cancel();
            startTimeCount();
            // 循环启动计步器，每秒记录步数


        }
    }

    private void execute(Request.Builder builder) {
        Call call = client.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String str = new String(response.body().bytes(), "utf-8");

                Message message = handler.obtainMessage();
                message.what = 1;
                message.obj = str;
                message.sendToTarget();
            }
        });
    }

}