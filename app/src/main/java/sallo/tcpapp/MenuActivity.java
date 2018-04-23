package sallo.tcpapp;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Matteo Salati on 24/07/2017.
 */

public class MenuActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CoordinatorLayout coordinatorLayout;
    private TextView aboutMeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutMenu);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        aboutMeTextView = (TextView) findViewById(R.id.aboutMe);

    }
}
