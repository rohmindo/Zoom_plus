package com.example.zoom

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_subject__detail.*

class Subject_Detail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject__detail)

        //값 가져와서 세팅하기 과목명,교수명,코드명
        var subname=intent.getStringExtra("subname")
        var professor_name=intent.getStringExtra("proname")
        var subcode=intent.getStringExtra("code")
        subname_proname.text=subname+"("+professor_name+")"

        //아이콘 색깔입히기
        subject_schedule.setColorFilter(Color.parseColor("#00a000"),PorterDuff.Mode.SRC_IN)
        subject_note.setColorFilter(Color.parseColor("#00a000"),PorterDuff.Mode.SRC_IN)
        subject_alarm.setColorFilter(Color.parseColor("#00a000"),PorterDuff.Mode.SRC_IN)
        subject_assignment.setColorFilter(Color.parseColor("#00a000"),PorterDuff.Mode.SRC_IN)
        subject_confirm.setColorFilter(Color.parseColor("#00a000"),PorterDuff.Mode.SRC_IN)
        subject_online.setColorFilter(Color.parseColor("#00a000"),PorterDuff.Mode.SRC_IN)
        subject_video.setColorFilter(Color.parseColor("#00a000"),PorterDuff.Mode.SRC_IN)
        subject_take.setColorFilter(Color.parseColor("#00a000"),PorterDuff.Mode.SRC_IN)
        subject_chart.setColorFilter(Color.parseColor("#00a000"),PorterDuff.Mode.SRC_IN)


        //강의계획서 및 강의정보 클릭시
        schedule_move.setOnClickListener {
            val intent = Intent(this, Subject_Schedule::class.java)
            startActivity(intent)
            finish()
        }
    }
    //공지사항
}