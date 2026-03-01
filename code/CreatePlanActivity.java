package com.app.doctor.patient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.app.doctor.patient.R;
import com.app.doctor.patient.api.ApiConstants;
import com.app.doctor.patient.base.BaseActivity;
import com.app.doctor.patient.entity.DoctorInfo;
import com.app.doctor.patient.entity.PlanInfo;
import com.app.doctor.patient.entity.UserInfo;
import com.app.doctor.patient.calendarview.http.HttpStringCallback;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.GetRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 创建回访计划   / 便捷回访计划
 */
public class CreatePlanActivity extends BaseActivity {
    private TextView tv_start_time, tv_end_time, tv_patient;
    private EditText et_plan_content;
    private Button create;

    private UserInfo patientInfo;

    private PlanInfo planInfo;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_plan;
    }

    @Override
    protected void initView() {
        tv_start_time = findViewById(R.id.tv_start_time);
        tv_end_time = findViewById(R.id.tv_end_time);
        et_plan_content = findViewById(R.id.et_plan_content);
        tv_patient = findViewById(R.id.tv_patient);
        create = findViewById(R.id.create);
    }

    @Override
    protected void initListener() {
        //开始时间
        tv_start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerView pvTime = new TimePickerBuilder(CreatePlanActivity.this, new OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {
                        tv_start_time.setText(getTime(date));
                    }
                })
                        .setType(new boolean[]{true, true, true, true, true, false})
                        .setLabel("年", "月", "日", "时", "分", "秒")
                        .build();

                pvTime.show();
            }
        });

        //结束时间
        tv_end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerView pvTime = new TimePickerBuilder(CreatePlanActivity.this, new OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {
                        tv_end_time.setText(getTime(date));
                    }
                })
                        .setType(new boolean[]{true, true, true, true, true, false})
                        .setLabel("年", "月", "日", "时", "分", "秒")
                        .build();

                pvTime.show();
            }
        });

        //选择病人
        tv_patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivityForResult(new Intent(CreatePlanActivity.this, PatientListActivity.class), 5000);
            }
        });


        //新建回访计划点击事件
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String plan_content = et_plan_content.getText().toString();
                String start_time = tv_start_time.getText().toString();
                String end_time = tv_end_time.getText().toString();
                String patient_name = tv_patient.getText().toString();

                if (TextUtils.isEmpty(plan_content)) {
                    showToast("请输入回访计划内容");
                    return;
                }
                if (TextUtils.isEmpty(start_time)) {
                    showToast("请选择开始时间");
                    return;
                }
                if (TextUtils.isEmpty(end_time)) {
                    showToast("请选择结束时间");
                    return;

                }
                if (TextUtils.isEmpty(patient_name)) {
                    showToast("请选择患者");
                    return;
                }

                if (planInfo == null) {
                    createPlan(plan_content, start_time, end_time, patient_name);
                } else {
                    editPlan(plan_content, start_time, end_time, patient_name);
                }


            }
        });

    }


    @Override
    protected void initData() {

        //获取跳转传值
        planInfo = (PlanInfo) getIntent().getSerializableExtra("planInfo");
        if (planInfo != null) {
            et_plan_content.setText(planInfo.getPlan_content());
            tv_start_time.setText(planInfo.getPlan_start_time());
            tv_end_time.setText(planInfo.getPlan_end_time());
            tv_patient.setText(planInfo.getPatient_name());
            create.setText("编辑");
            toolbar.setTitle("编辑");
        }


    }

    @SuppressLint("SimpleDateFormat")
    private String getTime(Date date) {//可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 5000) {
            patientInfo = (UserInfo) data.getSerializableExtra("userInfo");
            tv_patient.setText(patientInfo.getUsername());
        }
    }


    /**
     * 创建计划
     */
    private void createPlan(String planContent, String startTime, String endTime, String patientName) {
        OkGo.<String>get(ApiConstants.CREATE__PLAN_URL)
                .params("plan_content", planContent)
                .params("plan_start_time", startTime)
                .params("plan_end_time", endTime)
                .params("patient_name", patientName)
                .params("user_id", patientInfo.getUser_id())
                .params("doctor_id", DoctorInfo.getDoctorInfo().getDoctor_id())
                .params("doctor_name", DoctorInfo.getDoctorInfo().getReal_name())
                .execute(new HttpStringCallback(this) {
                    @Override
                    protected void onSuccess(String msg, String response) {
                        showToast(msg);
                        finish();
                    }

                    @Override
                    protected void onError(String response) {
                        showToast(response);
                    }
                });


    }

    /**
     * 编辑回访计划
     */
    private void editPlan(String plan_content, String start_time, String end_time, String patient_name) {

        GetRequest<String> stringGetRequest = OkGo.<String>get(ApiConstants.EDIT_PLAN_URL);
        stringGetRequest.params("plan_id", planInfo.getPlan_id());
        stringGetRequest.params("plan_content", plan_content);
        stringGetRequest.params("plan_start_time", start_time);
        stringGetRequest.params("plan_end_time", end_time);
        stringGetRequest.params("patient_name", patient_name);
        if (null!=patientInfo){
            stringGetRequest.params("user_id", patientInfo.getUser_id());
        }
        stringGetRequest.execute(new HttpStringCallback(null) {
            @Override
            protected void onSuccess(String msg, String response) {
                showToast(msg);
                setResult(6000);
                finish();
            }

            @Override
            protected void onError(String response) {
                showToast(response);
            }
        });
    }



}