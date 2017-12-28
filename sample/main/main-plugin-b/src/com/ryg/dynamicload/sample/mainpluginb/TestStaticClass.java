package com.ryg.dynamicload.sample.mainpluginb;

import android.util.Log;

/**
 * Created by xulong on 2017/12/19.
 */

public class TestStaticClass {


    static {

        Log.e("+-->", "---TestStaticClass---");
        staticFunc1();
    }

    private static void staticFunc1() {

        Log.e("+-->", "---staticFunc1---");
    }
}
