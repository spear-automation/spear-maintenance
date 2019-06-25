package com.spear.spearmaintenance.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.spear.spearmaintenance.R;
import com.spear.spearmaintenance.data.Part;

import java.util.ArrayList;
import java.util.List;

public class ExistingPartsActivity extends AppCompatActivity {
    public static class ViewHolder {
        public TextView number;
        public TextView name;
    }

    public static ListAdapter parts_list_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_parts);
        parts_list_adapter = new ListAdapter();

        ListView listView = (ListView) this.findViewById(R.id.existing_parts);
        listView.setAdapter(parts_list_adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
// Show alert box that asks to confirm deletion
                AlertDialog.Builder alert = new AlertDialog.Builder(ExistingPartsActivity.this);
                // Set up the input
                final EditText input = new EditText(ExistingPartsActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                alert.setView(input);
                alert.setTitle("How Many?");
                alert.setMessage("How many of this part do you need?");

                // Set up the buttons
                alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String x = input.getText().toString();
                        Part p = parts_list_adapter.getItem(position);
                        p.Quantity = x;
                        ReviewMaintenanceRequestActivity.parts.add(p);
                        Intent i = new Intent(ExistingPartsActivity.this, ReviewMaintenanceRequestActivity.class);
                        startActivity(i);
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alert.show();
            }
        });
        List<Part> existingParts = new ArrayList<>(ReviewMaintenanceRequestActivity.parts);
        existingParts.add(new Part("Front Axel", "0"));
        existingParts.add(new Part("Steering Wheel", "0"));
        existingParts.add(new Part("Driver's Seat", "0"));
        existingParts.add(new Part("Seat Belt", "0"));
        existingParts.add(new Part("Passenger Seat", "0"));
        existingParts.add(new Part("Back Seat", "0"));
        existingParts.add(new Part("Windshield", "0"));
        existingParts.add(new Part("Front Window", "0"));
        existingParts.add(new Part("Rear Window", "0"));
        parts_list_adapter.addAll(existingParts);

        parts_list_adapter.notifyDataSetChanged();

        Button cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(view -> {
            Intent i = new Intent(ExistingPartsActivity.this, ReviewMaintenanceRequestActivity.class);
            startActivity(i);
        });

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

        @Override
        public int getCount() {
            return parts_list.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ExistingPartsActivity.ViewHolder viewHolder;
            if (view == null) {
                view = ExistingPartsActivity.this.getLayoutInflater().inflate(R.layout.row_existing_part, null);
                viewHolder = new ViewHolder();
                viewHolder.number = (TextView) view.findViewById(R.id.part_number);
                viewHolder.name = (TextView) view.findViewById(R.id.part_title);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ExistingPartsActivity.ViewHolder) view.getTag();
            }
            viewHolder.number.setText(parts_list.get(i).SerialNumber);
            viewHolder.name.setText(parts_list.get(i).Name);

            return view;
        }
    }
}
