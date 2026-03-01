package com.app.doctor.patient.adapter;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.app.doctor.patient.R;
import com.app.doctor.patient.chatView.ChatView;
import com.app.doctor.patient.entity.ChatInfo;
import com.app.doctor.patient.utils.GlideEngine;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<ChatInfo> mChatInfoList = new ArrayList<>();

    public ChatAdapter(List<ChatInfo> list) {
        this.mChatInfoList = list;
    }//设置初始的聊天信息列表。

    public List<ChatInfo> getChatInfoList() {return mChatInfoList;}
    //允许getChatInfoList()外部代码获取当前的聊天信息列表。

    public void  addChatMsg(List<ChatInfo> list){
        mChatInfoList.addAll(list);
        notifyDataSetChanged();
    }
//addChatMsg添加新的聊天信息
//  notifyDataSetChanged() 方法是 RecyclerView.Adapter 类中的一个方法，
//  ↑用于通知 RecyclerView 数据集已经改变。这样，RecyclerView 就会重新绑定数据，更新界面以显示新的聊天信息

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            //加载Item布局文件//聊天气泡视图的加载
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_right, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //绑定数据
        ChatInfo chatInfo = mChatInfoList.get(position);
        holder.text_message.setText(chatInfo.getChat_message());
        if (chatInfo.getType() == ChatInfo.TYPE_DEFAULT) {
            if (chatInfo.getUser()!=null){
                GlideEngine.createGlideEngine().loadImage(holder.itemView.getContext(), chatInfo.getUser().getAvatar(), holder.avatar);
            }else {
                GlideEngine.createGlideEngine().loadImage(holder.itemView.getContext(), chatInfo.getUser_avatar(), holder.avatar);
            }
        }else {
            if (chatInfo.getDoctor()!=null){
                GlideEngine.createGlideEngine().loadImage(holder.itemView.getContext(), chatInfo.getDoctor().getDoctor_avatar_url(), holder.avatar);
            }else {
                GlideEngine.createGlideEngine().loadImage(holder.itemView.getContext(), chatInfo.getDoctor_avatar(), holder.avatar);
            }
        }


        holder.firstView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (null!=mOnItemClickListener){
                    mOnItemClickListener.onItemClick(chatInfo,position);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChatInfoList.size();
    }

    @Override
    public int getItemViewType(int position) {

        //更类型加载显示不同的布局
        return mChatInfoList.get(position).getType();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text_message;
        ImageView avatar;
        ChatView firstView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text_message = itemView.findViewById(R.id.chat_text);
            avatar = itemView.findViewById(R.id.avatar);
            firstView = itemView.findViewById(R.id.firstView);
        }
    }



    public interface  OnItemClickListener{
        void onItemClick(ChatInfo chatInfo,int position);
    }

    private  OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }
}
//以不同的布局展示聊天信息，并支持长按项以触发自定义的事件处理。