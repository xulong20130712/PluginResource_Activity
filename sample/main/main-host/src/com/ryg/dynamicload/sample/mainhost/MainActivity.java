package com.ryg.dynamicload.sample.mainhost;

import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ryg.dynamicload.inject.InjectClassLoader;
import com.ryg.dynamicload.inject.InjectResource;
import com.ryg.dynamicload.resource.BaseDynamiceActivity;
import com.ryg.utils.DLUtils;

import java.io.File;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

public class MainActivity extends BaseDynamiceActivity implements OnItemClickListener {

    public static final String FROM = "extra.from";
    public static final int FROM_INTERNAL = 0;
    public static final int FROM_EXTERNAL = 1;

    private ArrayList<PluginItem> mPluginItems = new ArrayList<PluginItem>();
    private PluginAdapter mPluginAdapter;

    private ListView mListView;
    private TextView mNoPluginTextView;
    
    private ServiceConnection mConnection;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

        pluginPackageName= "com.ryg.dynamicload.sample.mainhost";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        mPluginAdapter = new PluginAdapter();
        mListView = (ListView) findViewById(R.id.plugin_list);
        mNoPluginTextView = (TextView)findViewById(R.id.no_plugin);
    }

    private void initData() {
        String pluginFolder = Environment.getExternalStorageDirectory() + "/DynamicLoadHost";
        File file = new File(pluginFolder);
        File[] plugins = file.listFiles();
        if (plugins == null || plugins.length == 0) {
            mNoPluginTextView.setVisibility(View.VISIBLE);
            return;
        }

        for (File plugin : plugins) {

            if(plugin.isFile()&& plugin.getName().endsWith(".apk")) {

                PluginItem item = new PluginItem();
                item.pluginPath = plugin.getAbsolutePath();
                item.packageInfo = DLUtils.getPackageInfo(this, item.pluginPath);
                item.packageName= item.packageInfo.packageName;
                Log.e("+-->", "---item.packageName---"+ item.packageName);
                if (item.packageInfo.activities != null && item.packageInfo.activities.length > 0) {
                    item.launcherActivityName = item.packageInfo.activities[0].name;
                }
                if (item.packageInfo.services != null && item.packageInfo.services.length > 0) {
                    item.launcherServiceName = item.packageInfo.services[0].name;
                }
                mPluginItems.add(item);
//            DLPluginManager.getInstance(this).loadApk(item.pluginPath);
            }
        }

        mListView.setAdapter(mPluginAdapter);
        mListView.setOnItemClickListener(this);
        mPluginAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            DLUtils.showDialog(this, getString(R.string.action_about), getString(R.string.introducation));
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class PluginAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public PluginAdapter() {
            mInflater = MainActivity.this.getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mPluginItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mPluginItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.plugin_item, parent, false);
                holder = new ViewHolder();
                holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
                holder.appName = (TextView) convertView.findViewById(R.id.app_name);
                holder.apkName = (TextView) convertView.findViewById(R.id.apk_name);
                holder.packageName = (TextView) convertView.findViewById(R.id.package_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            PluginItem item = mPluginItems.get(position);
            PackageInfo packageInfo = item.packageInfo;
            holder.appIcon.setImageDrawable(DLUtils.getAppIcon(MainActivity.this, item.pluginPath));
            holder.appName.setText(DLUtils.getAppLabel(MainActivity.this, item.pluginPath));
            holder.apkName.setText(item.pluginPath.substring(item.pluginPath.lastIndexOf(File.separatorChar) + 1));
            holder.packageName.setText(packageInfo.applicationInfo.packageName + "\n" + 
                                       item.launcherActivityName + "\n" + 
                                       item.launcherServiceName);
            return convertView;
        }
    }

    private static class ViewHolder {
        public ImageView appIcon;
        public TextView appName;
        public TextView apkName;
        public TextView packageName;
    }

    public static class PluginItem {
        public PackageInfo packageInfo;
        public String pluginPath;
        public String packageName;
        public String launcherActivityName;
        public String launcherServiceName;

        public PluginItem() {
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {

        long start= System.currentTimeMillis();
        PluginItem item = mPluginItems.get(position);
        injectClassLoader(item.pluginPath);
        Log.e("+-->", "---cost inject ClassLoder time---"+ (System.currentTimeMillis()- start));
        Class mainClass= null;
        try {

            Class loadClass= this.getClassLoader().loadClass(item.launcherActivityName);
            Log.e("+-->", "---loadClass---"+ loadClass);
            mainClass = Class.forName(item.launcherActivityName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Log.e("+-->", "---cost reflect Class time---"+ (System.currentTimeMillis()- start));
        InjectResource.injectResource(item.pluginPath, item.packageName, mainClass, this);
        Log.e("+-->", "--host  getString---"+ getString(R.string.action_settings)+ "---"+ getString(R.string.action_about)+ "---"+ getString(R.string.app_name)+ "---"+ getString(R.string.hello_world));
        Log.e("+-->", "---cost all time---"+ (System.currentTimeMillis()- start));
    }

    private void injectClassLoader(final String dexPath) {

        String nativeLibDir = this.getDir("pluginlib", Context.MODE_PRIVATE).getAbsolutePath();
        Log.e("+-->", "---injectClassLoader---"+ getClassLoader());
        DexClassLoader subClassLoader= InjectClassLoader.getPluginClassLoader(this, dexPath, nativeLibDir);
//        InjectClassLoader.injectClassLoaderByMerge(this.getClassLoader(), subClassLoader, "com.ryg.dynamicload.sample.mainpluginb.MainActivity", false, "");
        InjectClassLoader.injectClassLoaderByReplaceLoaderApk(subClassLoader, "com.ryg.dynamicload.sample.mainpluginb.MainActivity", this);

        String classPath = System.getProperty("java.class.path", ".");
        Log.e("+-->", "---classPath---"+ classPath);
        try {

            subClassLoader.loadClass("com.ryg.dynamicload.sample.mainhost.TestSameClass");
            ClassLoader classLoader= Class.forName("com.ryg.dynamicload.sample.mainhost.TestSameClass").getClassLoader();
            Log.e("+-->", "---subClassLoader---"+ subClassLoader);
            Log.e("+-->", "---classLoader---"+ classLoader);
            Log.e("+-->", "---done---");
        }catch (ClassNotFoundException e) {

            Log.e("+-->", "---subClassLoader error!!!!---");
            e.printStackTrace();
        }

//        File dexOutputDir = this.getDir("dex", Context.MODE_PRIVATE);
//        String dexOutputPath = dexOutputDir.getAbsolutePath();
//        InjectEqualityClassLoader injectEqualityClassLoader= new InjectEqualityClassLoader(dexPath, dexOutputPath, nativeLibDir, this.getClassLoader().getParent(), this.getClassLoader());
//        try {
//
////            Class sameClass_plugin= injectEqualityClassLoader.loadClass("com.ryg.dynamicload.sample.mainpluginb.MainActivity");
//            Class sameClass_plugin= injectEqualityClassLoader.loadClass("com.ryg.dynamicload.sample.mainhost.TestSameClass");
//            Class sameClass_host= this.getClassLoader().loadClass("com.ryg.dynamicload.sample.mainhost.TestSameClass");
//            Log.e("+-->", "---injectClassLoader plugin ---"+ sameClass_plugin);
//            Log.e("+-->", "---injectClassLoader host ---"+ sameClass_host);
//        }catch (ClassNotFoundException e) {
//
//            Log.e("+-->", "---ClassNotFoundException---");
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mConnection != null) {
	        this.unbindService(mConnection);
        }
    }

}
