package com.example.zoom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_item_me.*
import kotlinx.android.synthetic.main.fragment_subject.*
import kotlinx.android.synthetic.main.fragment_subject.recycler

class ChatActivity : AppCompatActivity() {
    var mAuth= FirebaseAuth.getInstance()
    val user=mAuth.currentUser
    var name=user.displayName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        var data:MutableList<Chat> = mutableListOf()
        var data1:Chat= Chat("노민도","교수님 질문이 있습니다.","11:25")
        var data2:Chat= Chat("최민우","저도 질문이 있습니다.","11:26")
        var data3:Chat= Chat("김민건","저도 잘 모르겠습니다.","11:27")

        data.add(data1)
        data.add(data2)
        data.add(data3)

        // 어댑터 생성
        var adaptor:CustomAdaptor_chat= CustomAdaptor_chat()
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
            var new:Chat= Chat(name,content,"11:27")
            inputdata.setText("")
            data.add(new)
            adaptor.notifyDataSetChanged()
            chat_recycler.scrollToPosition(data.size-1)
        }
    }
}