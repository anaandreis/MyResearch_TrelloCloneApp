package com.anaandreis.minhapesquisa_trellocloneapp.signUp.presentation

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.anaandreis.minhapesquisa_trellocloneapp.R
import com.anaandreis.minhapesquisa_trellocloneapp.databinding.FragmentSignUpBinding
import com.anaandreis.minhapesquisa_trellocloneapp.login.presentation.LoginFragment
import com.anaandreis.minhapesquisa_trellocloneapp.signUp.data.models.User
import com.anaandreis.minhapesquisa_trellocloneapp.projectsHome.presentation.ProjectActivity
import com.anaandreis.minhapesquisa_trellocloneapp.utils.DialogFunctions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SignUpFragment : Fragment() {

    lateinit var binding: FragmentSignUpBinding

    private val dialogFunctions = DialogFunctions()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
        R.layout.fragment_sign_up, container, false)

        binding.backButtoncadastro.setOnClickListener{ view: View ->
            view.findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)
        }

        binding.criarContaButton.setOnClickListener { signUpUser() }


        return binding.root

    }

  //  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
  //      super.onViewCreated(view, savedInstanceState)


 //       binding.apply {
   //         // Specify the fragment as the lifecycle owner

   //         lifecycleOwner = viewLifecycleOwner

   //         // Assign the view model to a property in the binding class
    //       viewModel = signUpViewModel

   //         // Assign the fragment
   //         signUpFragment = this@SignUpFragment
    //    }
 //   }


    private fun signUpUser() {
        val name = binding.editTextName.text.toString().trim { it <= ' ' }
        val email = binding.editTextTextEmailAddress.text.toString().trim { it <= ' ' }
        val password = binding.password.text.toString().trim { it <= ' ' }
        val passwordConfirmation = binding.passwordConfirme.text.toString().trim { it <= ' ' }
        Log.d("SIGN UP", "variables.")

        if(validateForm(name, email, password, passwordConfirmation)
            && password == passwordConfirmation)
        { Log.d("SIGN UP", "User signed up successfully.")
            dialogFunctions.showProgressDialog(requireContext(),"Aguarde um momento")
            FirebaseAuth.getInstance().
            createUserWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
                    dialogFunctions.hideProgressDialog(requireContext())
                    if(task.isSuccessful) {
                        Toast.makeText(requireContext(), "Usuário registrado com sucesso.", Toast.LENGTH_SHORT).show()
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!

                        // Create a new user object as a HashMap
                        val user = hashMapOf(
                            "id" to firebaseUser.uid,
                            "name" to name,
                            "email" to registeredEmail,
                            "projects" to mutableListOf<String?>()
                        )
                        lifecycleScope.launch {
                        // Get a reference to the users collection in Firestore
                        val db = FirebaseFirestore.getInstance()
                        val usersRef = db.collection("users")

                        // Use the user's uid as the document ID in Firestore
                        usersRef.document(firebaseUser.uid)
                            .set(user)
                            .addOnSuccessListener {
                                // Data successfully written to Firestore
                            }
                            .addOnFailureListener { e ->
                                Log.w("SIGN UP", "Error writing document", e)
                            }
                        }

                        view?.findNavController()?.navigate(R.id.action_signUpFragment_to_loginFragment)

                    } else {
                        Log.e("SIGN UP", "Error signing up user.", task.exception)
                        dialogFunctions.showProgressDialog(requireContext(),"Falha no cadastro.")
                    }
                }

        }
    }


        fun validateForm(name: String, email: String, password: String, passwordConfirmation: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                dialogFunctions.showErrorSnackBar(binding.root, "Digite seu nome")
                false
            }
            TextUtils.isEmpty(email) -> {
                dialogFunctions.showErrorSnackBar(binding.root, "Digite seu email")
                false
            }
            TextUtils.isEmpty(password) -> {
                dialogFunctions.showErrorSnackBar(binding.root, "Digite sua senha")
                false
            }
            TextUtils.isEmpty(passwordConfirmation) -> {
                dialogFunctions.showErrorSnackBar(binding.root, "Confirme sua senha")
                false
            }
            password != passwordConfirmation -> {
                dialogFunctions.showErrorSnackBar(binding.root, "Digite senhas iguais.")
                false
            }
            else -> {
                return true
            }
        }
    }
}