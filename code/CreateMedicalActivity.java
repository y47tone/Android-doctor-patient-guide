package com.app.doctor.patient.activity;


import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.app.doctor.patient.R;
import com.app.doctor.patient.api.ApiConstants;
import com.app.doctor.patient.base.BaseActivity;
import com.app.doctor.patient.entity.DoctorInfo;
import com.app.doctor.patient.entity.MedicalRecordInfo;
import com.app.doctor.patient.entity.UserInfo;
import com.app.doctor.patient.calendarview.http.HttpStringCallback;
import com.lzy.okgo.OkGo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 创建病历
 */
public class CreateMedicalActivity extends BaseActivity {
    private TextView tv_patient;
    private TextView tv_age;
    private TextView tv_gender;
    private TextView doctor_name;
    private LinearLayoutCompat liner_tips;
    private EditText et_disease_description;
    private EditText et_inspection_report;
    private EditText et_diagnosis_result;
    private Button create;
    private Button deleteM;

    private UserInfo userInfo;

    private MedicalRecordInfo medicalRecordInfo;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_medical;
    }

    @Override
    protected void initView() {

        tv_patient = findViewById(R.id.tv_patient);
        tv_age = findViewById(R.id.tv_age);
        tv_gender = findViewById(R.id.tv_gender);
        doctor_name = findViewById(R.id.doctor_name);
        liner_tips = findViewById(R.id.liner_tips);
        et_disease_description = findViewById(R.id.et_disease_description);
        et_inspection_report = findViewById(R.id.et_inspection_report);
        et_diagnosis_result = findViewById(R.id.et_diagnosis_result);
        create = findViewById(R.id.create);
        deleteM = findViewById(R.id.deleteM);

    }

    @Override
    protected void initListener() {
        //新增病历点击事件
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String disease_description = et_disease_description.getText().toString();
                String inspection_report = et_inspection_report.getText().toString();
                String diagnosis_result = et_diagnosis_result.getText().toString();
                if (TextUtils.isEmpty(disease_description) || TextUtils.isEmpty(inspection_report) || TextUtils.isEmpty(diagnosis_result)) {
                    showToast("请填写完整信息");
                } else {
                    //新增病历
                    if (medicalRecordInfo != null) {
                        updateMedicalRecord(medicalRecordInfo.getMedical_record_id(), disease_description, inspection_report, diagnosis_result);
                    } else {
                        addMedicalRecord(disease_description, inspection_report, diagnosis_result);
                    }

                }

            }
        });

        //删除病历点击事件
        deleteM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMedicalRecord(medicalRecordInfo.getMedical_record_id());
            }
        });
        initData();
    }

    private void deleteMedicalRecord(int medicalRecordId) {
        OkGo.<String>get(ApiConstants.DELETE_MEDICAL_RECORD_URL) // 假设你有一个用于删除病例的 POST 接口
                .params("medical_record_id", medicalRecordId)
                .execute(new HttpStringCallback(this) {
                    @Override
                    protected void onSuccess(String msg, String response) {
                        showToast(msg);
                        setResult(7000);
                        finish();
                        showToast("删除成功");
                    }

                    @Override
                    protected void onError(String response) {
                        showToast(response);
                    }
                });
    }


    /**
     * 编辑病历
     */
    private void updateMedicalRecord(int medical_record_id, String disease_description, String inspection_report, String diagnosis_result) {
        OkGo.<String>get(ApiConstants.EDIT_MEDICAL_RECORD_URL)
                .params("medical_record_id", medical_record_id)
                .params("disease_description", disease_description)
                .params("inspection_report", inspection_report)
                .params("diagnosis_result", diagnosis_result)
                .execute(new HttpStringCallback(this) {
                    @Override
                    protected void onSuccess(String msg, String response) {
                        showToast(msg);
                        setResult(7000);
                        finish();
                        showToast("编辑成功");
                    }

                    @Override
                    protected void onError(String response) {
                        showToast(response);
                    }
                });


    }

    /**
     * 新增病历
     */
    private void addMedicalRecord(String disease_description, String inspection_report, String diagnosis_result) {
        OkGo.<String>get(ApiConstants.ADD_MEDICAL_RECORD_URL)
                .params("user_id", userInfo.getUser_id())
                .params("patient_name", userInfo.getUsername())
                .params("doctor_id", DoctorInfo.getDoctorInfo().getDoctor_id())
                .params("doctor_name", DoctorInfo.getDoctorInfo().getReal_name())
                .params("disease_description", disease_description)
                .params("inspection_report", inspection_report)
                .params("diagnosis_result", diagnosis_result)
                .execute(new HttpStringCallback(this) {
                    @Override
                    protected void onSuccess(String msg, String response) {
                        showToast(msg);
                        setResult(7000);
                        finish();
                    }

                    @Override
                    protected void onError(String response) {
                        showToast(response);
                    }
                });

    }

    @Override
    protected void initData() {

        //获取患者信息
        userInfo = (UserInfo) getIntent().getSerializableExtra("userInfo");
        medicalRecordInfo = (MedicalRecordInfo) getIntent().getSerializableExtra("medicalRecordInfo");
        if (null != userInfo) {
            tv_patient.setText(userInfo.getUsername());
            tv_age.setText(userInfo.getAge() + "(岁)");
            tv_gender.setText(userInfo.getGender());
            doctor_name.setText(DoctorInfo.getDoctorInfo().getReal_name() + "(本人)");
            liner_tips.setVisibility(View.GONE);
        }

        if (null != medicalRecordInfo) {
            tv_patient.setText(medicalRecordInfo.getUser().getUsername());
            tv_age.setText(medicalRecordInfo.getUser().getAge() + "(岁)");
            tv_gender.setText(medicalRecordInfo.getUser().getGender());
            et_disease_description.setText(medicalRecordInfo.getDisease_description());
            et_inspection_report.setText(medicalRecordInfo.getInspection_report());
            et_diagnosis_result.setText(medicalRecordInfo.getDiagnosis_result());
            if (medicalRecordInfo.getDoctor_id() == DoctorInfo.getDoctorInfo().getDoctor_id()) {
                doctor_name.setText(medicalRecordInfo.getDoctor_name() + "(本人)");
                create.setEnabled(true);
                deleteM.setEnabled(true);
                liner_tips.setVisibility(View.GONE);
            } else {
                doctor_name.setText(medicalRecordInfo.getDoctor_name());
                create.setEnabled(false);
                deleteM.setEnabled(false);
                liner_tips.setVisibility(View.VISIBLE);
            }
            toolbar.setTitle("编辑病历");
            create.setText("编辑");
            deleteM.setText("删除");

        }

    }
}