package com.idogfooding.xquick.base;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenenyu.router.Router;
import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.idogfooding.backbone.RequestCode;
import com.idogfooding.backbone.photo.PhotoPickerAdapter;
import com.idogfooding.backbone.photo.PhotoPickerEntity;
import com.idogfooding.backbone.utils.ViewUtils;
import com.idogfooding.xquick.android.R;
import com.idogfooding.xquick.photopicker.GlideEngine;
import com.idogfooding.xquick.photopicker.UCropUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * 表单页面
 * 支持多种图片获取与压缩、确认按钮等
 * 在xml中设置onCropPhotoClick onPhotoClick attemptSubmit
 * 在继承的activity中实现onPhotoMultiCompressSuccess onPhotoCompressSuccess onPhotoCropCompressSuccess
 *
 * @author Charles
 */
public abstract class FormActivity extends AppBaseActivity {

    // 表示已经开始编辑，在退出的时候提示
    protected boolean isEditing;

    // 图片是否正在压缩
    protected boolean isPhotoCompressing;
    protected boolean isPhotoMultiCompressing;
    protected boolean isPhotoCropCompressing;
    protected List<String> multiCompressedRawPaths; // 压缩原文件
    protected List<File> multiCompressedFiles; // 压缩结果

    // 多图片
    @Nullable
    @BindView(R.id.rv_photos)
    RecyclerView rvPhotos;
    protected PhotoPickerAdapter photoAdapter;

    // 带裁剪的图片
    @Nullable
    @BindView(R.id.iv_crop_photo)
    ImageView ivCropPhoto;
    // 单图片
    @Nullable
    @BindView(R.id.iv_photo)
    ImageView ivPhoto;
    // 确认按钮
    @Nullable
    @BindView(R.id.btn_submit)
    View btnSubmit;

    @Override
    protected void onSetup(Bundle savedInstanceState) {
        super.onSetup(savedInstanceState);
        registerSoftInputChanged();
    }

    @Override
    protected void onSoftInputChanged(boolean visible, int height) {
        if (null == btnSubmit)
            return;

        btnSubmit.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    /**
     * 初始化多图选择器
     */
    protected void initPhotoPicky() {
        if (null == rvPhotos) {
            return;
        }
        photoAdapter = new PhotoPickerAdapter(null);
        // photoAdapter.setMorePhotoResId(R.mipmap.photo_add);
        photoAdapter.setDeleteEnable(true);
        photoAdapter.setOnItemClickListener((adapter, view, position) -> {
            clearEditTextFocus();
            PhotoPickerEntity photoEntity = (PhotoPickerEntity) adapter.getItem(position);
            if (null == photoEntity) {

            } else if (photoEntity.getType() == PhotoPickerEntity.TYPE_ADD) {
                if (isPhotoMultiCompressing) {
                    ToastUtils.showLong("图片压缩中,请稍等...");
                }
                // todo fix 可能存在原先的url图片
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setFileProviderAuthority(AppUtils.getAppPackageName() + ".camera.file.provider")
                        .setPuzzleMenu(false)
                        .setCleanMenu(false)
                        .setGif(false)
                        .setCount(9)
                        .setSelectedPhotoPaths(photoAdapter.getRawPhotos())
                        .start(RequestCode.PHOTO_MULT_PICKER);
            } else {
                // todo
                /*PhotoPreview.builder()
                        .setPhotos(photoAdapter.getRealPhotos())
                        .setCurrentItem(position)
                        .setShowDeleteButton(true)
                        .start(this, RequestCode.PHOTO_MULT_PREVIEW);*/
            }
        });
        rvPhotos.setLayoutManager(new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL));
        rvPhotos.setAdapter(photoAdapter);
    }

    /**
     * 图片正在压缩
     *
     * @return isPhotoInCompressing
     */
    protected boolean isPhotoInCompressing() {
        return isPhotoCompressing || isPhotoMultiCompressing || isPhotoCropCompressing;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK && requestCode >= RequestCode.PHOTO_MULT_PICKER && requestCode <= RequestCode.PHOTO_MULT_PICKER10) {
            // 多图选择
            if (intent == null) {
                Log.e("FormActivity", "data is null on requestCode=" + requestCode);
            } else {
                List<Photo> photos = intent.getParcelableArrayListExtra(EasyPhotos.RESULT_PHOTOS);
                if (ObjectUtils.isEmpty(photos)) {
                    Log.e("FormActivity", "photos is empty on requestCode=" + requestCode);
                } else {
                    onPhotoMultiSelectSuccess(requestCode, photos);
                }
            }
        } else if (resultCode == RESULT_OK && requestCode >= RequestCode.PHOTO_PICKER && requestCode <= RequestCode.PHOTO_PICKER10) {
            // 单图，获取到结果
            if (intent == null) {
                Log.e("FormActivity", "data is null on requestCode=" + requestCode);
            } else {
                List<String> photos = intent.getStringArrayListExtra(EasyPhotos.RESULT_PATHS);
                if (ObjectUtils.isEmpty(photos) || ObjectUtils.isEmpty(photos.get(0))) {
                    Log.e("FormActivity", "photos is empty on requestCode=" + requestCode);
                } else {
                    onPhotoSelectedSuccess(requestCode, photos.get(0));
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == RequestCode.PHOTO_CROP_PICKER) {
            // 带裁剪图，获取选择结果
            if (intent == null) {
                Log.e("FormActivity", "data is null on requestCode=" + requestCode);
            } else {
                List<String> photos = intent.getStringArrayListExtra(EasyPhotos.RESULT_PATHS);
                if (ObjectUtils.isEmpty(photos) || ObjectUtils.isEmpty(photos.get(0))) {
                    Log.e("FormActivity", "photos is empty on requestCode=" + requestCode);
                } else {
                    try {
                        UCropUtils.initUCrop(this, Uri.fromFile(new File(photos.get(0))), Uri.fromFile(new File(photos.get(0))), true);
                    } catch (Exception e) {
                        ToastUtils.showShort("尝试裁剪图片失败");
                    }
                }
            }
        } else if (requestCode == UCrop.REQUEST_CROP) {
            // 带裁剪图，获得被裁剪后的图
            if (resultCode == RESULT_OK) {
                // 裁剪成功
                if (intent == null) {
                    Log.e("FormActivity", "data is null on requestCode=" + requestCode);
                } else {
                    Uri croppedPhotoUri = UCrop.getOutput(intent);
                    if (null == croppedPhotoUri) {
                        Log.e("FormActivity", "cropped output is null on requestCode=" + requestCode);
                    } else {
                        onPhotoCropSelectedSuccess(requestCode, croppedPhotoUri.getPath());
                    }
                }
            } else {
                // 裁剪失败
                try {
                    ToastUtils.showShort("图片裁剪失败:" + resultCode + "," + UCrop.getError(intent).getMessage());
                } catch (Exception e) {
                    ToastUtils.showShort("图片裁剪失败");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    /**
     * 多图获取成功
     *
     * @param photos
     */
    protected void onPhotoMultiSelectSuccess(int requestCode, List<Photo> photos) {
        List<String> unCompressFiles = new ArrayList<>();
        for (Photo photo : photos) {
            unCompressFiles.add(photo.path);
        }
        if (unCompressFiles.isEmpty()) {
            isPhotoMultiCompressing = false;
            onPhotoMultiCompressSuccess(requestCode, new ArrayList<>(), new ArrayList<>());
        } else {
            multiCompressedRawPaths = new ArrayList<>();
            multiCompressedFiles = new ArrayList<>();
            Luban.with(this)
                    .load(unCompressFiles)
                    .ignoreBy(100)
                    .filter(path -> !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif")))
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart(int index, String rawFilePath) {
                            // 压缩开始前调用，可以在方法内启动 loading UI
                            isPhotoMultiCompressing = true;
                            if (0 == index) {
                                showLoading("压缩图片...");
                            }
                        }

                        @Override
                        public void onSuccess(int index, String rawFilePath, File file) {
                            // 压缩成功后调用，返回压缩后的图片文件
                            multiCompressedRawPaths.add(rawFilePath);
                            multiCompressedFiles.add(file);
                        }

                        @Override
                        public void onError(int index, String rawFilePath, Throwable throwable) {
                            // 当压缩过程出现问题时调用
                            ToastUtils.showShort("图片压缩失败:" + throwable.getMessage());
                            throwable.printStackTrace();
                        }

                        @Override
                        public void onCompleted(int index, String rawFilePath) {
                            if (index >= unCompressFiles.size()) {
                                dismissLoading();
                                // 多图压缩完成
                                isEditing = true;
                                isPhotoMultiCompressing = false;
                                onPhotoMultiCompressSuccess(requestCode, multiCompressedRawPaths, multiCompressedFiles);
                                photoAdapter.setNewData(PhotoPickerEntity.filesAndPathsToEntities(multiCompressedRawPaths, multiCompressedFiles));
                            }
                        }

                        @Override
                        public void onSysError(Throwable e) {
                            dismissLoading();
                        }
                    }).launch();
        }
    }

    /**
     * 多图获取并压缩成功
     */
    protected void onPhotoMultiCompressSuccess(int requestCode, List<String> rawPathList, List<File> list) {

    }

    /**
     * 裁剪图获取成功
     *
     * @param photo
     */
    protected void onPhotoCropSelectedSuccess(int requestCode, String photo) {
        Luban.with(this)
                .load(photo)
                .ignoreBy(100)
                .filter(path -> !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif")))
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart(int index, String rawFilePath) {
                        // 压缩开始前调用，可以在方法内启动 loading UI
                        isPhotoCompressing = true;
                    }

                    @Override
                    public void onSuccess(int index, String rawFilePath, File file) {
                        // 压缩成功后调用，返回压缩后的图片文件
                        isEditing = true;
                        onPhotoCropCompressSuccess(requestCode, rawFilePath, file);
                    }

                    @Override
                    public void onError(int index, String rawFilePath, Throwable throwable) {
                        // 当压缩过程出现问题时调用
                        ToastUtils.showShort("单图片压缩失败:" + throwable.getMessage());
                    }

                    @Override
                    public void onCompleted(int index, String rawFilePath) {
                        isPhotoCompressing = false;
                    }

                    @Override
                    public void onSysError(Throwable e) {
                        isPhotoCompressing = false;
                    }
                }).launch();
    }

    /**
     * 裁剪图获取并压缩成功
     */
    protected void onPhotoCropCompressSuccess(int requestCode, String rawFilePath, File file) {

    }

    /**
     * 单图获取并压缩成功
     *
     * @param photo
     */
    protected void onPhotoSelectedSuccess(int requestCode, String photo) {
        Luban.with(this)
                .load(photo)
                .ignoreBy(100)
                .filter(path -> !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif")))
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart(int index, String rawFilePath) {
                        // 压缩开始前调用，可以在方法内启动 loading UI
                        isPhotoCompressing = true;
                    }

                    @Override
                    public void onSuccess(int index, String rawFilePath, File file) {
                        // 压缩成功后调用，返回压缩后的图片文件
                        isEditing = true;
                        onPhotoCompressSuccess(requestCode, rawFilePath, file);
                    }

                    @Override
                    public void onError(int index, String rawFilePath, Throwable throwable) {
                        // 当压缩过程出现问题时调用
                        ToastUtils.showShort("单图片压缩失败:" + throwable.getMessage());
                    }

                    @Override
                    public void onCompleted(int index, String rawFilePath) {
                        isPhotoCompressing = false;
                    }

                    @Override
                    public void onSysError(Throwable e) {
                        isPhotoCompressing = false;
                    }
                }).launch();
    }

    /**
     * 单图获取并压缩成功
     *
     * @param file
     */
    protected void onPhotoCompressSuccess(int requestCode,  String rawFilePath, File file) {

    }

    /**
     * 点击单图,如果存在多个单图,可以利用tag区分requestcode
     *
     * @param view
     */
    public void onPhotoClick(View view) {
        if (isPhotoCompressing) {
            ToastUtils.showLong("图片压缩中,请稍等...");
            return;
        }
        int tag = ViewUtils.getTagValue(view, RequestCode.PHOTO_PICKER);
        EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                .setFileProviderAuthority(AppUtils.getAppPackageName() + ".camera.file.provider")
                .setPuzzleMenu(false)
                .setCleanMenu(false)
                .setCount(1)
                .start(tag);
    }

    /**
     * 点击需要裁剪的图，比如头像
     *
     * @param view
     */
    public void onCropPhotoClick(View view) {
        if (isPhotoCropCompressing) {
            ToastUtils.showLong("图片压缩中,请稍等...");
            return;
        }
        EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                .setFileProviderAuthority(AppUtils.getAppPackageName() + ".camera.file.provider")
                .setPuzzleMenu(false)
                .setCleanMenu(false)
                .setCount(1)
                .start(RequestCode.PHOTO_CROP_PICKER);
    }

    /**
     * 区域选择
     *
     * @param view
     */
    public void onLocationChooseClick(View view) {
        int tag = ViewUtils.getTagValue(view, RequestCode.LOCATION_CHOOSE);
        Router.build("LocationChoose")
                .requestCode(tag)
                .go(this);
    }

    /**
     * 在退出时候提示正在编辑
     */
    @Override
    public void onBackPressed() {
        if (isPhotoInCompressing()) {
            showConfirmDialog("图片压缩中,退出后内容将会丢失。\n您确定要放弃?", (dialog, which) -> finish());
        } else if (isEditing) {
            showConfirmDialog("您尚未提交,退出后内容将会丢失。\n您确定要放弃?", (dialog, which) -> finish());
        } else {
            super.onBackPressed();
        }
    }

}
