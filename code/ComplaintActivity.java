package com.app.doctor.patient.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.app.doctor.patient.R;
import com.app.doctor.patient.api.ApiConstants;
import com.app.doctor.patient.base.BaseActivity;
import com.app.doctor.patient.base.BaseFragment;
import com.app.doctor.patient.calendarview.http.HttpStringCallback;
import com.app.doctor.patient.entity.ComplaintInfo;
import com.app.doctor.patient.utils.GlideEngine;
import com.app.doctor.patient.utils.ImageLoaderUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.engine.CropFileEngine;
import com.luck.picture.lib.engine.UriToFileTransformEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.utils.SandboxTransformUtils;
import com.lzy.okgo.OkGo;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropImageEngine;

import java.io.File;
import java.util.ArrayList;

public class ComplaintActivity  extends BaseActivity {

    private EditText et_tousu;
    private EditText et_tousuRen;
    private EditText et_iPhone;
    private EditText et_tousuName;
    private ImageView zhenju;
    private String zhengju_availablePath;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_complaint;
    }

    @Override
    protected void initView() {

        et_tousu = findViewById(R.id.et_tousu);
        et_tousuRen = findViewById(R.id.et_tousuRen);
        et_iPhone= findViewById(R.id.et_iPhone);
        et_tousuName = findViewById(R.id.et_tousuName);
        zhenju=findViewById(R.id.zhengju);
    }

    @Override
    protected void initListener() {

//证据图片选择
        zhenju.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PictureSelector.create(ComplaintActivity.this).openGallery(SelectMimeType.ofImage()).setImageEngine(GlideEngine.createGlideEngine()).setSelectionMode(SelectModeConfig.SINGLE).setSandboxFileEngine(new UriToFileTransformEngine() {
                    @Override
                    public void onUriToFileAsyncTransform(Context context, String srcPath, String mineType, OnKeyValueResultCallbackListener call) {
                        if (call != null) {
                            String sandboxPath = SandboxTransformUtils.copyPathToSandbox(context, srcPath, mineType);
                            call.onCallback(srcPath, sandboxPath);
                        }
                    }
                }).setCropEngine(new CropFileEngine() {
                    @Override
                    public void onStartCrop(Fragment fragment, Uri srcUri, Uri destinationUri, ArrayList<String> dataSource, int requestCode) {
                        UCrop uCrop = UCrop.of(srcUri, destinationUri, dataSource);
                        uCrop.setImageEngine(new UCropImageEngine() {
                            @Override
                            public void loadImage(Context context, String url, ImageView imageView) {
                                if (!ImageLoaderUtils.assertValidRequest(context)) {
                                    return;
                                }
                                Glide.with(context).load(url).override(180, 180).into(imageView);
                            }

                            @Override
                            public void loadImage(Context context, Uri url, int maxWidth, int maxHeight, OnCallbackListener<Bitmap> call) {
                                Glide.with(context).asBitmap().load(url).override(maxWidth, maxHeight).into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        if (call != null) {
                                            call.onCall(resource);
                                        }
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                        if (call != null) {
                                            call.onCall(null);
                                        }
                                    }
                                });
                            }
                        });
                        uCrop.start(fragment.requireActivity(), fragment, requestCode);
                    }
                }).forResult(new OnResultCallbackListener<LocalMedia>() {

                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        // TODO 做你自己的操作
                        if (result.size() > 0) {
                            zhengju_availablePath = result.get(0).getAvailablePath();
                            GlideEngine.createGlideEngine().loadImage(ComplaintActivity.this, zhengju_availablePath,zhenju);
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        });



//提交投诉
        findViewById(R.id.btn_sub).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tousuRen = et_tousuRen.getText().toString().trim(); // 投诉人名字
                String iphone = et_iPhone.getText().toString().trim();      // 联系电话
                String tousuDuiXiang = et_tousuName.getText().toString().trim(); // 投诉对象
                String tousu = et_tousu.getText().toString().trim();        // 投诉内容

                // 检查所有字段是否为空
                if (tousuRen.isEmpty()) {
                    showToast("请填写投诉人名字");
                } else if (iphone.isEmpty()) {
                    showToast("请填写联系电话");
                } else if (tousuDuiXiang.isEmpty()) {
                    showToast("请填写投诉对象");
                } else if (tousu.isEmpty()) {
                    showToast("请输入投诉内容");
                } else {
                    // 所有字段都已填写，提交反馈
                    submitTousu(tousuRen, iphone, tousuDuiXiang, tousu);
                }
            }


        });
    }

    @Override
    protected void initData() {

    }
    /**
    * 提交回访反馈
     */
    private void submitTousu(String patient_name,String mobile,String doctor_name,String complaint_context) {
        OkGo.<String>get(ApiConstants.ADD_TOUSU_RECORD_URL)
                .params("complaintid", ComplaintInfo.getComplaintid())
                .params("patient_name", patient_name)
                .params("doctor_name", doctor_name)
                .params("complaint_context", complaint_context)
                .params("mobile", mobile)
                .params("complaint_url", (zhengju_availablePath != null && !zhengju_availablePath.trim().isEmpty()) ? new File(zhengju_availablePath).getAbsolutePath() : null)
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
}