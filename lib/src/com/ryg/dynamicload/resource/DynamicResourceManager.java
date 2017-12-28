package com.ryg.dynamicload.resource;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.util.ArrayMap;

/**
 * Created by xulong on 2017/12/22.
 */

@TargetApi(Build.VERSION_CODES.KITKAT)
public class DynamicResourceManager {


    private ArrayMap<String, Resources> resourceContainer= new ArrayMap<String, Resources>(20);

    private DynamicResourceManager() {

    }

    private static class InstanceHolder {

        private final static DynamicResourceManager instance= new DynamicResourceManager();
    }
    public static DynamicResourceManager getInstance() {

        return InstanceHolder.instance;
    }

    public Resources getDynamicResource(String packageName) {

        return resourceContainer.get(packageName);
    }

    public void setDynamicResource(final String packageName, final Resources resources) {

        resourceContainer.put(packageName, resources);
    }

}
