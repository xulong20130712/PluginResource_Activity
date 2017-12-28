package com.ryg.dynamicload.inject;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import static com.ryg.dynamicload.inject.Reflect.getField;
import static com.ryg.dynamicload.inject.Reflect.setField;

/**
 * Created by xulong on 2017/12/20.
 */

public class InjectClassLoader {


    public static DexClassLoader getPluginClassLoader(final Context context, final String dexPath, final String mNativeLibDir) {

        File dexOutputDir = context.getDir("dex", Context.MODE_PRIVATE);
        String dexOutputPath = dexOutputDir.getAbsolutePath();
        Log.e("+-->", "---getPluginClassLoader---"+ dexOutputPath);
        PluginDexClassLoder loader = new PluginDexClassLoder(dexPath, dexOutputPath, mNativeLibDir, context.getClassLoader(), context.getClassLoader());
        return loader;
    }


    /**
     *
     * 将两个dexElements合并成一个，以宿主ClassLoader作为插件ClassLoader的父
     * @param parentClassLoader，宿主ClassLoader
     * @param childClassLoader，插件ClassLoader
     * @param checkClass，验证插件ClassLoader加载成功
     * @param parentFirst，是否先加载宿主dex
     * @param dexPath，dex路径
     */
    public static void injectClassLoaderByMerge(ClassLoader parentClassLoader, ClassLoader childClassLoader, String checkClass, boolean parentFirst, String dexPath) {


        PathClassLoader pathClassLoader = (PathClassLoader) parentClassLoader;
        DexClassLoader dexClassLoader = (DexClassLoader) childClassLoader;
        // 验证classloader有效
        try {
            dexClassLoader.loadClass(checkClass);

            Object pathClassLoaderPathList = Reflect.getPathList(pathClassLoader);
            Object dexClassLoaderPathList = Reflect.getPathList(dexClassLoader);

            Object dexElements = null;
            dexElements = combineArray(Reflect.getDexElements(pathClassLoaderPathList), Reflect.getDexElements(dexClassLoaderPathList));

            setField(pathClassLoaderPathList, pathClassLoaderPathList.getClass(), "dexElements", dexElements);

            Object dexNativeLibraryDirs = combineArray(Reflect.getNativeLibraryDirectories(pathClassLoaderPathList),
                    Reflect.getNativeLibraryDirectories(dexClassLoaderPathList));

            if (dexNativeLibraryDirs instanceof File[]) {
                Arrays.sort((File[]) dexNativeLibraryDirs, getSoPathComparator());
            } else if (dexNativeLibraryDirs instanceof List) {
                List<File> list = (List<File>) dexNativeLibraryDirs;
                Collections.sort(list, getSoPathComparator());
                dexNativeLibraryDirs = list;
            }
            setField(pathClassLoaderPathList, pathClassLoaderPathList.getClass(), "nativeLibraryDirectories", dexNativeLibraryDirs);

            Object systemNativeLibraryDirs = getField(pathClassLoaderPathList, pathClassLoaderPathList.getClass(), "systemNativeLibraryDirectories");
            List<File> listSystem = (List<File>) systemNativeLibraryDirs;
            List<File> listSelf = (List<File>) dexNativeLibraryDirs;
            List<File> listAll = new ArrayList<File>(listSelf);
            listAll.addAll(listSystem);
            Method method = pathClassLoaderPathList.getClass().getDeclaredMethod("makePathElements", List.class, File.class, List.class);
            method.setAccessible(true);
            Object object = method.invoke(pathClassLoaderPathList.getClass(), listAll, null, new ArrayList<IOException>());
            setField(pathClassLoaderPathList, pathClassLoaderPathList.getClass(), "nativeLibraryPathElements", object);
        } catch (ClassNotFoundException e) {

            Log.e("+-->", "---injectClassLoader  error!!!!!!!!---");
            e.printStackTrace();
        } catch (Throwable t) {

            Log.e("+-->", "---Throwable   injectClassLoader  error!!!!!!!!---");
            t.printStackTrace();
        }
    }


    /**
     *
     * 替换LoadedApk中ClassLoader，以宿主ClassLoader作为插件ClassLoader的父
     * @param classLoader，插件ClassLoader父类继承自宿主classLoader
     * @param checkClass，验证插件ClassLoader是否可用
     * @param context
     */
    public static void injectClassLoaderByReplaceLoaderApk(ClassLoader classLoader, final String checkClass, final Context context) {

        Object LoadedApk;

        try {

            // 验证classloader有效
            classLoader.loadClass(checkClass);
            Object baseContext = Reflect.getField(context.getApplicationContext(), ContextWrapper.class, "mBase");
            LoadedApk = Reflect.getField(baseContext, Class.forName("android.app.ContextImpl"), "mPackageInfo");
            Class loadedClass= LoadedApk.getClass();
            Reflect.setField(LoadedApk, loadedClass, "mClassLoader", classLoader);
        }catch (ClassNotFoundException e) {

            Log.e("+-->", "---ClassNotFoundException---");
            e.printStackTrace();
        } catch (NoSuchFieldException e) {

            Log.e("+-->", "---NoSuchFieldException---");
            e.printStackTrace();
        } catch (IllegalAccessException e) {

            Log.e("+-->", "---IllegalAccessException---");
            e.printStackTrace();
        }
    }

    private static final Comparator<File> getSoPathComparator() {
        return new Comparator<File>() {

            @Override
            public int compare(File object1, File object2) {
                if (object1 != null && object2 != null) {
                    return compareBySelf(object1.getAbsolutePath(), object2.getAbsolutePath());
                }
                return 0;
            }
        };
    }

    private static final int compareBySelf(String str1, String str2) {
        if (str1 != null && str2 != null) {
            int left = 0;
            int right = 0;
            if (str1.contains("com.test")) {
                left = -1;
            }
            if (str2.contains("com.test")) {
                right = -1;
            }
            return left - right;
        }
        return 0;
    }

    private static Object combineArray(Object aArrayLhs, Object aArrayRhs) {
        if (aArrayLhs == null) {
            return aArrayRhs;
        }
        if (aArrayRhs == null) {
            return aArrayLhs;
        }
        if (aArrayLhs.getClass().isArray() && aArrayRhs.getClass().isArray()) {
            Class<?> localClass = aArrayLhs.getClass().getComponentType();
            int i = Array.getLength(aArrayLhs);
            int j = i + Array.getLength(aArrayRhs);
            Object result = Array.newInstance(localClass, j);
            for (int k = 0; k < j; k++) {
                if (k < i) {
                    Array.set(result, k, Array.get(aArrayLhs, k));
                } else {
                    Array.set(result, k, Array.get(aArrayRhs, k - i));
                }
            }
            return result;
        } else if (aArrayLhs instanceof List
                && aArrayRhs instanceof List) {
            List<File> lList = (List<File>) aArrayLhs;
            List<File> rList = (List<File>) aArrayRhs;
            ArrayList<File> result = new ArrayList<File>(lList.size() + rList.size());
            result.addAll(lList);
            result.addAll(rList);
            return result;
        } else {
            return aArrayLhs;
        }

    }
}