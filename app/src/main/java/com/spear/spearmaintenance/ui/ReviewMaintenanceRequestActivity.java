package com.spear.spearmaintenance.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spear.spearmaintenance.R;
import com.spear.spearmaintenance.data.Constants;
import com.spear.spearmaintenance.data.Part;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewMaintenanceRequestActivity extends AppCompatActivity {

    public static class ViewHolder {
        public TextView number;
        public TextView name;
        public TextView quantity;
    }

    public static List<Part> parts;
    public ListAdapter parts_list_adapter;
    public static String TAM;
    private TextView hintLabel;
    public static String Description;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_maintenance_request);
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(ReviewMaintenanceRequestActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ReviewMaintenanceRequestActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        } else {
            // Permission has already been granted
        }
        parts_list_adapter = new ListAdapter();
        hintLabel = findViewById(R.id.textView10);
        if(parts.size() > 0) {
            hintLabel.setText("Tap a Part to Edit its Quantity or Remove it!");
        } else {
            hintLabel.setText("Tap the Plus to Add Parts");
        }

        ListView listView = (ListView) this.findViewById(R.id.needed_parts);
        listView.setAdapter(parts_list_adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Show alert box that asks to confirm deletion
                AlertDialog.Builder alert = new AlertDialog.Builder(ReviewMaintenanceRequestActivity.this);
                // Set up the input
                final EditText input = new EditText(ReviewMaintenanceRequestActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                alert.setView(input);
                alert.setTitle("Remove or Edit Quantity");
                alert.setMessage("How many of this part do you need?");

                // Set up the buttons
                alert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(input.getText().length() != 0) {
                            Part p = parts_list_adapter.getItem(position);
                            parts.remove(p);
                            parts_list_adapter.removePart(p);
                            String x = input.getText().toString();
                            p.Quantity = x;
                            parts.add(p);
                            parts_list_adapter.addPart(p);
                            parts_list_adapter.notifyDataSetChanged();
                        } else {
                            dialog.dismiss();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).setNeutralButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Part p = parts_list_adapter.getItem(position);
                        parts.remove(p);
                        if(parts.size() < 1) {
                            hintLabel.setText("Tap the Plus to Add Parts");
                        }
                        parts_list_adapter.removePart(p);
                        parts_list_adapter.notifyDataSetChanged();
                    }
                });
                alert.show();
            }
        });
        parts_list_adapter.addAll(parts);

        parts_list_adapter.notifyDataSetChanged();

        TextView addLabel = findViewById(R.id.add_part_label);
        addLabel.setOnClickListener(view -> addItemPopup());

        Button submitBtn = findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(view -> addToDB());

        if(parts.size() < 1) {
            // Show alert box that asks to confirm deletion
            AlertDialog.Builder alert = new AlertDialog.Builder(ReviewMaintenanceRequestActivity.this);
            // Set up the input
            final EditText input = new EditText(ReviewMaintenanceRequestActivity.this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            alert.setView(input);
            alert.setTitle("Describe this Request");
            alert.setMessage("2-5 Words to Describe this (e.g. Right Rear Tire Flat)");

            // Set up the buttons
            alert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(input.getText().length() > 0) {
                        Description = input.getText().toString();
                    } else {
                        alert.show();
                    }
                }
            });

            alert.show();
        }
    }

    private void addToDB() {
        List<HashMap<String, String>> maps = new ArrayList<>();
        for(Part p : parts) {
            maps.add(p.toDBFormat());
        }
        Map<String, Object> req = new HashMap<>();
        req.put("TAM", TAM);
        req.put("description", Description);
        req.put("parts", maps);
        db.collection("maintenance-requests").add(req).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        submit();
                    }
                });
                Log.d("Spear Maint.", "DocumentSnapshot added with ID: " + documentReference.getId());
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReviewMaintenanceRequestActivity.this,
                                "Unable to Connect to Internet.",
                                Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(ReviewMaintenanceRequestActivity.this, MainActivity.class);
                        startActivity(i);
                        Log.w("Spear Maint.", "Error adding document", e);
                    }
                });
    }

    private void submit() {

        Uri fileUri = getFormattedSensorData();
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "New Maintenance Production Report -- " + new Date());
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.setData(Uri.parse("mailto:chipcovin1@gmail.com"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // Show alert box that asks to confirm deletion
            AlertDialog.Builder alert = new AlertDialog.Builder(ReviewMaintenanceRequestActivity.this);

            alert.setTitle("Request Sent");

            // Set up the buttons
            alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(ReviewMaintenanceRequestActivity.this, MainActivity.class);
                    startActivity(i);
                }
            });
            alert.show();
        } catch(Exception e)  {
            System.out.println("is exception raises during sending mail"+e);
            // Show alert box that asks to confirm deletion
            AlertDialog.Builder alert = new AlertDialog.Builder(ReviewMaintenanceRequestActivity.this);

            alert.setTitle("Request Couldn't Be Sent");

            // Set up the buttons
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alert.show();
        }

    }

    /**
     * Creates a CSV file with fields
     * @return File path of csv (Uri)
     */
    private Uri getFormattedSensorData() {

            String columnString;
            columnString = "\"TAM\",\"Serial Number\",\"Name\",\"Quantity\"";

            StringBuilder resultString = new StringBuilder(columnString);
            for (int i = 0; i < parts_list_adapter.getCount(); i++) {
                Part p = parts_list_adapter.getItem(i);

                    resultString.append("\n")
                            .append(TAM)
                            .append(",")
                            .append(p.SerialNumber)
                            .append(",")
                            .append(p.Name)
                            .append(",")
                            .append(p.Quantity);
            }

            String combinedString = resultString.toString();

            File file = null;
            File root = Environment.getExternalStorageDirectory();
            if (root.canWrite()) {
                File dir = new File(root.getAbsolutePath() + "/SpearMaintenance");
                dir.mkdirs();
                file = new File(dir, "maintenanceRequest" + new Date().toString() + ".csv");
                FileOutputStream out;
                try {
                    out = new FileOutputStream(file);
                    out.write(combinedString.getBytes());
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return (file == null) ? null : Uri.fromFile(file);
    }

    private void addItemPopup() {
        final AlertDialog.Builder initialAsk = new AlertDialog.Builder(this);
        initialAsk.setTitle("Add new part or find from existing?");
        initialAsk.setPositiveButton("Existing Part", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent t = new Intent(ReviewMaintenanceRequestActivity.this, ExistingPartsActivity.class);
                startActivity(t);
            }
        }).setNegativeButton("New Part", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                LinearLayout layout = new LinearLayout(ReviewMaintenanceRequestActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText titleBox = new EditText(ReviewMaintenanceRequestActivity.this);
                titleBox.setHint("Part Title");
                layout.addView(titleBox);

                final EditText descriptionBox = new EditText(ReviewMaintenanceRequestActivity.this);
                descriptionBox.setInputType(InputType.TYPE_CLASS_NUMBER);
                descriptionBox.setHint("Quantity");
                layout.addView(descriptionBox);

                final AlertDialog.Builder alert = new AlertDialog.Builder(ReviewMaintenanceRequestActivity.this);
                alert.setTitle("Add new Part");
                alert.setView(layout).setPositiveButton("Add",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String x = titleBox.getText().toString();
                                String y = descriptionBox.getText().toString();
                                Part p = new Part(x, y);
                                parts.add(p);
                                parts_list_adapter.addPart(p);
                                parts_list_adapter.notifyDataSetChanged();
                                if(parts.size() == 1) {
                                    hintLabel.setText("Tap a Part to Edit its Quantity or Remove it!");
                                }
                                dialog.cancel();
                            }
                        }).setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alert.show();
            }
        });
        initialAsk.show();
    }
    private class ListAdapter extends BaseAdapter {
        private ArrayList<Part> parts_list;

        public ListAdapter() {
            super();
            parts_list = new ArrayList<>();
        }

        public void addAll(List<Part> parts) {
            for (Part p : parts
                 ) {
                addPart(p);
            }
        }

        public void addPart(Part part) {

            if(part.Name == null) {
                return;
            }

            if(!parts_list.contains(part)) {
                parts_list.add(part);
            }
        }

        public boolean contains(Part part) {
            return parts_list.contains(part);
        }

        @Override
        public Part getItem(int position) {
            return parts_list.get(position);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public void clear() {
            parts_list.clear();
        }

        public void removePart(Part p) {
            parts_list.remove(p);
        }

        @Override
        public int getCount() {
            return parts_list.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = ReviewMaintenanceRequestActivity.this.getLayoutInflater().inflate(R.layout.row_needed_part, null);
                viewHolder = new ViewHolder();
                viewHolder.number = (TextView) view.findViewById(R.id.part_number);
                viewHolder.name = (TextView) view.findViewById(R.id.part_title);
                viewHolder.quantity = (TextView) view.findViewById(R.id.part_quantity);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.number.setText(parts_list.get(i).SerialNumber);
            viewHolder.quantity.setText(parts_list.get(i).Quantity);
            viewHolder.name.setText(parts_list.get(i).Name);

            return view;
        }
    }
}
