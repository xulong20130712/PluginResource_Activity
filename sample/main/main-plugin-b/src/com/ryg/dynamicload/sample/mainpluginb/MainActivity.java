package com.ryg.dynamicload.sample.mainpluginb;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ryg.dynamicload.resource.BaseDynamiceActivity;

public class MainActivity extends BaseDynamiceActivity {

    private static final String TAG = "MainActivity";

    static {

        Log.e("+-->", "---MainActivity static---");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

        pluginPackageName= "com.ryg.dynamicload.sample.mainpluginb";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(savedInstanceState);

        Intent intent= new Intent(this, Service1.class);
        startService(intent);
        Intent intent2= new Intent(this, Service2.class);
        startService(intent2);
        Intent intent3= new Intent(this, Service3.class);
        startService(intent3);
        Intent intent4= new Intent(this, Service4.class);
        startService(intent4);

        getAllString();
    }

    private void getAllString() {

        String set= getString(R.string.action_settings);
        String set1= getString(R.string.action_settings1);
        String set2= getString(R.string.action_settings2);
        String set3= getString(R.string.action_settings3);
        String set4= getString(R.string.action_settings4);
        String set5= getString(R.string.action_settings5);
        String set6= getString(R.string.action_settings6);
        Log.e("+-->", "---getAllString---"+ set+ "--"+ set1+ "--"+ set2+ "--"+ set3+ "--"+ set4+ "--"+ set5+ "--"+ set6);
    }

    private void initView(Bundle savedInstanceState) {
        this.setContentView(generateContentView(this));
    }

    private View generateContentView(final Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        Button button = new Button(context);
        button.setText(R.string.action_settings);
        layout.addView(button, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                TestHostClass testHostClass = new TestHostClass();
//                testHostClass.testMethod(this);
            }
        });
        
        TextView textView = new TextView(context);
        textView.setText(R.string.hello_world6);
        textView.setTextSize(30);
        layout.addView(textView, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        return layout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult resultCode=" + resultCode);
        if (resultCode == RESULT_FIRST_USER) {
            this.finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public Resources getResources() {

        Resources resources= super.getResources();
        return resources;
    }
}