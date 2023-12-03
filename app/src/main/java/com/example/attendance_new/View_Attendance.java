package com.example.attendance_new;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class View_Attendance extends AppCompatActivity {

    Spinner date,division;
    Button fetch;
    ListView listview;
    ArrayAdapter<String> adapter;
    List<String> deviceList = new ArrayList<>();

    DatabaseReference databaseReference;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        date = findViewById(R.id.SpiDate);
        division = findViewById(R.id.SpiDiv);
        date.setPrompt("Select Date");
        division.setPrompt("Select Division");
        fetch = findViewById(R.id.btnFetch1);
        listview = findViewById(R.id.lvView);
        String uid = currentUser.getUid();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> classDivisions = new ArrayList<>();


                for (DataSnapshot divisionSnapshot : dataSnapshot.getChildren()) {
                    String division = divisionSnapshot.getKey();
                    classDivisions.add(division);


                }

                // Populate the Spinner with class divisions
                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                        View_Attendance.this,
                        R.layout.spinner_item,
                        R.id.tvDiv,
                        classDivisions
                );

                adapter1.setDropDownViewResource(R.layout.spinner_item);
                division.setAdapter(adapter1);
                adapter1.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });


        final String[] selected_div = new String[1];
        final String[] selected_date = new String[1];


        division.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle the selected item here
                String selectedClassDivision = (String) parentView.getItemAtPosition(position);
                selected_div[0] = selectedClassDivision;
                databaseReference = firebaseDatabase.getReference(uid).child(selectedClassDivision);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> classDates = new ArrayList<>();
                        for(DataSnapshot dates: snapshot.getChildren()){
                            String d = dates.getKey();
                            classDates.add(d);
                        }

                        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                                View_Attendance.this,
                                R.layout.spinner_date,
                                R.id.tvspDate,
                                classDates
                        );

                        adapter1.setDropDownViewResource(R.layout.spinner_date);
                        date.setAdapter(adapter1);
                        adapter1.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });

        date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDate = (String) parent.getItemAtPosition(position);
                selected_date[0] = selectedDate;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        Log.d("new",selected_date[0]+" "+selected_div[0]);

        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("new",selected_date[0]+" "+selected_div[0]);
                databaseReference = firebaseDatabase.getReference(uid).child(selected_div[0]).child(selected_date[0]);

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot s : snapshot.getChildren()){
                            String key = s.getKey();
                            String names = s.getValue(String.class);
                            deviceList.add(key+" - "+ names);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                adapter = new ArrayAdapter<>(View_Attendance.this, R.layout.list_view_attendence_item,R.id.tvAteen ,deviceList);
                listview.setAdapter(adapter);
            }
        });


    }

    private void populateClassDivisions() {

    }
}