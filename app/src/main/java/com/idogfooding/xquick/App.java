package com.idogfooding.xquick;

import com.blankj.utilcode.util.Utils;
import com.facebook.stetho.Stetho;
import com.idogfooding.backbone.BaseApplication;
import com.mob.MobSDK;
import com.tencent.bugly.Bugly;

/**
 * App
 *
 * @author Charles
 */
public class App extends BaseApplication {

    public static final boolean debug = false;

    private static App instance;

    /**
     * support a method to get a instance for the outside
     */
    public synchronized static App getInstance() {
        if (null == instance) {
            instance = new App();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // init utils
        Utils.init(this);
        // init okgo
        initOkHttp();
        // init bugly
        Bugly.init(getApplicationContext(), Const.Cfg.BUGLY_APPID, debug);
        // init tbs
        /*QbSdk.initX5Environment(getApplicationContext(), new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
            }

            @Override
            public void onCoreInitFinished() {
            }
        });*/
        // init push
        // init sharesdk
        MobSDK.init(this);
        // init Stetho
        if (isDebug()) {
            Stetho.initializeWithDefaults(this);
        }

        // init flowdb
        /*FlowManager.init(FlowConfig.builder(this)
                .addDatabaseConfig(DatabaseConfig.builder(MsgDatabase.class).databaseName("MsgDB").build())
                .build());*/

        initLog();
        initCrash(false);
        initDialog();
    }

}
