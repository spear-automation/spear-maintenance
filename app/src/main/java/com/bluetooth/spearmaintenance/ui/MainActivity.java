package com.bluetooth.spearmaintenance.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.bluetooth.spearmaintenance.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button maintBtn = findViewById(R.id.maintBtn);
        maintBtn.setOnClickListener(view -> toMaintReq());
    }

    private void toMaintReq() {
        Intent i = new Intent(MainActivity.this, MaintenanceRequestActivity.class);
        startActivity(i);
    }
}
