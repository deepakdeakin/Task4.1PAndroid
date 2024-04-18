package com.example.crudoperations;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;

import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    EditText titleEditText, descriptionEditText, dueDateEditText;
    Button dueDateButton;
    DatabaseHelper myDB;
    Calendar calendar;
    int year, month, day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dueDateEditText = findViewById(R.id.dueDateEditText);
        myDB = new DatabaseHelper(this);

        // Set OnClickListener for dueDateEditText
        dueDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }


    public void addTask(View view) {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String dueDate = dueDateEditText.getText().toString();

        if (title.equals("") || description.equals("") || dueDate.equals("")) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            boolean isInserted = myDB.insertData(title, description, dueDate);
            if (isInserted) {
                Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show();
                titleEditText.setText("");
                descriptionEditText.setText("");
                dueDateEditText.setText("");
            } else {
                Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to show DatePickerDialog
    private void showDatePickerDialog() {
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                        dueDateEditText.setText(date);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    public void viewTasks(View view) {
        Intent intent = new Intent(this, ViewTasksActivity.class);
        startActivity(intent);
    }
}