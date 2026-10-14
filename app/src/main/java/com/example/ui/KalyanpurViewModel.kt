package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import androidx.credentials.CustomCredential
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class KalyanpurViewModel : ViewModel() {

    private lateinit var repository: KalyanpurRepository
    val auth = Firebase.auth

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _isHindi = MutableStateFlow(true)
    val isHindi: StateFlow<Boolean> = _isHindi

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    private val _donors = MutableStateFlow<List<Donor>>(emptyList())
    val donators: StateFlow<List<Donor>> = _donors

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices

    private val _registeredDonors = MutableStateFlow<List<RegisteredDonor>>(emptyList())
    val registeredDonors: StateFlow<List<RegisteredDonor>> = _registeredDonors

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun init(context: Context) {
        repository = KalyanpurRepository(context)
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }

        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }

        viewModelScope.launch {
            repository.observeProjects().collect { _projects.value = it }
        }
        viewModelScope.launch {
            repository.observeDonors().collect { _donors.value = it }
        }
        viewModelScope.launch {
            repository.observeExpenses().collect { _expenses.value = it }
        }
        viewModelScope.launch {
            repository.observeNotices().collect { _notices.value = it }
        }
        viewModelScope.launch {
            repository.observeRegisteredDonors().collect { _registeredDonors.value = it }
        }
    }

    fun toggleLanguage() {
        _isHindi.value = !_isHindi.value
    }

    fun addProject(project: Project, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addProject(project)
            onComplete()
        }
    }

    fun updateProject(project: Project, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateProject(project, onComplete)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
        }
    }

    fun addDonor(donor: Donor, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addDonor(donor)
            onComplete()
        }
    }

    fun deleteDonor(donorId: String, projectId: String, amount: Long) {
        viewModelScope.launch {
            repository.deleteDonor(donorId, projectId, amount)
        }
    }

    fun addExpense(expense: Expense, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addExpense(expense)
            onComplete()
        }
    }

    fun deleteExpense(expenseId: String, projectId: String, amount: Long) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId, projectId, amount)
        }
    }

    fun addNotice(notice: Notice, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addNotice(notice)
            onComplete()
        }
    }

    fun deleteNotice(noticeId: String) {
        viewModelScope.launch {
            repository.deleteNotice(noticeId)
        }
    }

    fun addRegisteredDonor(donor: RegisteredDonor, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addRegisteredDonor(donor, onComplete)
        }
    }

    fun deleteRegisteredDonor(donorId: String) {
        viewModelScope.launch {
            repository.deleteRegisteredDonor(donorId)
        }
    }

    fun updateRegisteredDonor(donor: RegisteredDonor, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateRegisteredDonor(donor, onComplete)
        }
    }

    fun signInWithGoogle(context: Context, activity: Activity) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            _errorMessage.value = "Google Sign-In configuration missing"
            return
        }

        val credentialManager = CredentialManager.create(context)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    _currentUser.value = auth.currentUser
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Sign-in cancelled")
            } catch (e: Exception) {
                Log.e("Auth", "Sign-in failed", e)
                _errorMessage.value = e.localizedMessage ?: "Sign-in failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut(context: Context) {
        auth.signOut()
        _currentUser.value = null
        try {
            val credentialManager = CredentialManager.create(context)
            viewModelScope.launch {
                credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
            }
        } catch (e: Exception) {
            Log.e("Auth", "Clear credential failed", e)
        }
    }
}
