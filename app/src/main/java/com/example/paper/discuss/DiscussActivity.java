package com.example.paper.discuss;

import static com.example.paper.Permanent.IP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.paper.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class DiscussActivity extends AppCompatActivity {
    private static String APPID = "603ce868";   //讯飞SDK

    private RecyclerView mRvDiscuss;
    private DiscussAdapter mAdapter;

    private OkHttpClient client = new OkHttpClient().newBuilder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60,TimeUnit.SECONDS)
            .readTimeout(60,TimeUnit.SECONDS)
            .build();

    private Button mBtnVoice,mBtnSend;
    private EditText mEtQuestion;

    private int GET_RECODE_AUDIO = 1;

    final static int LEFT = 1;
    final static int RIGHT = 2;


    private final String url = IP + "discuss/";

    private Handler handler;

    private List<DialogFrom> mList;
    private String saveId,q;

    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discuss);

        permissionCheck();

        SpeechUtility.createUtility(DiscussActivity.this, SpeechConstant.APPID
                + "=" + APPID);




        mBtnVoice = (Button)findViewById(R.id.btn_voice);
        mBtnSend = (Button)findViewById(R.id.btn_question);
        mEtQuestion = (EditText)findViewById(R.id.et_question);

        mRvDiscuss = (RecyclerView) findViewById(R.id.rv_discuss);
        mRvDiscuss.setLayoutManager(new LinearLayoutManager(DiscussActivity.this));

        List<String> list = new ArrayList<>();
        mAdapter = new DiscussAdapter(this, new DiscussAdapter.OnItemClickListener() {
            @Override
            public void Click(int pos, String s) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DiscussActivity.this);
                builder.setTitle("");
                builder.setMessage(s);
                builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        },list);
        mRvDiscuss.setAdapter(mAdapter);
        mRvDiscuss.setItemViewCacheSize(50);
        leftShow("您好，请问小艾医生可以帮你点什么？",0);
        leftShow("您可以询问我疾病的症状表现、起因、并发症、适宜饮食、预防措施、治疗方法等内容",0);




        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                q = mEtQuestion.getText().toString();
                rightShow(q);
                mEtQuestion.setText("");

                SharedPreferences sp = getSharedPreferences("now", Context.MODE_PRIVATE);
                saveId = sp.getString("id","");
                question(q,saveId);

            }
        });

        mBtnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RecognizerDialog mDialog = new RecognizerDialog(DiscussActivity.this,mInitListener);
                mDialog.setParameter(SpeechConstant.LANGUAGE,"zh_cn");
                mDialog.setParameter(SpeechConstant.ACCENT,"mandarin");
                mDialog.setParameter(SpeechConstant.DOMAIN, "iat");
                mDialog.setParameter(SpeechConstant.SAMPLE_RATE,"16000");
                mDialog.setParameter(SpeechConstant.RESULT_TYPE,"json");
                mDialog.setListener(mRecognizerDialogListener);

                mDialog.show();


            }
        });


        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 1:

                        String string = (String)msg.obj;

                        Gson gson = new Gson();
                        mList = gson.fromJson(string,new TypeToken<List<DialogFrom>>(){}.getType());
                        for(int i = 0;i <= mList.size()-1;i++){
                            DialogFrom dialog = mList.get(i);
                            String leftDialog = dialog.getmTitle()+'\n'+dialog.getmValue();

                            if(dialog.getmValue().equals("")){
                                leftShow(leftDialog,0);
                            }else {
                                leftShow(leftDialog,1);
                            }

                        }

                }
            }
        };
    }

    public void leftShow(String string,int isValueNull){
        mAdapter.addItem(string,LEFT,isValueNull);
        mRvDiscuss.smoothScrollToPosition(i);
        i++;

    }

    public void rightShow(String string){
        mAdapter.addItem(string,RIGHT,0);
        mRvDiscuss.smoothScrollToPosition(i);
        i++;
    }

    private void execute(Request.Builder builder) {
        Call call = client.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                e.printStackTrace();
                Toast.makeText(DiscussActivity.this,"失败",Toast.LENGTH_LONG).show();
                Looper.loop();

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

    private void question(String q,String saveId){


        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        formBodyBuilder.add("question",q)
                .add("id",saveId);


        RequestBody requestBody = formBodyBuilder.build();

        Request.Builder builder = new Request.Builder().url(url).post(requestBody);


        execute(builder);
    }



    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int i) {
            if(i != ErrorCode.SUCCESS){
                Toast.makeText(DiscussActivity.this,i+"code",Toast.LENGTH_LONG).show();
            }
        }
    };

    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            if(!b){
                q = JsonParser.parseIatResult(recognizerResult.getResultString());
                rightShow(q);
                SharedPreferences sp = getSharedPreferences("now", Context.MODE_PRIVATE);
                saveId = sp.getString("id","");
                question(q,saveId);
            }
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    };

public static class JsonParser{
    public static String parseIatResult(String json){
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for(int i = 0;i < words.length();i++){
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                for(int j = 0;j < items.length();j++){
                    JSONObject obj = items.getJSONObject(j);
                    if(obj.getString("w").contains("nomatch")){
                        ret.append("没有匹配结果。");
                        return ret.toString();
                    }
                    ret.append(obj.getString("w"));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            ret.append("没有匹配结果。");
        }
        return ret.toString();
    }
}

    public void permissionCheck(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},GET_RECODE_AUDIO);
    }
}