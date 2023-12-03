package com.example.attendance_new;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Mark_Attendance extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 100;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    EditText etSectionName;
    Button markAttendance, fetch;
    ListView listview;
    List<String> deviceList = new ArrayList<>();;
    private ArrayList<String> studentNames = new ArrayList<>();
    private DatabaseReference studentsRef;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();
    ArrayAdapter<String> adapter;
    String div;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(Mark_Attendance.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                // Process the discovered device (add it to a list, display it, etc.)
                Log.d("Bluetooth", "Found device: " + deviceName + " (" + deviceAddress + ")");

                if (isValidDeviceName(deviceName) && !deviceList.contains(deviceName)) {
                    String customName = deviceName;
                    deviceList.add(customName);
                    adapter.notifyDataSetChanged();
                }

            }
        }
    };
    // Register the receiver in your activity or fragment


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);
        etSectionName = findViewById(R.id.etSectionName);
        listview = findViewById(R.id.listView1);
        fetch = findViewById(R.id.btnAddStudent);
        markAttendance = findViewById(R.id.markAttend);
        Intent intent = getIntent();


        adapter = new ArrayAdapter<>(Mark_Attendance.this, R.layout.list_item,R.id.textViewList ,deviceList);
        listview.setAdapter(adapter);
        String subject;
        if (intent != null) {
            subject = intent.getStringExtra("subject");
        }

        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter == null) {
                    // Device doesn't support Bluetooth
                } else {
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                }

                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    // Start discovery
                    if (ActivityCompat.checkSelfPermission(Mark_Attendance.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    }

                    if (ContextCompat.checkSelfPermission(Mark_Attendance.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Request the permission
                        ActivityCompat.requestPermissions(Mark_Attendance.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST_CODE);
                    } else {
                        // The app already has the permission, proceed with Bluetooth operations
                        bluetoothAdapter.startDiscovery();

                    }



                } else {
                    // Bluetooth is not available or not enabled
                    Log.d("error", "Bluthoot not available!");
                }


            }
        });
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        markAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                studentsRef = database.getReference("Student");

                // Retrieve and traverse student names
                retrieveStudentNames();
                String uid = currentUser.getUid();
                DatabaseReference curr_db = database.getReference(uid);
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1; // Month is zero-based
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Format the date as needed
                div = etSectionName.getText().toString();
                String currentDate = year + "-" + month + "-" + day;
                for(int i = 0 ; i<studentNames.size() ; i++){
                        if(deviceList.contains(studentNames.get(i))){
                            curr_db.child(div).child(currentDate).child(studentNames.get(i)).setValue("Present");
                        }else {
                            curr_db.child(div).child(currentDate).child(studentNames.get(i)).setValue("Absent");
                        }
                }

            }
        });
    }

    private boolean isValidDeviceName(String deviceName) {
        // Implement your logic to check if the device name follows the convention "Div_Roll no."
        // For example, you can use regular expressions for this check
        String div = etSectionName.getText().toString();
        return deviceName != null && deviceName.matches(div+"+_\\d+");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void retrieveStudentNames() {
        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentNames.clear(); // Clear the list to avoid duplicates on updates

                // Iterate through each student
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve the student name
                    String studentName = studentSnapshot.child("name").getValue(String.class);

                    // Add the name to the ArrayList
                    studentNames.add(studentName);

                    // Log the name for demonstration purposes
                    Log.d("StudentName", studentName);
                }

                // Now, studentNames ArrayList contains the names of all students
                // You can use this ArrayList as needed in your application
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Log.w("Firebase", "Failed to read value.", error.toException());
            }
        });
    }

}