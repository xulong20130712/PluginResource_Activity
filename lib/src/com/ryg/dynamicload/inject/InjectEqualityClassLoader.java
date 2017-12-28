package com.ryg.dynamicload.inject;

import android.util.Log;

import dalvik.system.DexClassLoader;

/**
 * Created by xulong on 2017/12/21.
 */

public class InjectEqualityClassLoader extends DexClassLoader{


    private ClassLoader mHostClassLoader;

    public InjectEqualityClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent, final ClassLoader hostClassLoader) {

        super(dexPath, optimizedDirectory, libraryPath, parent);
        this.mHostClassLoader= hostClassLoader;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        Log.e("=-->", "---findClass    ---"+ name);
        Class<?> loadedClass= null;
        loadedClass= super.findClass(name);
        if(loadedClass== null&& mHostClassLoader!= null) {

            loadedClass= mHostClassLoader.loadClass(name);
            Log.e("+-->", "---mHostClassLoader---"+ loadedClass);
        }
        Log.e("+-->", "---findClass---"+ loadedClass);
        return loadedClass;
    }
}