package com.example.paper.ball;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.paper.MainActivity;
import com.example.paper.R;

import java.util.Calendar;
import java.util.List;

public class WindowService extends Service {

    private WindowManager.LayoutParams wmParams;
    private WindowManager mWindowManager;
    private View mWindowView;
    private ImageView mIvBall;
    private TextView mTvLeft,mTvRight;
    private AlarmReceiver alarmReceiver;
    private final int P = 100;


    private int mStartX;
    private int mStartY;
    private int mEndX;
    private int mEndY;

    private final String BALL = "android.intent.action.BALL";
    @Override
    public void onCreate() {
        super.onCreate();
        alarmReceiver = new AlarmReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BALL);
        registerReceiver(alarmReceiver,filter);


        initWindowParams();
        initView();
        addWindowView2Window();
        initClick();

    }


    private void initWindowParams() {
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        //type
        if (Build.VERSION.SDK_INT>=26) {//8.0新特性
            wmParams.type= WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            wmParams.type= WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        wmParams.format = PixelFormat.TRANSLUCENT;
        //flags
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    private void initView() {
        mWindowView = LayoutInflater.from(getApplication()).inflate(R.layout.layout_window, null);
        mIvBall = (ImageView) mWindowView.findViewById(R.id.iv_ball);
        mTvLeft = (TextView) mWindowView.findViewById(R.id.tv_left);
        mTvRight = (TextView) mWindowView.findViewById(R.id.tv_right);

    }

    private void addWindowView2Window() {
        mWindowManager.addView(mWindowView, wmParams);
    }
    @Override

    public int onStartCommand(Intent intent, int flags, int startId) {
        alarm();

        return super.onStartCommand(intent, flags, startId);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWindowView != null) {
            //移除悬浮窗口
            mWindowManager.removeView(mWindowView);
            unregisterReceiver(alarmReceiver);
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void initClick() {
        mIvBall.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mStartX = (int) event.getRawX();
                        mStartY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mEndX = (int) event.getRawX();
                        mEndY = (int) event.getRawY();
                        if (needIntercept()) {
                            //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                            wmParams.x = (int) event.getRawX() - mWindowView.getMeasuredWidth() / 2;
                            wmParams.y = (int) event.getRawY() - mWindowView.getMeasuredHeight() / 2;
                            mWindowManager.updateViewLayout(mWindowView, wmParams);
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (needIntercept()) {
                            return true;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        mIvBall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAppAtBackground(WindowService.this)) {
                    Intent intent = new Intent(WindowService.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }
            }
        });

    }


    private void alarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int minutes = 10*1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + minutes;

        Intent i = new Intent(BALL);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
    }

    //对话框显示广播
    private class AlarmReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            int x = wmParams.x;

            if(x > 672){
                mTvLeft.setWidth(500);
                mTvLeft.setMaxLines(4);
                leftShow();
                mTvLeft.setVisibility(View.VISIBLE);
            }
            else {
                mTvRight.setWidth(500);
                mTvRight.setMaxLines(4);
                rightShow();
                mTvRight.setVisibility(View.VISIBLE);
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTvLeft.setText("1");
                    mTvRight.setText("1");
                    mTvLeft.setWidth(0);
                    mTvRight.setWidth(0);
                    mTvLeft.setMaxLines(0);
                    mTvRight.setMaxLines(0);
                    mTvLeft.setVisibility(View.INVISIBLE);
                    mTvRight.setVisibility(View.INVISIBLE);
                }
            },3000);

            Intent i = new Intent(context,WindowService.class);
            context.startService(i);
        }
    }

    //获取当前步数
    private int getSteps(){
        SharedPreferences sp = getSharedPreferences("stepping",Context.MODE_PRIVATE);
        int today = sp.getInt("sum",0);
        int yesterday = sp.getInt("yesterday",0);
        int now = today - yesterday;
        return now;
    }
    //获取当前屏幕时间
    private long getLightTime(){
        SharedPreferences sp_light = getSharedPreferences("act",Context.MODE_PRIVATE);
        long light = sp_light.getLong("sum",0);
        return light/(1000*60);
    }
    //步数逻辑
    private String aboutStep(int stepLevel){
        int step = getSteps();
        if(step < 0.8 * stepLevel){
            return String.format("您目前的步数为%d步，目前尚未达标，请继续努力哦",step);
        }
        else if(step < 1.2 * stepLevel){
            return String.format("您目前的步数为%d步，目前已达标，真不错",step);
        }
        else{
            return String.format("您目前的步数为%d,请注意合理休息",step);
        }
    }
    //屏幕使用逻辑
    private String aboutLight(){
        long light = getLightTime();
        if(light < 120){
            return String.format("您今日手机已使用了%d分钟，请注意适当休息",(int)light);
        }
        else if(light < 240){
            return String.format("您今日手机已使用了%d分钟，请避免长时间使用手机",(int)light);
        }
        else
            return String.format("您今日手机已使用了%d分钟，继续使用手机将对眼睛造成巨大伤害，为了您的健康着想请不要继续使用手机",(int)light);
    }
    //悬浮球以左显示
    private void leftShow(){
        int r = (int)(Math.random()*100);
        if(r <= P){ //5%概率触发
            Calendar now = Calendar.getInstance();
            int month = now.get(Calendar.MONTH);
            int hour = now.get(Calendar.HOUR);
            int am_pm = now.get(Calendar.AM_PM);
            int level;
            if((month >= 2 && month <=4) || (month >= 8 && month <= 10)){ //适宜季节
                level = 10000;
            }
            else { //夏冬季
                level = 8500;
            }
            String step = aboutStep(level);
            String light = aboutLight();
            String ask = "";
            if(hour >= 6 && hour <= 8 && am_pm == 0){
                ask = "早上好，新的一天开始了，要记得按时吃早饭哦！";
            }
            else if(hour >= 5 && hour <= 7 && am_pm == 1){
                ask = "晚上好，晚饭时间到啦，记得饭后去散步吧！";
            }
            else if(hour == 0 && am_pm == 1){
                ask = "中午好，午饭啦，饭后午休一会儿吧";
            }
            if(!ask.equals("")){
                int p = (int)(Math.random()*100);
                if(p >= 0 && p <= 50){
                    mTvLeft.setText(ask);
                }
                else if(p <= 75){
                    mTvLeft.setText(step);
                }
                else{
                    mTvLeft.setText(light);
                }
            }
            else {
                int p = (int)(Math.random()*100);
                if(p >= 0 && p <= 50){
                    mTvLeft.setText(step);
                }
                else {
                    mTvLeft.setText(light);
                }
            }
        }
    }
    //悬浮球以右显示
    private void rightShow(){
        int r = (int)(Math.random()*100);
        if(r <= P){ //概率触发
            Calendar now = Calendar.getInstance();
            int month = now.get(Calendar.MONTH);
            int hour = now.get(Calendar.HOUR);
            int am_pm = now.get(Calendar.AM_PM);
            int level;
            if((month >= 2 && month <=4) || (month >= 8 && month <= 10)){ //适宜季节
                level = 10000;
            }
            else { //夏冬季
                level = 8500;
            }
            String step = aboutStep(level);
            String light = aboutLight();
            String ask = "";
            if(hour >= 6 && hour <= 8 && am_pm == 0){
                ask = "早上好，新的一天开始了，要记得按时吃早饭哦！";
            }
            else if(hour >= 5 && hour <= 7 && am_pm == 1){
                ask = "晚上好，晚饭时间到啦，记得饭后去散步吧！";
            }
            else if(hour == 0 && am_pm == 1){
                ask = "中午好，午饭啦，饭后午休一会儿吧";
            }
            if(!ask.equals("")){
                int p = (int)(Math.random()*100);
                if(p >= 0 && p <= 50){
                    mTvRight.setText(ask);
                }
                else if(p <= 75){
                    mTvRight.setText(step);
                }
                else{
                    mTvRight.setText(light);
                }
            }
            else {
                int p = (int)(Math.random()*100);
                if(p >= 0 && p <= 50){
                    mTvRight.setText(step);
                }
                else {
                    mTvRight.setText(light);
                }
            }
        }
    }
    /**
     * 是否拦截
     * @return true:拦截;false:不拦截.
     */
    private boolean needIntercept() {
        if (Math.abs(mStartX - mEndX) > 30 || Math.abs(mStartY - mEndY) > 30) {
            return true;
        }
        return false;
    }


    /**
     *判断当前应用程序处于前台还是后台
     */
    private boolean isAppAtBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }
}