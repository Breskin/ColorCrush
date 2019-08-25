package com.github.breskin.squares;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class MainActivity extends Activity {

    private RenderView renderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        renderView = new RenderView(this);
        renderView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(renderView);

        super.onCreate(savedInstanceState);
    }

    public void openBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/breskin67"));
        startActivity(browserIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        renderView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        renderView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        renderView.resume();
    }

    @Override
    public void onBackPressed() {
        if (!renderView.onBackPressed())
            super.onBackPressed();
    }
}
