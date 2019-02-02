package com.zymobi.plugin.uexsignet;

import android.content.Context;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

import java.util.Map;

import cn.org.bjca.signet.component.core.activity.SignetCoreApi;
import cn.org.bjca.signet.component.core.activity.SignetToolApi;
import cn.org.bjca.signet.component.core.bean.results.FindBackUserResult;
import cn.org.bjca.signet.component.core.bean.results.GetUserListResult;
import cn.org.bjca.signet.component.core.bean.results.LoginResult;
import cn.org.bjca.signet.component.core.bean.results.SignetBaseResult;
import cn.org.bjca.signet.component.core.callback.FindBackUserCallBack;
import cn.org.bjca.signet.component.core.callback.LoginCallBack;
import cn.org.bjca.signet.component.core.enums.CertType;
import cn.org.bjca.signet.component.core.enums.FindBackType;
import cn.org.bjca.signet.component.core.enums.IdCardType;

/**
 * File Description: 入口类
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 2019/2/1.
 */
public class EUExSignet extends EUExBase {

    private static final String TAG = "EUExSignet";

    public EUExSignet(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
    }

    public void findBackUser(String[] params){
        if (params.length < 2){
            return;
        }
        String userName = null;
        String idCardNumber = null;
        IdCardType idCardType = null;

        final String jsCallbackId = params[params.length - 1];

        try {
            JSONObject json = new JSONObject(params[0]);
            userName = json.getString(JSConstants.JSON_KEY_USER_NAME);
            idCardNumber = json.getString(JSConstants.JSON_KEY_IDCARD_NUMBER);
            String idCardTypeStr = json.getString(JSConstants.JSON_KEY_IDCARD_TYPE);
            int idCardTypeInt = Integer.parseInt(idCardTypeStr);
            if (idCardTypeInt < IdCardType.values().length){
                idCardType = IdCardType.values()[idCardTypeInt];
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(userName)
                && TextUtils.isEmpty(idCardNumber)
                && idCardType == null){
            BDebug.i("findBackUser: 传入参数均为空，走自带UI的找回逻辑");
            //使用输入信息方式找回个人/企业用户
            SignetCoreApi.useCoreFunc(
                    new FindBackUserCallBack(mContext, FindBackType.FINDBACK_USER) {

                        @Override
                        public void onFindBackResult(FindBackUserResult findBackUserResult) {
                            Log.e(TAG, findBackUserResult.getErrMsg());
                            JSONObject json = new JSONObject();
                            try {
                                json.put(JSConstants.JSON_KEY_ERROR_CODE, findBackUserResult.getErrCode());
                                json.put(JSConstants.JSON_KEY_ERROR_MSG, findBackUserResult.getErrMsg());
                                json.put(JSConstants.JSON_KEY_MSSP_ID, findBackUserResult.getMsspID());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            jsCallbackJsonObjectWithCallbackId(jsCallbackId, json.toString());
                        }

                    });
        }else{
            BDebug.i("findBackUser: userName=" + userName + " idCardNumber=" + idCardNumber + " idCardType=" + idCardType);
            SignetCoreApi.useCoreFunc(new FindBackUserCallBack(mContext, userName, idCardNumber, idCardType) {

                @Override
                public void onFindBackResult(FindBackUserResult findBackUserResult) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put(JSConstants.JSON_KEY_ERROR_CODE, findBackUserResult.getErrCode());
                        json.put(JSConstants.JSON_KEY_ERROR_MSG, findBackUserResult.getErrMsg());
                        json.put(JSConstants.JSON_KEY_MSSP_ID, findBackUserResult.getMsspID());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jsCallbackJsonObjectWithCallbackId(jsCallbackId, json.toString());
                }

            });
        }
    }

    public void userLogin(String[] params){
        if (params.length < 2){
            return;
        }
        String msspId = null;
        String signId = null;
        try {
            JSONObject json = new JSONObject(params[0]);
            msspId = json.getString(JSConstants.JSON_KEY_MSSP_ID);
            signId = json.getString(JSConstants.JSON_KEY_SIGN_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String callbackId = params[1];
        final int callbackIdInt = Integer.parseInt(callbackId);

        SignetCoreApi.useCoreFunc(new LoginCallBack(mContext, msspId, signId) {

            @Override
            public void onLoginResult(LoginResult loginResult) {
                String loginResultStr = DataHelper.gson.toJson(loginResult);
                callbackToJs(callbackIdInt, false, loginResultStr);
            }

        });
    }

    public String getUserList(String[] params){
        GetUserListResult result = SignetToolApi.getUserList(mContext);
        Map<String, String> userList = result.getUserListMap();
        String jsonResult = DataHelper.gson.toJson(userList);
        return jsonResult;
    }

    public String clearCert(String[] params){
        if (params.length < 1){
            return null;
        }
        String msspId = null;
        CertType certType = null;
        try {
            JSONObject json = new JSONObject(params[0]);
            msspId = json.getString(JSConstants.JSON_KEY_MSSP_ID);
            String certTypeStr = json.getString(JSConstants.JSON_KEY_CERT_TYPE);
            int certTypeInt = Integer.parseInt(certTypeStr);
            if (certTypeInt < CertType.values().length){
                certType = CertType.values()[certTypeInt];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SignetBaseResult result = SignetToolApi.clearCert(mContext, msspId, certType);
        JSONObject json = new JSONObject();
        try {
            json.put(JSConstants.JSON_KEY_ERROR_CODE, result.getErrCode());
            json.put(JSConstants.JSON_KEY_ERROR_MSG, result.getErrMsg());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

//    private void callbackFindCertBackByUser(String jsonStr) {
//        Log.i(TAG, "callback->findCertBackByUser->cbFindCertBackByUser " + jsonStr);
//        jsCallbackJsonObject(JSConstants.CALLBACK_FIND_CERT_BACK_BY_USER, jsonStr);
//    }

    private void jsCallbackJsonObjectWithCallbackId(String jsCallbackId, String jsonStr) {
        if (TextUtils.isEmpty(jsCallbackId)){
            return;
        }
        int jsCallbackIdInt = Integer.parseInt(jsCallbackId);
        callbackToJs(jsCallbackIdInt, false, jsonStr);
    }

    @Override
    protected boolean clean() {
        return false;
    }
}
