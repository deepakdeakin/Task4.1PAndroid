package com.example.crudoperations;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class EditTaskActivity extends AppCompatActivity {
    EditText titleEditText, descriptionEditText, dueDateEditText;
    DatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dueDateEditText = findViewById(R.id.dueDateEditText);
        myDB = new DatabaseHelper(this);

        // Retrieve task details from intent extras
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("title")) {
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            String dueDate = intent.getStringExtra("dueDate");

            // Ensure description is displayed correctly
            description = description.replace("\n", ""); // Remove newline characters

            // Populate the input fields with task details
            titleEditText.setText(title);
            descriptionEditText.setText(description);
            dueDateEditText.setText(dueDate);
        } else {
            // Clear the input fields
            titleEditText.setText("");
            descriptionEditText.setText("");
            dueDateEditText.setText("");

            // Set the due date text view text
            TextView dueDateTextView = findViewById(R.id.dueDateTextView);
            dueDateTextView.setText("Select Due Date");
        }
    }


    public void updateTask(View view) {
        // Get updated task details from input fields
        String newTitle = titleEditText.getText().toString();
        String newDescription = descriptionEditText.getText().toString();
        String newDueDate = dueDateEditText.getText().toString();

        // Update task in the database
        boolean isUpdated = myDB.updateData(newTitle, newDescription, newDueDate);
        if (isUpdated) {
            Toast.makeText(this, "Task Updated", Toast.LENGTH_SHORT).show();
            // Redirect to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Finish current activity to prevent going back
        } else {
            Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
        }
    }

    public void showDatePickerDialog(View view) {
        // Get current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(EditTaskActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Set selected date to the due date edit text
                        String formattedDate = (month + 1) + "/" + dayOfMonth + "/" + year;
                        dueDateEditText.setText(formattedDate);
                    }
                }, year, month, dayOfMonth);

        // Show date picker dialog
        datePickerDialog.show();
    }

    public void viewTask(View view) {
        Intent intent = new Intent(this, ViewTasksActivity.class);
        startActivity(intent);
        finish(); // Finish current activity to prevent going back
    }

    public void backToMain(View view) {
        // Implement the logic to go back to the main screen here
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish current activity to prevent going back
    }
}

