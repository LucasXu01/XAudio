package com.lucas.xaudio.mediaplayer.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.lucas.xaudio.R;
import com.lucas.xaudio.mediaplayer.core.AudioController;
import com.lucas.xaudio.mediaplayer.model.AudioBean;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;


public class MusicListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<AudioBean> mAudioBeans;
    private AudioBean mCurrentBean;

    public MusicListAdapter(ArrayList<AudioBean> audioBeans, AudioBean bean, Context context) {
        mAudioBeans = audioBeans;
        mCurrentBean = bean;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_bottom_sheet_item, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final AudioBean bean = mAudioBeans.get(position);
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioController.getInstance().addAudio(bean);
            }
        });
        myViewHolder.name.setText(bean.name);
        myViewHolder.author.setText(
                new StringBuilder().append(" ").append("-").append(" ").append(bean.author));
        if (bean.id.equals(mCurrentBean.id)) {
            //为播放中歌曲，红色提醒
            myViewHolder.tip.setVisibility(View.VISIBLE);
            myViewHolder.name.setTextColor(
                    mContext.getResources().getColor(android.R.color.holo_red_light));
            myViewHolder.author.setTextColor(
                    mContext.getResources().getColor(android.R.color.holo_red_light));
        } else {
            myViewHolder.tip.setVisibility(View.GONE);
            myViewHolder.name.setTextColor(Color.parseColor("#333333"));
            myViewHolder.author.setTextColor(Color.parseColor("#333333"));
        }
    }

    @Override
    public int getItemCount() {
        return mAudioBeans == null ? 0 : mAudioBeans.size();
    }

    //更新Adater状态
    public void updateAdapter(AudioBean bean) {
        mCurrentBean = bean;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout mLayout;
        private TextView name;
        private TextView author;
        private ImageView tip;

        public MyViewHolder(View itemView) {
            super(itemView);
            mLayout = itemView.findViewById(R.id.item_layout);
            tip = itemView.findViewById(R.id.tip_view);
            name = itemView.findViewById(R.id.item_name);
            author = itemView.findViewById(R.id.item_author);
        }
    }
}
