package com.example.paper.recommend;

import static com.example.paper.Permanent.IP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.example.paper.passage.PassageActivity;
import com.example.paper.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecommendActivity extends AppCompatActivity {
    private OkHttpClient client = new OkHttpClient().newBuilder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60,TimeUnit.SECONDS)
            .readTimeout(60,TimeUnit.SECONDS)
            .build();

    private final String url = IP + "recommend/";

    private RecyclerView mRvRecommend;
    private RecommendAdapter mAdapter;

    private List<RecommendFile> mList;

    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        initList();

        mRvRecommend = (RecyclerView) findViewById(R.id.rv_recommend);
        mRvRecommend.setLayoutManager(new LinearLayoutManager(RecommendActivity.this));



        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 1:
                        String string = (String)msg.obj;
                        Gson gson = new Gson();
                        mList = gson.fromJson(string,new TypeToken<List<RecommendFile>>(){}.getType());
                        mAdapter = new RecommendAdapter(RecommendActivity.this, new RecommendAdapter.OnItemClickListener() {
                            @Override
                            public void Click(int pos) {
                                RecommendFile file = mList.get(pos);

                                Bundle bundle = new Bundle();
                                bundle.putString("title",file.getmTitle());
                                bundle.putString("main",file.getmValue());
                                bundle.putString("type","recommend");

                                Intent intent = new Intent(RecommendActivity.this, PassageActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        },mList);
                        mRvRecommend.setAdapter(mAdapter);
                }

            }
        };
    }

    private void initList() {
        SharedPreferences sp = getSharedPreferences("now", Context.MODE_PRIVATE);
        String saveId = sp.getString("id","");

        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        formBodyBuilder.add("id",saveId);

        RequestBody requestBody = formBodyBuilder.build();

        Request.Builder builder = new Request.Builder().url(url).post(requestBody);

        execute(builder);

    }

    private void execute(Request.Builder builder) {
        Call call = client.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                Toast.makeText(RecommendActivity.this,"失败",Toast.LENGTH_LONG).show();
                e.printStackTrace();
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
}