package com.example.attendance_new;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Home_page extends AppCompatActivity {

    Button mark1,view1;
    TextView sub1;


//    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mark1 = findViewById(R.id.mark1);
        sub1 = findViewById(R.id.sub1);
        view1 = findViewById(R.id.view1);

        mark1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home_page.this,Mark_Attendance.class);
                String sub = sub1.toString();
                intent.putExtra("subject",sub);
                startActivity(intent);

            }
        });

        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home_page.this,View_Attendance.class);
                startActivity(intent);
            }
        });
    }
}