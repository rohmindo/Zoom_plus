package com.example.zoom

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.EngineIOException
import io.socket.engineio.client.transports.WebSocket
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_question.*
import kotlinx.android.synthetic.main.item_question.*
import org.json.JSONObject
import kotlin.concurrent.thread

class QuestionActivity : AppCompatActivity() {
    public lateinit var QuestionList: List<Question>
    public lateinit var adapter: ExpandableAdapter
    public lateinit var Questions:ArrayList<Question>
    lateinit var mSocket: Socket
    var mAuth= FirebaseAuth.getInstance()
    val user=mAuth.currentUser
    var name=user.displayName
    var Tempcount:Int=1
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        val recyclerView = findViewById<RecyclerView>(R.id.question_recycler)

        QuestionList = ArrayList()
        Questions = ArrayList<Question>()
        var data1=Question("POD가 뭔가요?",1,"이해잘되요\n")
        var data2=Question("알고리즘이 이해가안되요",1,"나도 이해안되요\n")
        var data3=Question("입냄새가 심한데 맞나요?",1,"너 입냄새 심해요\n")
        Questions.add(data1)
        Questions.add(data2)
        Questions.add(data3)
        QuestionList=Questions



        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExpandableAdapter(QuestionList)

        recyclerView.adapter = adapter
        //소켓연결
        try{
            val opts = IO.Options()
            opts.reconnection = true;
            opts.reconnectionDelay = 1000;
            opts.timeout = 10000;
            opts.transports = arrayOf(WebSocket.NAME)
            mSocket = IO.socket(getString(R.string.app_domain)+":3000",opts)
        }catch (e:Exception){
            Log.e("chaterror", e.printStackTrace().toString())
        }
        mSocket.connect()
        //연결 테스트
        mSocket.on(Socket.EVENT_CONNECT) {
            //연결됨
            Log.i("socket", "connect")
            var data= JSONObject()
            //개인이름
            data.put("name",name)
            //수업코드만 셋팅하면됨
            data.put("code","1234")
            mSocket.emit("user",data)
        }.on(Socket.EVENT_CONNECT_ERROR) { args ->
            //연결 에러
            if (args[0] is EngineIOException) {
                Log.i("socket", args[0].toString())
            }
            Log.i("socket", "connect error" + args[0].toString())
        }.on("sendQ"){ args ->
            var inputmsg=JSONObject(args[0].toString())
            var content=inputmsg.getString("content")
            var new:Question= Question(content,0,"")
            thread(){
                runOnUiThread {
                    Questions.add(new)
                    adapter.notifyDataSetChanged()
                }
            }
        }
        //데이터 받으면 1 < index 내용,댓글수,원래 comment + 추가된 내용
        //Questions.set(1,Question("asdf","22",추가))
        //adapter.notifyDataSetChanged()
        //뒤로가기버튼
        backTo.setOnClickListener {
            finish()
        }
    }
    //질문 추가 버튼
    fun Question_plus(v: View?) {
        val intent = Intent(this, PopUpActivity_Question::class.java)
        startActivityForResult(intent,1)

    }
    //질문추가후
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== RESULT_OK&&data!=null) {
            var test = data.getStringExtra("result")
            Questions.add(Question(test.toString(),0,""))
            adapter.notifyDataSetChanged()
            var question_Data=JSONObject();
            question_Data.put("content",test.toString());
            question_Data.put("qNum",Tempcount)
            Tempcount++
            mSocket.emit("question",question_Data)
        //Log.d("returnaa", test + "")
        }
    }
}