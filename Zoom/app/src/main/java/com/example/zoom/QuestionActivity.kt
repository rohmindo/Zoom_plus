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
import kotlinx.android.synthetic.main.activity_question.*
import kotlinx.android.synthetic.main.item_question.*

class QuestionActivity : AppCompatActivity() {
    public lateinit var QuestionList: List<Question>
    public lateinit var adapter: ExpandableAdapter
    public lateinit var Questions:ArrayList<Question>
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
        //Log.d("returnaa", test + "")
        }

    }
}