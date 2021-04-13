package com.example.zoom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.zoom.R.string.app_domain
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up__pro.*
import okhttp3.*
import java.io.IOException
import java.net.URL

class SignUp_Pro : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up__pro)

        var mAuth= FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            //'R.string.default_web_client_id' 에는 본인의 클라이언트 아이디를 넣어주시면 됩니다.
            .requestEmail()
            .build()
        val user=mAuth.currentUser
        googleSignInClient = GoogleSignIn.getClient(this, gso)



        btnpro_signup.setOnClickListener {
            var pro_name=name.text.toString()
            var pro_school=school.text.toString()
            var pro_stid=stid.text.toString()
            var pro_major=major.text.toString()
            val intent= Intent(this,MainActivity::class.java)
            //db 저장
            HttpCheckId("professor",pro_name,user.email,user.photoUrl.toString(),pro_school,pro_major,"11",pro_stid)
            startActivity(intent)
            finish()
        }
    }
    //회원가입
    fun HttpCheckId(
        type:String, name:String?,
        email:String?,
        photourl: String?,
        school:String?,
        major:String?,
        grade:String?,
        stid:String?){
        // URL을 만들어 주고
        var temp:String= getString(R.string.app_domain)+"/auth/signup"
        val url = URL(temp)
///auth/signup
        //데이터를 담아 보낼 바디를 만든다
        val requestBody : RequestBody = FormBody.Builder()
            .add("type",type)
            .add("name",name)
            .add("email",email)
            .add("photourl",photourl)
            .add("school",school)
            .add("major",major)
            .add("grade",grade)
            .add("identityID",stid)
            .build()
       // OkHttp Request 를 만들어준다.
        Log.d("test",requestBody.toString())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        // 클라이언트 생성
        val client = OkHttpClient()
        // 요청 전송
        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                Log.d("요청","요청 완료")
                //Log.d("data",response?.body()?.string()+"")
                //response 는 String 변수
                var t: String? = response.body()?.string()

                //val t = "\ub178\ubbfc\ub3c4"
                // val utf8String= String(t.toByteArray(Charsets.ISO_8859_1),Charsets.UTF_8)
                //val utf8String = String(test.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
                Log.d("data",t+"")
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.d("요청","요청 실패 ")
            }

        })

    }
}