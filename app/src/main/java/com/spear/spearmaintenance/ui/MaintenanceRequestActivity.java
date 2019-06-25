package com.spear.spearmaintenance.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spear.spearmaintenance.R;
import com.spear.spearmaintenance.data.Constants;
import com.spear.spearmaintenance.data.Part;
import com.spear.spearmaintenance.data.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MaintenanceRequestActivity extends AppCompatActivity {

    private String mRequestType;
    private EditText mTamInput;
    private List<String> options;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_request);
        mTamInput = findViewById(R.id.tam_input);
        mTamInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                } else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                }
            }
        });
        options = new ArrayList<>();
        initDB();
        setupUI(findViewById(R.id.maintenancePage1));
    }

    /**
     * Create a dropdown menu with options for setting data fetching time-interval
     * Eg. Selecting '10s' will get reading from sensor every 10sec
     */
    private void createSpinner() {
        Spinner spinner = findViewById(R.id.req_spinner);
        options.add("Custom");
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


                if(options.get(i).equalsIgnoreCase("custom")) {
                    nextBtn.setText("Create Custom Request");
                    ReviewMaintenanceRequestActivity.parts = new ArrayList<>();
                    mRequestType = Constants.CUSTOM;
                    ReviewMaintenanceRequestActivity.parts = new ArrayList<>();
                    nextBtn.setEnabled(true);
                } else {
                    nextBtn.setText("Review Request");
                    mRequestType = options.get(i);
                    db.collection("maintenance-requests").whereEqualTo("description", options.get(i)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            ReviewMaintenanceRequestActivity.parts = new ArrayList<>();
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            if(doc != null) {
                                List<HashMap<String, String>> maps = ((ArrayList<HashMap<String, String>>) doc.getData().get("parts"));
                                for(HashMap<String, String> map : maps) {
                                    Part p = new Part(map.get("Name"), map.get("Quantity"));
                                    p.SerialNumber = map.get("SerialNumber");
                                    ReviewMaintenanceRequestActivity.parts.add(p);
                                }
                            }
                            nextBtn.setEnabled(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MaintenanceRequestActivity.this,
                                    "Unable to Connect to Internet.",
                                    Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(MaintenanceRequestActivity.this, MainActivity.class);
                            startActivity(i);
                            Log.w("Spear Maint.", "Error getting documents.");
                        }
                    });
                }
                }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                nextBtn.setText("Select Request");
                nextBtn.setEnabled(false);
            }
        });
    }

    private void moveNext() {
                Intent i = new Intent(MaintenanceRequestActivity.this, ReviewMaintenanceRequestActivity.class);
                ReviewMaintenanceRequestActivity.TAM =mTamInput.getText().toString();
                ReviewMaintenanceRequestActivity.Description = mRequestType;
                startActivity(i);
    }

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    Utils.hideSoftKeyboard(MaintenanceRequestActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    private void initDB() {
        db.collection("maintenance-requests")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String des = document.get("description") != null ? document.get("description").toString() : "";
                                if(!options.contains(des) ) {
                                    options.add(des);
                                }
                            }
                            createSpinner();
                        } else {
                            Toast.makeText(MaintenanceRequestActivity.this,
                                    "Unable to Connect to Internet.",
                                    Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(MaintenanceRequestActivity.this, MainActivity.class);
                            startActivity(i);
                            Log.w("Spear Maint.", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}
