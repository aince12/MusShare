package com.twinstartech.musshare.tools;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;

/**
 * Created by Siri on 3/26/2017.
 */

public class ProxyFactory {

    private static HttpProxyCacheServer sharedProxy;

    private ProxyFactory() {
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        return sharedProxy == null ? (sharedProxy = newProxy(context)) : sharedProxy;
    }

    private static HttpProxyCacheServer newProxy(Context context) {
        return new HttpProxyCacheServer(context);
    }
}