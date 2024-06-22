package com.example.paper.discuss;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paper.R;

import java.util.List;

public class DiscussAdapter extends RecyclerView.Adapter<DiscussAdapter.MyViewHolder>{
    private Context mContext;
    private List<String> mList;
    private int LeftOrRight;
    private int Value;

    private DiscussAdapter.OnItemClickListener mListener;
    public interface OnItemClickListener{
        void Click(int pos,String s);
    }

    public DiscussAdapter(Context context,DiscussAdapter.OnItemClickListener mListener,List<String> list){
        this.mContext = context;
        mList = list;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.chatting_item_from,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if(LeftOrRight == DiscussActivity.LEFT){
            holder.mTvLeft.setVisibility(View.VISIBLE);
            holder.mTvRight.setVisibility(View.GONE);
            final String s = mList.get(position);
            holder.mTvLeft.setText(s);
            if(this.Value == 1){
                holder.mTvLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.Click(position,s);
                    }
                });
            }
        }else if(LeftOrRight == DiscussActivity.RIGHT){
            holder.mTvLeft.setVisibility(View.GONE);
            holder.mTvRight.setVisibility(View.VISIBLE);
            holder.mTvRight.setText(mList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView mTvLeft,mTvRight;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvLeft = itemView.findViewById(R.id.tv_content_left);
            mTvRight = itemView.findViewById(R.id.tv_content_right);
            mTvLeft.setMovementMethod(ScrollingMovementMethod.getInstance());
        }
    }

    public void addItem(String string,int leftOrRight,int isValueNull){
        this.LeftOrRight = leftOrRight;
        mList.add(string);
        notifyItemInserted(mList.size()-1);
        this.Value = isValueNull;
    }

}
