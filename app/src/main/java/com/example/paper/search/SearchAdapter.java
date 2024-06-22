package com.example.paper.search;

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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder>{
    private Context mContext;
    private List<SearchFile> mList;
    private SearchAdapter.OnItemClickListener mListener;
    public interface OnItemClickListener{
        void Click(int pos);
    }
    public SearchAdapter(Context context, SearchAdapter.OnItemClickListener onItemClickListener, List<SearchFile> list){
        this.mContext = context;
        this.mList = list;
        this.mListener = onItemClickListener;

    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SearchAdapter.MyViewHolder holder = new SearchAdapter.MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.search_item,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        SearchFile searchFile = mList.get(position);
        holder.mTvSearch.setText(searchFile.getmTitle());
        holder.mTvValue.setText(searchFile.getmValue());

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
        private TextView mTvSearch,mTvValue;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvSearch = (TextView) itemView.findViewById(R.id.tv_search_item);
            mTvValue = (TextView) itemView.findViewById(R.id.tv_search_value);
        }
    }
}
