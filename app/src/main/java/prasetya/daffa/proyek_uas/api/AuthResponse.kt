package prasetya.daffa.proyek_uas.api

data class AuthResponse(
    val status: Boolean,
    val message: String,
    val user: ApiUser?
)

data class ApiUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)