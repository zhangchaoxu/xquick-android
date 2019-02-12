package com.idogfooding.xquick.common;

import android.Manifest;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.Route;
import com.idogfooding.backbone.widget.CountDownTimerWithPause;
import com.idogfooding.xquick.android.R;
import com.idogfooding.xquick.base.AppBaseActivity;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Splash
 * App启动页面
 * 支持启动页和广告
 * 支持通过接口替换内容
 *
 * @author Charles
 */
@Route(value = {"Splash"})
public class SplashActivity extends AppBaseActivity {

    // 在这个版本中是否需要重新进入intro
    boolean reActiveIntro = true;
    // 是否启动intro
    boolean activeIntro = false;

    @BindView(R.id.iv_splash)
    Banner ivSplash;
    @BindView(R.id.tv_skip)
    TextView tvSkip;

    @Override
    protected int getLayoutId() {
        return R.layout.splash;
    }

    // countDownTimer
    CountDownTimerWithPause countDownTimer;

    @Override
    protected void onSetup(Bundle savedInstanceState) {
        super.onSetup(savedInstanceState);
        // 覆盖状态栏
        BarUtils.setStatusBarVisibility(this, false);
    }

    protected void initViews() {
        // 有sb会喜欢在加载时候滚动多张图片，因此使用Banner
        ivSplash.setBannerStyle(BannerConfig.NOT_INDICATOR);
        ivSplash.setBannerAnimation(Transformer.Default);

        // 从缓存中读取和配置cfg
        /*CacheEntity<List<CfgSelect>> splashCfgList = null; //(CacheEntity<List<CfgSelect>>) CacheManager.getInstance().get("splashimage");
        if (null == splashCfgList || EmptyUtils.isEmpty(splashCfgList.getData())) {
            List<Integer> imgList = new ArrayList<>();
            imgList.add(R.mipmap.splash);
            ivSplash.setImages(imgList)
                    .setImageLoader(new GlideBannerImageLoader())
                    .start();
        } else {
            ivSplash.setImages(splashCfgList.getData())
                    .setImageLoader(new GlideBannerImageLoader())
                    .start();
                     OkGo.<HttpResult<List<CfgSelect>>>get(Api.CFG_LIST)
                .tag(this)
                .cacheKey("splashimage")
                .cacheMode(CacheMode.FIRST_CACHE_THEN_REQUEST)
                .params("type", "splashimage")
                .params("versionOnly", false)
                .execute(new ApiCallback<>(this));
        }
*/
        List<Integer> imgList = new ArrayList<>();
        imgList.add(R.mipmap.splash);
        ivSplash.setImages(imgList)
                .setImageLoader(new GlideBannerImageLoader())
                .start();


        // 暴力处理，在启动页中一次性请求所有需要的权限，强制用户授权
        askForPermissions(Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    protected void afterPermissionGranted() {
        // tvSkip.setVisibility(View.VISIBLE);
        // init counter down timer
        countDownTimer = new CountDownTimerWithPause(DateUtils.SECOND_IN_MILLIS * 1, DateUtils.SECOND_IN_MILLIS) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isFinishing()) {
                    tvSkip.setText(getString(R.string.skip_wait, (millisUntilFinished / 1000)));
                }
            }

            @Override
            public void onFinish() {
                tvSkip.setText("正在跳转");
                checkAccountLogin();
            }
        };
        countDownTimer.start();
    }

    @OnClick({R.id.tv_skip})
    public void onSkipClick(View view) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        checkAccountLogin();
    }

    private void checkAccountLogin() {
        Router.build("Home").go(this);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (null != countDownTimer) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ivSplash.startAutoPlay();
        // 当是初次安装或者是新版本需要重新引导
        if (activeIntro
                && (SPUtils.getInstance("cfg").getBoolean("first", true)
                || (reActiveIntro && SPUtils.getInstance("cfg").getBoolean("first" + AppUtils.getAppVersionCode(), true)))) {
            Router.build("Intro").go(this);
        } else {
            initViews();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ivSplash.stopAutoPlay();
    }

}
