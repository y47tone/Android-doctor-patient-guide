package com.app.doctor.patient.activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.doctor.patient.R;
import com.app.doctor.patient.adapter.DoctorChatAdapter;
import com.app.doctor.patient.api.ApiConstants;
import com.app.doctor.patient.base.BaseActivity;
import com.app.doctor.patient.entity.ChatInfo;
import com.app.doctor.patient.entity.ChatListInfo;
import com.app.doctor.patient.entity.DoctorInfo;
import com.app.doctor.patient.entity.UserInfo;
import com.app.doctor.patient.calendarview.http.HttpStringCallback;
import com.app.doctor.patient.utils.GsonUtils;
import com.lzy.okgo.OkGo;

import java.util.ArrayList;
import java.util.List;

public class DoctorChat2Activity extends BaseActivity {

    private List<ChatInfo> mChatInfoList = new ArrayList<>();
    private EditText inputText;
    private RecyclerView mRecyclerView;
    private CardView send;
    private DoctorChatAdapter mDoctorChatAdapter;

    private UserInfo userInfo;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_doctor_chat;
    }

    @Override
    protected void initView() {
        //初始化控件
        inputText = findViewById(R.id.input_text);
        send = findViewById(R.id.send);
        mRecyclerView = findViewById(R.id.recyclerView);

    }

    @Override
    protected void initAdapter() {
        //创建适配器
        mDoctorChatAdapter = new DoctorChatAdapter(mChatInfoList);
        //设置适配器
        mRecyclerView.setAdapter(mDoctorChatAdapter);
    }

    @Override
    protected void initListener() {


        //列表点击事件
        mDoctorChatAdapter.setOnItemClickListener(new DoctorChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ChatInfo chatInfo, int position) {
                new AlertDialog.Builder(DoctorChat2Activity.this)
                        .setCancelable(true)
                        .setTitle("温馨提示")
                        .setMessage("确定删除该消息吗？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteChatMsg(chatInfo.getChat_id(),position);
                            }
                        })
                        .show();
            }
        });




        //发送消息
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String content = inputText.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    showToast("请输入内容");
                    return;
                }
                //发送消息
                handleMessage(content);
            }
        });
    }

    private void handleMessage(String content) {
        OkGo.<String>get(ApiConstants.SEND_CHAT_MSG_URL)
                .params("chat_message", content)
                .params("user_id", userInfo.getUser_id())
                .params("user_name", userInfo.getUsername())
                .params("user_avatar", userInfo.getAvatar())
                .params("doctor_id", DoctorInfo.getDoctorInfo().getDoctor_id())
                .params("doctor_name", DoctorInfo.getDoctorInfo().getReal_name())
                .params("doctor_avatar", DoctorInfo.getDoctorInfo().getDoctor_avatar_url())
                .params("type", ChatInfo.TYPE_RESULT)
                .execute(new HttpStringCallback(null) {
                    @Override
                    protected void onSuccess(String msg, String response) {

                        ChatInfo chatInfo = GsonUtils.parseJson(response, ChatInfo.class);
                        chatInfo.setType(ChatInfo.TYPE_DEFAULT);
                        mChatInfoList.add(chatInfo);
                         //更新适配器，添加最后一条数据
                        mDoctorChatAdapter.notifyItemInserted(mChatInfoList.size() - 1);
                        mRecyclerView.scrollToPosition(mChatInfoList.size() - 1);
                        inputText.setText("");

                        Log.d("------------------", "onSuccess: "+chatInfo.toString());
                    }

                    @Override
                    protected void onError(String response) {

                    }
                });


    }

    @Override
    protected void initData() {

        userInfo = (UserInfo) getIntent().getSerializableExtra("userInfo");
        toolbar.setTitle("正在和"+userInfo.getUsername()+"沟通中...");

        //获取聊天记录
        queryChatListMsg();
    }


    /**
     * 获取聊天记录
     */
    private void queryChatListMsg() {
        OkGo.<String>get(ApiConstants.QUERY_CHAT_MSG_URL)
                .params("user_id", userInfo.getUser_id())
                .params("doctor_id", DoctorInfo.getDoctorInfo().getDoctor_id())
                .execute(new HttpStringCallback(null) {
                    @Override
                    protected void onSuccess(String msg, String response) {
                        ChatListInfo chatListInfo = GsonUtils.parseJson(response, ChatListInfo.class);
                        for (int i = 0; i < chatListInfo.getList().size(); i++) {
                            if (chatListInfo.getList().get(i).getType() == (ChatInfo.TYPE_DEFAULT)) {
                                chatListInfo.getList().get(i).setType(ChatInfo.TYPE_RESULT);
                            } else {
                                chatListInfo.getList().get(i).setType(ChatInfo.TYPE_DEFAULT);
                            }

                        }
                        mDoctorChatAdapter.addChatMsg(chatListInfo.getList());

                        //刷新适配器
                        mRecyclerView.scrollToPosition(mDoctorChatAdapter.getItemCount() - 1);

                    }

                    @Override
                    protected void onError(String response) {

                    }
                });
    }


    /**
     * 删除聊天消息
     */
    private void deleteChatMsg(int  chat_id,int position ) {
        OkGo.<String>get(ApiConstants.DELETE_CHAT_MSG_URL)
                .params("chat_id", chat_id)
                .execute(new HttpStringCallback(null) {
                    @Override
                    protected void onSuccess(String msg, String response) {
                        showToast(msg);
                        //刷新数据
                        mDoctorChatAdapter.getChatInfoList().remove(position);
                        mDoctorChatAdapter.notifyItemRemoved(position);
                    }

                    @Override
                    protected void onError(String response) {
                         showToast(response);
                    }
                });
    }
}