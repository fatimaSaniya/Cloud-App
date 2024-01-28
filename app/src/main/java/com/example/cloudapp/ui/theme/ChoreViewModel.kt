package com.example.cloudapp.ui.theme

import androidx.lifecycle.ViewModel
import com.example.cloudapp.ChoreEvent
import com.example.cloudapp.data.Chores
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class UploadStatus {
    SUCCESS,
    FAILURE,
    IN_PROGRESS,
    IDLE,
}

enum class DownloadStatus {
    SUCCESS,
    FAILURE,
    IN_PROGRESS,
    IDLE,
}

data class UiState(
    val chore: Chores = Chores(),
    val name: String = "",
    var errorMessage: String = "",
    var uploadStatus: UploadStatus = UploadStatus.IDLE,
    var downloadStatus: DownloadStatus = DownloadStatus.IDLE,
    val choresList: List<Chores> = emptyList(),
)

class ChoreViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState())
    val uiState: StateFlow<UiState> get() = _uiState.asStateFlow()
    var db = Firebase.firestore

    init {
        loadData()
    }

    private fun saveData() {
        _uiState.value.uploadStatus = UploadStatus.IN_PROGRESS
        if (_uiState.value.name.length < 4) {
            _uiState.value.errorMessage = "Chore Name should be 4 or more char"
            _uiState.value.uploadStatus = UploadStatus.FAILURE
            return
        }
        _uiState.update { it.copy(chore = Chores(name = _uiState.value.name)) }
        db.collection("chores_list").add(_uiState.value.chore).addOnSuccessListener {
            _uiState.value.uploadStatus = UploadStatus.SUCCESS
            _uiState.value.errorMessage = ""
            _uiState.update { it.copy(name = "", chore = Chores()) } //reset to blank
            loadData()
        }.addOnFailureListener {
            _uiState.value.uploadStatus = UploadStatus.FAILURE
            _uiState.value.errorMessage = it.message ?: "Could not save data!"
        }
    }

    private fun loadData() {
        _uiState.value.downloadStatus = DownloadStatus.IN_PROGRESS
        db.collection("chores_list").get().addOnSuccessListener { snapshot ->
            if (snapshot.documents.size > 0) {
                _uiState.update {
                    it.copy(
                        choresList = snapshot.toObjects(Chores::class.java),
                        downloadStatus = DownloadStatus.SUCCESS,
                        errorMessage = ""
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        choresList = emptyList(),
                        downloadStatus = DownloadStatus.SUCCESS,
                        errorMessage = ""
                    )
                }
            }
        }.addOnFailureListener {
            _uiState.value.downloadStatus = DownloadStatus.FAILURE
            _uiState.value.errorMessage = it.message ?: "Some error occurred!"
        }
    }

    private fun deleteItem(chore: Chores) {
//        db.collection("chores_list").whereEqualTo("name", chore.name).get().addOnSuccessListener {
//            if (it.documents.size > 0){
//                val id = it.documents[0].id
//                db.collection("chores_list").document(id).delete().addOnSuccessListener {
//                    loadData()
//                }
//            }
//        }
        db.collection("chores_list").whereEqualTo("name", chore.name).get()
            .addOnCompleteListener { query ->
                if (query.result.documents.size > 0) {
                    val id = query.result.documents[0].id
                    db.collection("chores_list").document(id).delete().addOnSuccessListener {
                        _uiState.value.downloadStatus = DownloadStatus.SUCCESS
                        loadData()
                    }
                }
            }
    }

    fun onEvent(event: ChoreEvent) {
        when (event) {
            is ChoreEvent.OnItemDelete -> deleteItem(event.chore)
            is ChoreEvent.OnNameEdit -> _uiState.update { it.copy(name = event.name) }
            ChoreEvent.OnRefreshClicked -> loadData()
            ChoreEvent.OnSaveClicked -> saveData()
        }
    }
}