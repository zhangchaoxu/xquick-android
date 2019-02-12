package com.idogfooding.xquick;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.Utils;
import com.facebook.stetho.Stetho;
import com.idogfooding.backbone.BaseApplication;
import com.idogfooding.backbone.utils.GsonUtils;
import com.idogfooding.xquick.network.Api;
import com.idogfooding.xquick.user.User;
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

    // [+] 用户账户相关
    private User user;
    private String user_token;

    /**
     * 用户是否登录
     */
    public boolean isUserLogin() {
        return null != getUser() && !StringUtils.isEmpty(getUserToken());
    }

    /**
     * 获得当前登录用户
     */
    public User getUser() {
        if (null == user) {
            user = GsonUtils.fromJson(getSPInstance().getString("login_user"), User.class);
        }
        return user;
    }

    /**
     * 获得当前登录用户Token
     */
    public String getUserToken() {
        if (StringUtils.isEmpty(user_token)) {
            user_token = getSPInstance().getString("login_user_token");
        }
        return user_token;
    }

    /**
     * 注销用户登录
     */
    public void clearUserLogin() {
        getSPInstance().remove("login_user");
        getSPInstance().remove("login_user_token");
        getSPInstance().remove("login_user_time");
        user = null;
        user_token = null;
    }

    /**
     * 保存用户登录信息
     */
    public void saveUserLogin(User user, String user_token) {
        getSPInstance().put("login_user", user.toJson());
        getSPInstance().put("login_user_token", user_token);
        getSPInstance().put("login_user_time", System.currentTimeMillis());
        this.user = user;
        this.user_token = user_token;
    }

    /**
     * 更新用户信息
     */
    public void refreshUser(User user) {
        getSPInstance().put("login_user", user.toJson());
        this.user = user;
    }

    /**
     * 更新用户信息
     */
    public void refreshUserToken(String user_token) {
        getSPInstance().put("login_user_token", user_token);
        getSPInstance().put("login_user_time", System.currentTimeMillis());
        this.user_token = user_token;
    }

    /**
     * 加入忽略版本
     */
    public void setIgnoreVersionCode(int versionCode) {
        getSPInstance().put("settings_ignore_version_code", versionCode);
    }

    public int getIgnoreVersionCode() {
        return getSPInstance().getInt("settings_ignore_version_code", 0);
    }

    // [+] 服务器配置相关
    private String api_server;

    /**
     * 保存服务器地址
     */
    public boolean saveApiServer(String api_server) {
        if (StringUtils.isEmpty(api_server)) {
            return false;
        }
        this.api_server = api_server;
        getSPInstance().put("settings_api_server", api_server);
        return true;
    }

    /**
     * 获取服务器地址
     */
    public String getApi() {
        if (StringUtils.isTrimEmpty(api_server)) {
            api_server = getSPInstance().getString("api_server", Api.SERVER_API);
        }
        return api_server;
    }
}
