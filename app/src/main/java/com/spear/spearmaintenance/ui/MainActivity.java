package com.spear.spearmaintenance.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import com.spear.spearmaintenance.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        Button maintBtn = findViewById(R.id.maintBtn);
        maintBtn.setOnClickListener(view -> toMaintReq());


    }

    private void toMaintReq() {
        Intent i = new Intent(MainActivity.this, MaintenanceRequestActivity.class);
        startActivity(i);
    }
}
