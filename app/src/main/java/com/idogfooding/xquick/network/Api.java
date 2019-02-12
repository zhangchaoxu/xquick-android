package com.idogfooding.xquick.network;


import com.idogfooding.xquick.App;

/**
 * Api
 *
 * @author Charles
 */
public class Api {

    public static final String SERVER_API = "";
    public static final String TOKEN_KEY = "member_token";

    private static String getApi() {
        return App.getInstance().getApi();
    }

}
