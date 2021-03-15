package com.example.employeeexample.ui

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.employeeexample.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_register.*


class RegisterFragment : Fragment() {

private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth= FirebaseAuth.getInstance()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login.setOnClickListener{
            findNavController().navigate(
                    RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
            )
        }

        register_button.setOnClickListener {
            user_email_container.error=null
            user_pswd_container.error=null

            val email=user_email.text.toString()
            val pass=user_pass.text.toString()

            if(validateInput(email,pass)) {
                progress.visibility = View.VISIBLE

                auth.createUserWithEmailAndPassword(email,pass)
                        .addOnCompleteListener(requireActivity()){task->
                            progress.visibility=View.INVISIBLE
                            if(task.isSuccessful){
                                findNavController().navigate(
                                RegisterFragmentDirections.actionRegisterFragmentToEmployeeListFragment()
                                )
                            }else{
                                val toast= Toast.makeText(requireActivity(),
                                        "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG)
                                toast.setGravity(Gravity.CENTER_VERTICAL,0,0)
                                toast.show()
                            }
                        }
            }
        }
    }

    private fun validateInput(email:String,pass:String):Boolean{
        var valid=true

        if(email.isBlank()){
            user_email_container.error=getString(R.string.valid_email)
            valid=false
        }
        if(pass.isBlank()){
            user_pswd_container.error=getString(R.string.valid_pswd)
            valid=false
        }else if(pass.length<8){
            user_pswd_container.error=getString(R.string.valid_pswd_length)
            valid=false
        }
        return valid
    }


}