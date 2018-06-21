package com.example.omer.imageserviceapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    /**
     * onCreate() is where we initialize our activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * startService() is the "onClick" function to bind
     * to the start button.
     * @param view - Ignored
     */
    public void startService(View view) {

        Intent intent = new Intent(this, ImageService.class);
        startService(intent);

    }

    /**
     * stopService() is the "onClick" function to bind
     * to the stop button.
     * @param view
     */
    public void stopService(View  view) {
        Intent intent = new Intent(this, ImageService.class);
        stopService(intent);
    }
}
