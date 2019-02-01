package com.zymobi.plugin.uexsignet;

import android.content.Context;

import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

/**
 * File Description: 入口类
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 2019/2/1.
 */
public class EUExSignet extends EUExBase {


    public EUExSignet(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
    }

    @Override
    protected boolean clean() {
        return false;
    }
}
