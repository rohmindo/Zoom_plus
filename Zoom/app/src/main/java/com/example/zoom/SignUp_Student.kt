package com.example.zoom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.zoom.httpconnection.HttpConnection
import com.example.zoom.httpconnection.HttpConnection_SignUp_Student
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up__pro.*
import kotlinx.android.synthetic.main.activity_sign_up__student.*
import okhttp3.*
import java.io.IOException
import java.net.URL

class SignUp_Student : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up__student)

    //구글정보받아오기
        var mAuth= FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            //'R.string.default_web_client_id' 에는 본인의 클라이언트 아이디를 넣어주시면 됩니다.
            .requestEmail()
            .build()
        val user=mAuth.currentUser


        btnstu_signup.setOnClickListener {
            var pro_name=sname.text.toString()
            var pro_school=sschool.text.toString()
            var pro_stid=sstid.text.toString()
            var pro_major=smajor.text.toString()
            val intent= Intent(this,MainActivity::class.java)
            var t= HttpConnection_SignUp_Student(getString(R.string.app_domain),"student",pro_name,user.email,user.photoUrl.toString(),pro_school,pro_major,"11",pro_stid);

            t.result()
            var tee=t.temp;
            Log.d("rerere",tee)
            startActivity(intent)
            finish()
        }
    }
}