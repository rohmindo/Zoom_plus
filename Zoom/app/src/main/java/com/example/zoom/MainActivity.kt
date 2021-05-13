package com.example.zoom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.zoom.httpconnection.HttpConnection_join
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_subject.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,View.OnClickListener {
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //로그아웃을 위한 구글 계정
        var mAuth=FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            //'R.string.default_web_client_id' 에는 본인의 클라이언트 아이디를 넣어주시면 됩니다.
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val user=mAuth.currentUser
        var photourl=user.photoUrl
        Log.d("url", photourl.toString())


        //네비게이션바 셋팅
        navigation.setOnNavigationItemSelectedListener(this)



        //로그아웃버튼
        btn_logout.setOnClickListener {
            signOut()
        }

    }

    private fun signOut() { // 로그아웃
        // Firebase sign out
        FirebaseAuth.getInstance().signOut()

        // Google sign out
        googleSignInClient.signOut()
        startActivity(Intent(this, LoginActivity::class.java))

    }
    //하단 네비게이션바
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_home -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame, Fragment_home())
                transaction.commit()
                return true
            }

            R.id.action_room -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame, Fragment_room())
                transaction.commit()
                return true
            }
            R.id.action_subject -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame, Fragment_subject())
                transaction.commit()
                return true
            }
            R.id.action_info -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame, Fragment_info())
                transaction.commit()
                return true
            }
        }
        return false;
    }
    override fun onClick(v: View?) {
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()

    }
    fun plus(v: View?) {
        val intent = Intent(this, PopUpActivity::class.java)
        startActivityForResult(intent, 1)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== RESULT_OK&&data!=null) {
            var test = data.getStringExtra("result")
            //subject/joint test
            var tt= HttpConnection_join(test)
            tt.result2()
            var teee=tt.temp
            //통신결과 tee로 전달됨
            Log.d("valuess", teee)
            //받은 결과값 json파싱
            val jObject = JSONObject(teee.toString())
            val subjectstr=jObject.getString("subject")
            val jObject2=JSONObject(subjectstr)
            val sub_name=jObject2.getString("name")
            //val pro_name=jObject.getString("professor")
            val code=jObject2.getString("code")

            val fragment = Fragment_subject()
            val bundle = Bundle()
            bundle.putString("sub_name",sub_name)
            bundle.putString("pro_name", "교수이름")
            bundle.putString("code", code)
            Log.d("testttt",sub_name+" "+code)
            fragment.arguments=bundle

            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(R.id.frame, fragment)
            transaction.commit()

        }

    }
}