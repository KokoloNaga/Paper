package com.example.paper.passage;

import static com.example.paper.Permanent.IP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.paper.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PassageActivity extends AppCompatActivity {
    private TextView mTvTitle,mTvValue,mTvIntroduction;
    private ProgressDialog mDialog;
    private String URL_POST;
    private Button mBtn;
    private OkHttpClient client = new OkHttpClient().newBuilder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60,TimeUnit.SECONDS)
            .readTimeout(60,TimeUnit.SECONDS)
            .build();

    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passage);

        mTvTitle = (TextView)findViewById(R.id.tv_title);
        mTvValue = (TextView)findViewById(R.id.tv_main);
        mTvIntroduction = (TextView)findViewById(R.id.tv_introduction);
        mBtn = (Button) findViewById(R.id.btn_graph);

        Bundle bundle = getIntent().getExtras();
        final String title = bundle.getString("title");
        String main = bundle.getString("main");
        String type = bundle.getString("type");

        mTvTitle.setText(title);
        mTvValue.setText(main);

        //扩展
        if(type.equals("search")) {
            mTvIntroduction.setText("具体介绍");
            URL_POST = IP + "search/find/";
        }
        else if(type.equals("recommend")) {
            mTvIntroduction.setText("饮食介绍");
            URL_POST = IP + "recommend/find/";
        }
        mBtn.setVisibility(View.VISIBLE);
        mBtn.setText("知识图谱介绍");

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog = new ProgressDialog(PassageActivity.this);
                mDialog.setTitle("正在跳转");
                mDialog.setMessage("即将前往知识图谱界面");
                mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mDialog.show();

                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        SharedPreferences sp = getSharedPreferences("now", Context.MODE_PRIVATE);
                        String saveId = sp.getString("id","");

                        FormBody.Builder formBodyBuilder = new FormBody.Builder();
                        formBodyBuilder.add("title",title)
                                .add("id",saveId);

                        RequestBody requestBody = formBodyBuilder.build();

                        Request.Builder builder = new Request.Builder().url(URL_POST).post(requestBody);

                        execute(builder);

                    }
                }.start();
            }
        });




        mHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1:
                        String url = (String)msg.obj;

                        mDialog.dismiss();

                        Bundle bundle = new Bundle();
                        bundle.putString("url",url);


                        Intent intent = new Intent(PassageActivity.this,GraphActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);

                }
            }
        };
    }

    private void execute (Request.Builder builder) {
        Call call = client.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String str = new String(response.body().bytes(), "utf-8");

                Message message = mHandler.obtainMessage();
                message.what = 1;
                message.obj = str;
                message.sendToTarget();
            }
        });
    }
}