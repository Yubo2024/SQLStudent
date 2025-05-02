package com.example.sqlstudent
import android.app.AlertDialog
import android.database.Cursor
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: StudentDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = StudentDatabaseHelper(this)
        val nameInput = findViewById<EditText>(R.id.nameInput)
        val ageInput = findViewById<EditText>(R.id.ageInput)
        val idInput = findViewById<EditText>(R.id.idInput)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnView = findViewById<Button>(R.id.btnView)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val btnClearAll = findViewById<Button>(R.id.btnClearAll)
        btnAdd.setOnClickListener {
            val name = nameInput.text.toString()
            val age = ageInput.text.toString().toIntOrNull()

            if (name.isEmpty() || age == null) {
                Toast.makeText(this, "Please enter valid name and age", Toast.LENGTH_SHORT).show()
            } else {
                val success = dbHelper.insertStudent(name, age)
                Toast.makeText(this, if (success) "Student added" else "Failed to add", Toast.LENGTH_SHORT).show()
                nameInput.text.clear()
                ageInput.text.clear()
            }
        }
        btnView.setOnClickListener {
            val cursor: Cursor = dbHelper.getAllStudents()
            if (cursor.count == 0) {
                showMessage("No Data", "No student records found.")
                return@setOnClickListener
            }
            val buffer = StringBuffer()
            while (cursor.moveToNext()) {
                buffer.append("ID: ${cursor.getInt(0)}\n")
                buffer.append("Name: ${cursor.getString(1)}\n")
                buffer.append("Age: ${cursor.getInt(2)}\n\n")
            }
            showMessage("All Students", buffer.toString())
        }
        btnUpdate.setOnClickListener {
            val id = idInput.text.toString().toIntOrNull()
            val name = nameInput.text.toString()
            val age = ageInput.text.toString().toIntOrNull()

            if (id == null || name.isEmpty() || age == null) {
                Toast.makeText(this, "Please enter ID, name, and age", Toast.LENGTH_SHORT).show()
            } else {
                val success = dbHelper.updateStudent(id, name, age)
                Toast.makeText(this, if (success) "Student updated" else "Update failed", Toast.LENGTH_SHORT).show()
            }
        }
        btnDelete.setOnClickListener {
            val id = idInput.text.toString().toIntOrNull()

            if (id == null) {
                Toast.makeText(this, "Please enter valid ID", Toast.LENGTH_SHORT).show()
            } else {
                val success = dbHelper.deleteStudent(id)
                Toast.makeText(this, if (success) "Student deleted" else "Delete failed", Toast.LENGTH_SHORT).show()
                idInput.text.clear()
            }
        }

        btnClearAll.setOnClickListener {
            val success = dbHelper.deleteAllStudentsAndResetId()
            Toast.makeText(this, if (success) "All students deleted, ID reset" else "Failed to clear data", Toast.LENGTH_SHORT).show()
        }

    }
    private fun showMessage(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .show()
    }
}
