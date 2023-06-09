package com.anaandreis.minhapesquisa_trellocloneapp.newProject


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anaandreis.minhapesquisa_trellocloneapp.FirestoreClass
import com.anaandreis.minhapesquisa_trellocloneapp.home.MainActivity
import com.anaandreis.minhapesquisa_trellocloneapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.Date

    class NewProjectViewModel: ViewModel() {

        var projectName: String = ""
        var projectDescription: String = ""
        var startDate: Date? = null
        var endDate: Date? = null
        var createdBy: String = ""
        var assignedTo: ArrayList<String> = ArrayList()

        var projectsListenerRegistration: ListenerRegistration? = null

        private val _projectsList: MutableLiveData<List<Project>> = MutableLiveData()
        val projectsList: LiveData<List<Project>>
            get() = _projectsList

        private val mFireStore = FirebaseFirestore.getInstance()
        val createBoardResult: MutableLiveData<Boolean> = MutableLiveData()
        private val FirestoreClass = FirestoreClass()



        fun createBoard() {
            assignedTo.clear()
            assignedTo.add(FirestoreClass.getCurrentUserId())


            var projectInfo = Project(
                projectName,
                projectDescription,
                startDate!!,
                endDate!!,
                createdBy,
                assignedTo
            )


            mFireStore.collection(Constants.PROJECTS)
                .document()
                .set(projectInfo, SetOptions.merge())
                .addOnSuccessListener {
                    createBoardResult.postValue(true)
                    Log.d("MapSize", "Size of assignedTo map: ${assignedTo.size}")
                    resetValues()
                }.addOnFailureListener {
                    createBoardResult.postValue(false)
                }


        }

        fun resetValues() {
            projectName = ""
            projectDescription = ""
            startDate = null
            endDate = null
            createdBy = ""
            assignedTo = ArrayList()
        }

        fun addEmailToAssignedList(email: String) {
            assignedTo.add(email)
            Log.d("MapSize", "Size of assignedTo map: ${assignedTo.size}")
        }



        fun getBoardsList() {

            viewModelScope.launch {
                try {
                    projectsSnapshotListener()
                } catch (e: Exception) {
                    Log.e("GETBOARD", "ERROR")  // handle error
                }
            }
        }



        fun projectsSnapshotListener() {
            val collectionRef = mFireStore.collection(Constants.PROJECTS)
                .whereArrayContains(
                    Constants.ASSIGNED_TO,
                    FirestoreClass.getCurrentUserId()
                )
            projectsListenerRegistration = collectionRef.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("SNAPSHOT", "Error listening to projects collection", exception)
                    return@addSnapshotListener
                }

                val projects = mutableListOf<Project>()
                for (doc in snapshot?.documents ?: emptyList()) {
                    val project = doc.toObject(Project::class.java)
                    if (project != null) {
                        projects.add(project)
                    }
                }

                _projectsList.value = projects
            }
        }
    }

