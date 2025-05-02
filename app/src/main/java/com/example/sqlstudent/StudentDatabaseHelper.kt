package com.example.sqlstudent

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StudentDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Student.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "students"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_AGE = "age"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_AGE INTEGER
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertStudent(name: String, age: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_AGE, age)
        }
        val result = db.insert(TABLE_NAME, null, values)
        return result != -1L
    }

    fun getAllStudents(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun updateStudent(id: Int, name: String, age: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_AGE, age)
        }
        val result = db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        return result > 0
    }

    fun deleteStudent(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        return result > 0
    }
    fun deleteAllStudentsAndResetId(): Boolean {
        val db = writableDatabase
        return try {
            db.execSQL("DELETE FROM $TABLE_NAME")
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='$TABLE_NAME'")
            true
        } catch (e: Exception) {
            false
        }
    }

}
