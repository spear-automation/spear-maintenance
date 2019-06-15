package com.bluetooth.spearmaintenance.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.bluetooth.spearmaintenance.R;
import com.bluetooth.spearmaintenance.data.Constants;

import java.util.ArrayList;

public class MaintenanceRequestActivity extends AppCompatActivity {

    private String mRequestType;
    private EditText mTamInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_request);
        mTamInput = findViewById(R.id.tam_input);
        createSpinner();
    }

    /**
     * Create a dropdown menu with options for setting data fetching time-interval
     * Eg. Selecting '10s' will get reading from sensor every 10sec
     */
    private void createSpinner() {
        Spinner spinner = findViewById(R.id.req_spinner);
        ArrayList<String> options = new ArrayList<>();
        options.add("Custom");
        options.add("Leaky Oil Pan");
        options.add("Right Rear Axel Down");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                options);
        spinner.setAdapter(adapter);
        Button nextBtn = findViewById(R.id.moveBtn);
        nextBtn.setEnabled(false);
        nextBtn.setText("Select Request");
        nextBtn.setOnClickListener(view -> moveNext());
        // Map total_ticks to the selected item in spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        nextBtn.setText("Create Custom Request");
                        nextBtn.setEnabled(true);
                        mRequestType = Constants.CUSTOM;
                        break;
                    case 1:
                        nextBtn.setText("Review Request");
                        nextBtn.setEnabled(true);
                        mRequestType = Constants.LEAKY_OIL_PAN;
                        break;
                    case 2:
                        nextBtn.setText("Review Request");
                        nextBtn.setEnabled(true);
                        mRequestType = Constants.RIGHT_REAR_AXEL;
                        break;
                    default:
                        nextBtn.setEnabled(false);
                        nextBtn.setText("Select Request");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void moveNext() {
                Intent i = new Intent(MaintenanceRequestActivity.this, (mRequestType.equalsIgnoreCase(Constants.CUSTOM)) ? NewRequestFormActivity.class : ReviewMaintenanceRequestActivity.class);
                i.putExtra(Constants.TAM_INPUT, mTamInput.getText());
                i.putExtra(Constants.REQ_EXTRA, mRequestType);
                startActivity(i);
    }

}
