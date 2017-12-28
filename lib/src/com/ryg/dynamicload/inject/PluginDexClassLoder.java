package com.ryg.dynamicload.inject;

import dalvik.system.DexClassLoader;

/**
 * Created by xulong on 2017/12/27.
 */

public class PluginDexClassLoder extends DexClassLoader {

    private ClassLoader mHostClassLoader;

    public PluginDexClassLoder(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent, final ClassLoader hostClassLoader) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        this.mHostClassLoader= hostClassLoader;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        Class getClass= null;
        try {
            getClass= super.findClass(name);
        }catch (ClassNotFoundException e) {

        }
        if(getClass== null) {

            getClass= mHostClassLoader.loadClass(name);
        }
        return getClass;
    }
}
