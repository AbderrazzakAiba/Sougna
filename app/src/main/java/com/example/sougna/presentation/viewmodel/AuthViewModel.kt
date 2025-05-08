package com.example.sougna.presentation.viewmodel

package com.example.sougna.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.EmailAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        // Observe changes in the current user
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
    }

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
    }

    fun toggleAuthMode() {
        _isLoginMode.value = !_isLoginMode.value
    }

    fun authenticateUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentEmail = email.value.trim()
        val currentPassword = password.value

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            onError("يرجى إدخال البريد الإلكتروني وكلمة المرور")
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            if (_isLoginMode.value) {
                // تسجيل الدخول
                firebaseAuth.signInWithEmailAndPassword(currentEmail, currentPassword)
                    .addOnCompleteListener { task ->
                        _isLoading.value = false
                        if (task.isSuccessful) {
                            onSuccess()
                        } else {
                            onError("فشل تسجيل الدخول: ${task.exception?.message}")
                        }
                    }
            } else {
                // إنشاء حساب
                firebaseAuth.createUserWithEmailAndPassword(currentEmail, currentPassword)
                    .addOnCompleteListener { task ->
                        _isLoading.value = false
                        if (task.isSuccessful) {
                            onSuccess()
                        } else {
                            onError("فشل إنشاء الحساب: ${task.exception?.message}")
                        }
                    }
            }
        }
    }

    fun logout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()
                onSuccess()
            } catch (e: Exception) {
                onError("فشل تسجيل الخروج: ${e.message}")
            }
        }
    }

    fun changePassword(newPassword: String, currentPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = firebaseAuth.currentUser
        if (user != null && user.email != null) {
            _isLoading.value = true
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                _isLoading.value = false
                                if (updateTask.isSuccessful) {
                                    onSuccess()
                                } else {
                                    onError("فشل تحديث كلمة المرور: ${updateTask.exception?.message}")
                                }
                            }
                    } else {
                        _isLoading.value = false
                        onError("فشل إعادة المصادقة: ${reauthTask.exception?.message}")
                    }
                }
        } else {
            onError("المستخدم غير مسجل الدخول أو البريد الإلكتروني غير متاح")
        }
    }

    fun changeEmail(newEmail: String, currentPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = firebaseAuth.currentUser
        if (user != null && user.email != null) {
            _isLoading.value = true
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.updateEmail(newEmail)
                            .addOnCompleteListener { updateTask ->
                                _isLoading.value = false
                                if (updateTask.isSuccessful) {
                                    onSuccess()
                                } else {
                                    onError("فشل تحديث البريد الإلكتروني: ${updateTask.exception?.message}")
                                }
                            }
                    } else {
                        _isLoading.value = false
                        onError("فشل إعادة المصادقة: ${reauthTask.exception?.message}")
                    }
                }
        } else {
            onError("المستخدم غير مسجل الدخول أو البريد الإلكتروني غير متاح")
        }
    }
}
