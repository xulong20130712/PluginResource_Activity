package com.ryg.dynamicload.inject;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by xulong on 2017/12/27.
 */

public class PluginPackageInfo extends PackageInfo{


    public String appName= "";
    public String packageName= "";
    public String versionName= "";
    public PackageManager packageManager;
    public PackageInfo packageInfo;
    public ApplicationInfo applicationInfo;
}
