package sallo.tcpapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

public class BlocklyActivity extends AppCompatActivity  {

    private Toolbar mToolbar;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blockly);

        Intent intent = getIntent();
        String arrayJson = intent.getStringExtra("simboliJSON");
        int level = intent.getIntExtra("level", 1);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutMenu);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        SharedPreferences sharedPref = this.getSharedPreferences("tiger536_maze", Context.MODE_PRIVATE);
        int maze = sharedPref.getInt("maze", level);

        WebView myWebView = (WebView) findViewById(R.id.webviewBlockly);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setDisplayZoomControls(false);
        myWebView.loadUrl("file:///android_asset/maze.html?"+"code="+arrayJson+"&level="+maze);

    }
}
