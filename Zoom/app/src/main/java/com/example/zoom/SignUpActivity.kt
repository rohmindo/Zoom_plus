package com.example.zoom

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.zoom.httpconnection.HttpConnection
import com.example.zoom.httpconnection.HttpConnection_join
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up.*
import okhttp3.*
import org.json.JSONObject
import java.net.CookieManager

class SignUpActivity : AppCompatActivity() {
    //교수인지 학생인지 check해주는 value
    var type:Int = 0
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)



        //이미 가입된거면 바로 메인화면으로
        var mAuth= FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            //'R.string.default_web_client_id' 에는 본인의 클라이언트 아이디를 넣어주시면 됩니다.
            //저는 스트링을 따로 빼서 저렇게 사용했지만 스트링을 통째로 넣으셔도 됩니다.
            .requestEmail()
            .build()

        val user=mAuth.currentUser
        var email=user.email

        //이미 로그인 정보가 있을 시 쿠키받아오고 메인화면으로 이동
        ///auth/login통신
        var t=HttpConnection(getString(R.string.app_domain),email);
        t.result()
        var tee=t.temp
        //통신결과 tee로 전달됨
        val jObject = JSONObject(tee)
        val logInisSuccess=jObject.getString("success")
        Log.d("logintest",logInisSuccess)
        //만약 기존의 로그인 정보가 있다면 메인화면으로 이동
        if(logInisSuccess=="true"){
            val intent=Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }



        //회원가입 버튼
        btn_signup.setOnClickListener {
            if(rbtn_pro.isChecked()){
                //교수
                type=0
                val intent=Intent(this, SignUp_Pro::class.java)
                intent.putExtra("type", type)
                startActivity(intent)
                finish()
            }
            if(rbtn_stu.isChecked()){
                //학생
                type=1
                val intent=Intent(this, SignUp_Student::class.java)
                intent.putExtra("type", type)
                startActivity(intent)
                finish()
            }
        }

        //가입 2개 버튼 커스텀
        rbtn_pro.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (b) {
                compoundButton.buttonTintList = ColorStateList.valueOf(
                    Color.parseColor(
                        "#407ad6"
                    )
                )
            } else {
                compoundButton.buttonTintList = ColorStateList.valueOf(
                    Color.parseColor(
                        "#000000"
                    )
                )
            }
        })
        rbtn_stu.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (b) {
                compoundButton.buttonTintList = ColorStateList.valueOf(
                    Color.parseColor(
                        "#407ad6"
                    )
                )
            } else {
                compoundButton.buttonTintList = ColorStateList.valueOf(
                    Color.parseColor(
                        "#000000"
                    )
                )
            }
        })
    }
}