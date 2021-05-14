package com.example.zoom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.EngineIOException
import io.socket.engineio.client.transports.WebSocket
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_item_me.*
import kotlinx.android.synthetic.main.fragment_subject.*
import kotlinx.android.synthetic.main.fragment_subject.recycler
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class ChatActivity : AppCompatActivity() {
    var mAuth= FirebaseAuth.getInstance()
    val user=mAuth.currentUser
    var name=user.displayName
    var adaptor:CustomAdaptor_chat= CustomAdaptor_chat()
    lateinit var mSocket: Socket
    //현재시각
    lateinit var currenttime:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("hh:mm")
        val curTime = dateFormat.format(Date(time))
        currenttime=curTime.toString()

        var data:MutableList<Chat> = mutableListOf()
        var data1:Chat= Chat("노민도","교수님 질문이 있습니다.","11:25")
        var data2:Chat= Chat("최민우","저도 질문이 있습니다.","11:26")
        var data3:Chat= Chat("김민건","저도 잘 모르겠습니다.","11:27")

        data.add(data1)
        data.add(data2)
        data.add(data3)
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
        }.on("sendMsg"){ args ->
            //메세지 왓을때
            Log.d("sendmsg",args[0].toString())
            var inputmsg=JSONObject(args[0].toString())
            var content=inputmsg.getString("content")
            var time=inputmsg.getString("time")
            var name=inputmsg.getString("name")
            var new:Chat= Chat(name,content,time)
            thread(){
                runOnUiThread {
                    data.add(new)
                    adaptor.notifyDataSetChanged()
                    chat_recycler.scrollToPosition(data.size-1)
                }
            }
        }
        // 어댑터 생성
        // 어댑터에 데이터 전달
        adaptor.listData=data
        // 화면의 있는 리사이클러 뷰에 어댑터 연결
        chat_recycler.adapter=adaptor
        // 레이아웃 매니저 연결
        chat_recycler.layoutManager= LinearLayoutManager(this)
        chat_recycler.scrollToPosition(data.size-1)

        //전송 버튼 리스너
        send.setOnClickListener {
            var content=inputdata.text.toString()
            var new:Chat= Chat(name,content,currenttime)
            inputdata.setText("")
            data.add(new)
            adaptor.notifyDataSetChanged()
            chat_recycler.scrollToPosition(data.size-1)
            var inputdata_server=JSONObject()
            inputdata_server.put("time",currenttime)
            inputdata_server.put("content",content)
            mSocket.emit("msg",inputdata_server)
        }
    }
}