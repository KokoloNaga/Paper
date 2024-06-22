package com.example.paper.search;

import static com.example.paper.Permanent.IP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class SearchActivity extends AppCompatActivity {
    private Button mBtnSearch;
    private EditText mEtSearch;
    private RadioGroup mRgType;
    private ProgressDialog mDialog;
    private int type = 1;

    private OkHttpClient client = new OkHttpClient().newBuilder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60,TimeUnit.SECONDS)
            .readTimeout(60,TimeUnit.SECONDS)
            .build();

    private final String url = IP + "search/";

    private List<SearchFile> mList;

    private SearchAdapter mAdapter;
    private RecyclerView mRvSearch;

    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mBtnSearch = findViewById(R.id.btn_search);
        mEtSearch = findViewById(R.id.et_search);
        mRgType = findViewById(R.id.rg_type);
        mRvSearch =findViewById(R.id.rv_search);
        mRvSearch.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        mAdapter = new SearchAdapter(this, new SearchAdapter.OnItemClickListener() {
            @Override
            public void Click(int pos) {
                SearchFile file = mList.get(pos);

                Bundle bundle = new Bundle();
                bundle.putString("title",file.getmTitle());
                bundle.putString("main",file.getmValue());

                Intent intent = new Intent(SearchActivity.this, PassageActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        },mList);

        mRvSearch.setAdapter(mAdapter);

        mRgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton mRBtnAccurate = findViewById(R.id.rb_accurate);
                RadioButton mRBtnMore = findViewById(R.id.rb_more);
                if(mRBtnAccurate.isChecked()){
                    type = 1;
                }else if(mRBtnMore.isChecked()){
                    type = 2;
                }
            }
        });

        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog = new ProgressDialog(SearchActivity.this);
                mDialog.setTitle("正在加载");
                mDialog.setMessage("正在加载实体信息数据");
                mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mDialog.show();
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        String searchWord = mEtSearch.getText().toString();

                        SharedPreferences sp = getSharedPreferences("now", Context.MODE_PRIVATE);
                        String saveId = sp.getString("id","");

                        FormBody.Builder formBodyBuilder = new FormBody.Builder();
                        formBodyBuilder.add("id",saveId)
                                .add("type", String.valueOf(type))
                                .add("word",searchWord);

                        RequestBody requestBody = formBodyBuilder.build();

                        Request.Builder builder = new Request.Builder().url(url).post(requestBody);

                        execute(builder);
                    }
                }.start();

            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 1:
                        mDialog.dismiss();
                        String string = (String)msg.obj;
                        Gson gson = new Gson();
                        List<SearchFile> user = gson.fromJson(string,new TypeToken<List<SearchFile>>(){}.getType());
                        mList = user;
                        if(mList == null){
                            Toast.makeText(SearchActivity.this,"对不起，没有合适的结果。",Toast.LENGTH_LONG).show();
                        }
                        else {
                            mAdapter = new SearchAdapter(SearchActivity.this, new SearchAdapter.OnItemClickListener() {
                                @Override
                                public void Click(int pos) {
                                    SearchFile file = mList.get(pos);

                                    Bundle bundle = new Bundle();
                                    bundle.putString("title", file.getmTitle());
                                    bundle.putString("main", file.getmValue());
                                    bundle.putString("type", "search");

                                    Intent intent = new Intent(SearchActivity.this, PassageActivity.class);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }
                            }, mList);
                            mRvSearch.setAdapter(mAdapter);
                        }
                }

            }
        };
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