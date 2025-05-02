在学生信息管理系统中，数据库的创建与管理是项目的核心部分。我们通过继承 SQLiteOpenHelper 创建了一个名为 StudentDatabaseHelper 的数据库帮助类，在 onCreate() 方法中定义了 students 表的结构，包含 id（主键自增）、name 和 age 三个字段。添加学生数据时，使用 ContentValues 封装键值对，将学生的姓名和年龄传入数据库，通过 insert() 方法插入记录。在查询数据时，使用 db.rawQuery("SELECT * FROM students", null) 获取所有学生信息，结果保存在 Cursor 对象中，通过 while(cursor.moveToNext()) 遍历每一行数据，并从中提取字段值显示到界面上。此外，更新与删除功能分别使用 update() 和 delete() 方法实现，均以学生的 ID 为条件，实现对指定记录的修改与删除。

界面操作逻辑全部集中在 MainActivity.kt 文件中。通过 findViewById() 获取用户输入框和操作按钮引用，再对每个按钮设置 setOnClickListener 监听器。例如，当用户点击“添加学生”按钮时，程序会读取输入框中的姓名与年龄内容，调用 dbHelper.insertStudent(name, age) 方法进行存储，并根据返回结果使用 Toast.makeText() 向用户反馈成功与否。点击“查看所有学生”按钮则调用 getAllStudents() 获取数据库中所有学生数据，拼接成字符串后使用 AlertDialog.Builder 弹出显示，确保数据结果清晰易读。更新与删除操作也采用类似的逻辑，确保每次操作都基于当前输入的 ID，并向用户返回结果提示。

为了处理用户提出的一个关键问题 —— 即当删除所有学生后再添加新学生时，ID 编号仍然延续而不是从 1 开始，我们新增了一个重置 ID 的方法 deleteAllStudentsAndResetId()。该方法先执行 DELETE FROM students 清空所有表内数据，再通过 DELETE FROM sqlite_sequence WHERE name='students' 删除系统对该表的自增 ID 记录，使下次插入记录从 ID 1 重新开始。在界面中，我们为该功能专门设置了一个按钮“Clear All & Reset ID”，通过点击调用此方法，结合 Toast 提示用户是否清空成功。此设计不仅解决了 ID 不重置的问题，还提供了一个高效便捷的方式批量清理数据，是管理 SQLite 数据完整性的一个关键实现。

以下是学生信息系统中三部分关键代码的完整注释版，包括：数据库操作类 StudentDatabaseHelper.kt、主界面逻辑 MainActivity.kt、以及新增的重置 ID 功能方法。

class StudentDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "StudentDB", null, 1) {

    // 创建 students 表
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE students (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "age INTEGER NOT NULL)"
        )
    }

    // 升级数据库时调用（此处简单删除旧表重建）
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS students")
        onCreate(db)
    }

    // 插入学生记录
    fun insertStudent(name: String, age: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("age", age)
        }
        val result = db.insert("students", null, values)
        return result != -1L
    }

    // 获取所有学生记录
    fun getAllStudents(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM students", null)
        val buffer = StringBuffer()

        // 遍历查询结果
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val name = cursor.getString(1)
            val age = cursor.getInt(2)
            buffer.append("ID: $id\nName: $name\nAge: $age\n\n")
        }
        cursor.close()
        return buffer.toString()
    }

    // 更新指定学生信息
    fun updateStudent(id: Int, name: String, age: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("age", age)
        }
        val result = db.update("students", values, "id = ?", arrayOf(id.toString()))
        return result > 0
    }

    // 删除指定学生记录
    fun deleteStudent(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete("students", "id = ?", arrayOf(id.toString()))
        return result > 0
    }

    // 清空所有学生并重置自增 ID（sqlite_sequence 是 SQLite 内部表）
    fun deleteAllStudentsAndResetId(): Boolean {
        val db = writableDatabase
        db.execSQL("DELETE FROM students") // 删除所有学生记录
        db.execSQL("DELETE FROM sqlite_sequence WHERE name='students'") // 重置 ID 自增计数
        return true
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: StudentDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = StudentDatabaseHelper(this)

        // 获取 UI 控件
        val nameInput = findViewById<EditText>(R.id.nameInput)
        val ageInput = findViewById<EditText>(R.id.ageInput)
        val idInput = findViewById<EditText>(R.id.idInput)

        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnView = findViewById<Button>(R.id.btnView)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val btnClearAll = findViewById<Button>(R.id.btnClearAll)

        // 添加学生
        btnAdd.setOnClickListener {
            val name = nameInput.text.toString()
            val age = ageInput.text.toString().toIntOrNull()
            if (name.isNotEmpty() && age != null) {
                val success = dbHelper.insertStudent(name, age)
                showToast(if (success) "Student added" else "Insert failed")
            } else {
                showToast("Please enter valid name and age")
            }
        }

        // 显示全部学生
        btnView.setOnClickListener {
            val data = dbHelper.getAllStudents()
            AlertDialog.Builder(this)
                .setTitle("All Students")
                .setMessage(if (data.isNotEmpty()) data else "No data found")
                .setPositiveButton("OK", null)
                .show()
        }

        // 更新学生信息
        btnUpdate.setOnClickListener {
            val id = idInput.text.toString().toIntOrNull()
            val name = nameInput.text.toString()
            val age = ageInput.text.toString().toIntOrNull()
            if (id != null && name.isNotEmpty() && age != null) {
                val success = dbHelper.updateStudent(id, name, age)
                showToast(if (success) "Student updated" else "Update failed")
            } else {
                showToast("Please enter valid ID, name and age")
            }
        }

        // 删除学生信息
        btnDelete.setOnClickListener {
            val id = idInput.text.toString().toIntOrNull()
            if (id != null) {
                val success = dbHelper.deleteStudent(id)
                showToast(if (success) "Student deleted" else "Delete failed")
            } else {
                showToast("Please enter valid ID")
            }
        }

        // 清空数据并重置 ID
        btnClearAll.setOnClickListener {
            val success = dbHelper.deleteAllStudentsAndResetId()
            showToast(if (success) "All students deleted and ID reset" else "Reset failed")
        }
    }

    // 快捷 Toast 提示方法
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}


