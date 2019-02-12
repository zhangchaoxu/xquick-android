package com.idogfooding.xquick.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;

import com.chenenyu.router.annotation.Route;
import com.idogfooding.xquick.android.R;
import com.idogfooding.xquick.base.AppBaseActivity;

import butterknife.BindView;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

/**
 * ScannerActivity
 * see {https://github.com/bingoogolapple/BGAQRCode-Android/}
 * 二维码扫描页面
 *
 * @author Charles
 */
@Route("Scanner")
public class ScannerActivity extends AppBaseActivity implements QRCodeView.Delegate {

    @BindView(R.id.zxingview)
    ZXingView zxingview;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_scanner;
    }

    @Override
    protected void onSetup(Bundle savedInstanceState) {
        super.onSetup(savedInstanceState);
        setTitle(R.string.scanner);
        initScanner();
    }

    /**
     * 初始化扫描器
     */
    private void initScanner() {
        zxingview.setDelegate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        zxingview.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
        zxingview.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别
    }

    @Override
    protected void onStop() {
        zxingview.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        zxingview.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (null != vibrator) {
            vibrator.vibrate(200);
        }
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        vibrate();

        // 将扫描结果返回
        Intent data = new Intent();
        data.putExtra("ScanResult", result);
        finish(Activity.RESULT_OK, data);

        zxingview.startSpot(); // 延迟0.1秒后开始识别
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
        String tipText = zxingview.getScanBoxView().getTipText();
        String ambientBrightnessTip = "\n环境过暗，请打开闪光灯";
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                zxingview.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
                zxingview.getScanBoxView().setTipText(tipText);
            }
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        showMessageDialog("打开相机出错");
    }

}
