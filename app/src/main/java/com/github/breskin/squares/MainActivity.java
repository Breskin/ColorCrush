package com.github.breskin.squares;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {

    private MainView mMainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mMainView = new MainView(this);
        mMainView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(mMainView);

        super.onCreate(savedInstanceState);
    }

    public void openBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/breskin67"));
        startActivity(browserIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMainView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mMainView.resume();
    }

    @Override
    public void onBackPressed() {
        if (!mMainView.onBackPressed())
            super.onBackPressed();
    }
}
