package com.example.crudoperations;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewTasksActivity extends AppCompatActivity {
    ListView listView;
    DatabaseHelper myDB;
    ArrayList<String> taskList;
    ArrayList<Boolean> taskSelections; // Keep track of selected tasks
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tasks);
        listView = findViewById(R.id.listView);
        myDB = new DatabaseHelper(this);
        taskSelections = new ArrayList<>();

        // Display tasks
        displayTasks();

        // Initialize task selections
        for (int i = 0; i < taskList.size(); i++) {
            taskSelections.add(false);
        }

        // Set up the adapter
        adapter = new ArrayAdapter<String>(this, R.layout.list_item_task, R.id.taskTextView, taskList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                CheckBox checkBox = view.findViewById(R.id.taskCheckBox);
                checkBox.setChecked(taskSelections.get(position));

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        taskSelections.set(position, isChecked);
                    }
                });

                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Listen for item click events
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Toggle the selection state of the clicked task
                CheckBox checkBox = view.findViewById(R.id.taskCheckBox);
                checkBox.setChecked(!checkBox.isChecked());
                taskSelections.set(position, checkBox.isChecked());
            }
        });
    }

    private void displayTasks() {
        Cursor cursor = myDB.getAllData();
        if (cursor.getCount() == 0) {
            // No tasks found, display a toast message
            Toast.makeText(this, "No tasks found", Toast.LENGTH_SHORT).show();
            // Redirect to Main screen
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Finish the current activity to prevent the user from going back to it
        } else {
            taskList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String task = cursor.getString(1) + ": " + cursor.getString(2) + " (Due: " + cursor.getString(3) + ")";
                taskList.add(task);
            }

            // Sort tasks by due date
            Collections.sort(taskList, new Comparator<String>() {
                @Override
                public int compare(String task1, String task2) {
                    String dueDate1 = task1.substring(task1.lastIndexOf("Due:") + 5).trim();
                    String dueDate2 = task2.substring(task2.lastIndexOf("Due:") + 5).trim();
                    return dueDate1.compareTo(dueDate2);
                }
            });

            // Set up the adapter and populate the ListView
            adapter = new ArrayAdapter<>(this, R.layout.list_item_task, R.id.taskTextView, taskList);
            listView.setAdapter(adapter);
        }
    }

    public void deleteSelectedTasks(View view) {
        // Check if any task is selected
        boolean isAnyTaskSelected = false;
        for (boolean isSelected : taskSelections) {
            if (isSelected) {
                isAnyTaskSelected = true;
                break;
            }
        }

        // If no task is selected, display a toast message and return
        if (!isAnyTaskSelected) {
            Toast.makeText(this, "Please select a task to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        // Delete selected tasks
        ArrayList<Boolean> selectedTasksCopy = new ArrayList<>(taskSelections);
        for (int i = 0; i < selectedTasksCopy.size(); i++) {
            if (selectedTasksCopy.get(i)) {
                String[] data = taskList.get(i).split(":");
                String title = data[0].trim();

                int deletedRows = myDB.deleteData(title);
                if (deletedRows > 0) {
                    taskList.remove(i);
                    taskSelections.remove(i);
                    i--;
                }
            }
        }

        // Update the ListView adapter after removing tasks
        adapter.notifyDataSetChanged();
    }

    public void editTask(View view) {
        // Get the position of the selected task
        int selectedPosition = -1;
        for (int i = 0; i < taskSelections.size(); i++) {
            if (taskSelections.get(i)) {
                selectedPosition = i;
                break;
            }
        }

        // If no task is selected, show a toast message and return
        if (selectedPosition == -1) {
            Toast.makeText(this, "Please select a task to edit", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extract the task title from the selected task
        String selectedTask = taskList.get(selectedPosition);
        String[] taskDetails = selectedTask.split(":");
        String title = taskDetails[0].trim();

        // Retrieve task details from the database based on the title
        Cursor cursor = myDB.getAllData();
        if (cursor.moveToFirst()) {
            do {
                int titleIndex = cursor.getColumnIndex(DatabaseHelper.COL_2);
                if (titleIndex != -1) {
                    String dbTitle = cursor.getString(titleIndex);
                    if (dbTitle.trim().equalsIgnoreCase(title)) { // Check for case-insensitive match
                        // Extract task details from the cursor
                        int descriptionIndex = cursor.getColumnIndex(DatabaseHelper.COL_3);
                        int dueDateIndex = cursor.getColumnIndex(DatabaseHelper.COL_4);

                        String description = (descriptionIndex != -1) ? cursor.getString(descriptionIndex) : "";
                        String dueDate = (dueDateIndex != -1) ? cursor.getString(dueDateIndex) : "";

                        // Open the EditTaskActivity and pass the task details as extras
                        Intent intent = new Intent(this, EditTaskActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("description", description);
                        intent.putExtra("dueDate", dueDate);
                        startActivity(intent);
                        cursor.close(); // Close the cursor after use
                        return; // Exit the loop once the task is found
                    }
                }
            } while (cursor.moveToNext());
        }

        // If the task is not found in the database, display a message
        Toast.makeText(this, "Task not found in database", Toast.LENGTH_SHORT).show();
    }


    private String extractDueDate(String task) {
        String pattern = "\\(Due:(.*?)\\)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(task);
        if (m.find()) {
            return m.group(1).trim();
        } else {
            return ""; // Default value if due date is not found
        }
    }

    // Method to navigate back to the main screen
    public void backToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}
