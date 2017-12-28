package com.ryg.dynamicload.inject;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import com.ryg.dynamicload.resource.DynamicResourceManager;

import java.lang.reflect.Method;

import static com.ryg.dynamicload.inject.Reflect.onObject;

/**
 * Created by xulong on 2017/12/27.
 */

public class InjectResource {

    public static void injectResource(final String dexPath, final String packageName, final Class mainClass, final Context context) {

        long start = System.currentTimeMillis();
        Log.e("+-->", "---host resource---" + context.getResources());
        Log.e("+-->", "---host AssetManager---" + Reflect.onObject(context.getResources()).get("mAssets"));
        try {

            Object loadedApkInstance;
            Object baseContext = Reflect.getField(context.getApplicationContext(), ContextWrapper.class, "mBase");
            loadedApkInstance = Reflect.getField(baseContext, Class.forName("android.app.ContextImpl"), "mPackageInfo");
            String mResDir= Reflect.onObject(loadedApkInstance).get("mResDir");
            Log.e("+-->", "---old resourceDirs---" + mResDir);
        }catch (Exception e) {

            e.printStackTrace();
        }
        AssetManager assetManager = createAssetManager(dexPath);
        Resources resources = createResources(assetManager, context);
        Log.e("+-->", "---plugin resource---" + resources);
        Log.e("+-->", "---plugin AssetManager---" + Reflect.onObject(resources).get("mAssets"));
        Log.e("+-->", "---host resource inject after---" + context.getResources());
        Log.e("+-->", "---host assetManager inject after---" + Reflect.onObject(context.getResources()).get("mAssets"));
        DynamicResourceManager.getInstance().setDynamicResource(packageName, resources);
        resetHostResource(context);
        Log.e("+-->", "---host resource after reset---" + context.getResources());
        Log.e("+-->", "---host AssetManager after reset---" + Reflect.onObject(context.getResources()).get("mAssets"));
        try {

            Object loadedApkInstance;
            Object baseContext = Reflect.getField(context.getApplicationContext(), ContextWrapper.class, "mBase");
            loadedApkInstance = Reflect.getField(baseContext, Class.forName("android.app.ContextImpl"), "mPackageInfo");
            String mResDir= Reflect.onObject(loadedApkInstance).get("mResDir");
            Log.e("+-->", "---new resourceDirs---" + mResDir);
        }catch (Exception e) {

            e.printStackTrace();
        }
        Log.e("+-->", "---cost inject Resource time---" + (System.currentTimeMillis() - start));
        Intent intent = new Intent(context, mainClass);
        context.startActivity(intent);
        Log.e("+-->", "---cost start Activity time---" + (System.currentTimeMillis() - start));
    }

    private static AssetManager createAssetManager(String dexPath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
//            Method addAssetPath = assetManager.getClass().getMethod("addOverlayPath", String.class);
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, dexPath);
            return assetManager;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static void resetHostResource(final Context context) {

        Resources newRes= context.getResources();
        onObject(context).set("mResources", null);
        Object mOverrideConfiguration= Reflect.onObject(context).get("mOverrideConfiguration");
        Log.e("+-->", "---mOverrideConfiguration---"+ mOverrideConfiguration);
        context.getResources();
        Resources oldRes= context.getResources();
        Log.e("+-->", "---resetHostResource---"+ newRes+ "---"+ oldRes);
    }

    private static Resources createResources(AssetManager assetManager, final Context context) {
        Resources superRes = context.getResources();
        Resources resources = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        return resources;
    }
}
