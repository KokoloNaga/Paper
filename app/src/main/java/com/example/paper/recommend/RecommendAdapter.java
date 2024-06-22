package com.example.paper.recommend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paper.R;

import java.util.List;

public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.MyViewHolder>{
    private Context mContext;
    private List<RecommendFile> mList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void Click(int pos);
    }

    public RecommendAdapter(Context context, OnItemClickListener onItemClickListener,List<RecommendFile> list){
        this.mContext = context;
        this.mList = list;
        this.mListener = onItemClickListener;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyViewHolder holder = new RecommendAdapter.MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.recommend_item,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        RecommendFile recommendFile = mList.get(position);
        holder.mTvRecommend.setText(recommendFile.getmTitle());
        holder.mTvValue.setText(recommendFile.getmValue());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.Click(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList == null?0:mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView mTvRecommend,mTvValue;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvRecommend = (TextView) itemView.findViewById(R.id.tv_recommend);
            mTvValue = (TextView) itemView.findViewById(R.id.tv_recommend_value);
        }
    }
}
