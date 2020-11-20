package com.example.finalapplication

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var mIsShowPass = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()

        val myTxt = findViewById<TextView>(R.id.signUp)
        val myBtn = findViewById<Button>(R.id.signIn)
        val myPass = findViewById<TextView>(R.id.forgotPassword)

        showPass.setOnClickListener{
            mIsShowPass = !mIsShowPass
            showPassword(mIsShowPass)
        }

        myTxt.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
            finish()
        }

        myBtn.setOnClickListener{
            doLogin()
        }

        myPass.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Forgot Password")
            val view= layoutInflater.inflate(R.layout.dialog_forgot_password, null)
            val username = view.findViewById<EditText>(R.id.et_username)
            builder.setView(view)
            builder.setPositiveButton("reset", DialogInterface.OnClickListener{ _,_->
                forgotPass(username)
            })
            builder.setNegativeButton("close", DialogInterface.OnClickListener{ _,_-> })
            builder.show()
        }
        showPassword(mIsShowPass)
    }

    private fun showPassword(isShow: Boolean) {
        if(isShow) {
            editPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            showPass.setImageResource(R.drawable.ic_visibleoff)
        } else {
            editPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            showPass.setImageResource(R.drawable.ic_visible)
        }
        editPassword.setSelection(editPassword.text.toString().length)
    }

    private fun forgotPass(username : EditText){
        if (username.text.toString().isEmpty()){
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(username.text.toString()).matches()) {
            return
        }

        auth.sendPasswordResetEmail(username.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this,"Email sent",Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun doLogin(){
        if (editUser.text.toString().isEmpty()){
            editUser.error = "Please enter email"
            editUser.requestFocus()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(editUser.text.toString()).matches()) {
            editUser.error = "Please enter email"
            editUser.requestFocus()
            return
        }
        if (editPassword.text.toString().isEmpty()) {
            editPassword.error = "Please enter password"
            editPassword.requestFocus()
            return
        }
        auth.signInWithEmailAndPassword(editUser.text.toString(), editPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Toast.makeText(baseContext, "Login failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser : FirebaseUser?){
        if(currentUser != null) {
            if(currentUser.isEmailVerified) {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }else{
                Toast.makeText(
                    baseContext, "Please verify your email",
                    Toast.LENGTH_SHORT
                ) .show()
            }
        }


    }
}