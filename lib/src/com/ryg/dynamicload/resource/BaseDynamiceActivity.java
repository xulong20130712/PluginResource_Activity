package com.ryg.dynamicload.resource;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by xulong on 2017/12/22.
 */

public class BaseDynamiceActivity extends Activity {


    protected String pluginPackageName= "";

    @Override
    public Resources getResources() {

        Resources dynamicResource= DynamicResourceManager.getInstance().getDynamicResource(pluginPackageName);
        return dynamicResource== null? super.getResources(): dynamicResource;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void setPluginPackageName(String packageName) {

        if(TextUtils.isEmpty(packageName)) {

            pluginPackageName= this.getPackageName();
        }
        Log.e("+-->", "---pluginPackageName---"+ pluginPackageName);
    }
}