package prasetya.daffa.proyek_uas

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBOpenHelper(context: Context) :
    SQLiteOpenHelper(context, "UserDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT," +
                    "password TEXT," +
                    "role TEXT)"
        )

        // data dummy
        insertUser(db, "admin@gmail.com", "12345", "admin")
        insertUser(db, "user@gmail.com", "12345", "user")
    }

    private fun insertUser(db: SQLiteDatabase, email: String, pass: String, role: String) {
        val cv = ContentValues()
        cv.put("email", email)
        cv.put("password", pass)
        cv.put("role", role)
        db.insert("users", null, cv)
    }

    fun checkLogin(email: String, pass: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT role FROM users WHERE email=? AND password=?",
            arrayOf(email, pass)
        )

        return if (cursor.moveToFirst()) {
            val role = cursor.getString(0)
            cursor.close()
            role
        } else {
            cursor.close()
            null
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}