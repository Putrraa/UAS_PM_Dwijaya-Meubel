package prasetya.daffa.proyek_uas.helper

import android.content.Context

class SessionManager(context: Context) {

    val pref = context.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
    val editor = pref.edit()

    fun saveUser(id: Int, name: String, email: String, role: String) {
        editor.putBoolean("is_login", true)
        editor.putInt("user_id", id)
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("role", role)
        editor.apply()
    }

    fun isLogin(): Boolean {
        return pref.getBoolean("is_login", false)
    }

    fun getUserId(): Int {
        return pref.getInt("user_id", 0)
    }

    fun getName(): String {
        return pref.getString("name", "") ?: ""
    }

    fun getEmail(): String {
        return pref.getString("email", "") ?: ""
    }

    fun getRole(): String {
        return pref.getString("role", "") ?: ""
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }
}