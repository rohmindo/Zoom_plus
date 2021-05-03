package com.example.zoom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_room.*
import kotlinx.android.synthetic.main.fragment_subject.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Fragment_room.newInstance] factory method to
 * create an instance of this fragment.
 */
class Fragment_subject : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_subject, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            if (currentUser!!.photoUrl != null) {
                Log.d("MyProfileEditFragment", "PHOTO URL: ${currentUser!!.photoUrl}")
                Glide.with(this).load(currentUser.photoUrl).into(profile_image)
            }
        }

        //리사이클러뷰 셋팅
        var data:MutableList<Subject> = mutableListOf()
        var data1:Subject= Subject("SW 캡스톤 디자인",".임장순","8ab37b016de9a2aa")
        var data2:Subject= Subject("인공지능",".김민구","8ab37b016de9a2aa")
        var data3:Subject= Subject("데이터 베이스",".변광준","8ab37b016de9a2aa")
        var data4:Subject= Subject("도메인 분석 설계",".정 크리스틴","8ab37b016de9a2aa")
        var data5:Subject= Subject("분산시스템",".최민우","8ab37b016de9a2aa")
        var data6:Subject= Subject("프로그래밍 설계",".노민도","8ab37b016de9a2aa")
        var data7:Subject= Subject("마음챙김과 자기조절",".김수민","8ab37b016de9a2aa")

        data.add(data1)
        data.add(data2)
        data.add(data3)
        data.add(data4)
        data.add(data5)
        data.add(data6)
        data.add(data7)

        // 어댑터 생성
        var adaptor:CustomAdaptor= CustomAdaptor()
        // 어댑터에 데이터 전달
        adaptor.listData=data
        // 화면의 있는 리사이클러 뷰에 어댑터 연결
        recycler.adapter=adaptor
        // 레이아웃 매니저 연결
        recycler.layoutManager= LinearLayoutManager(context)
        recycler.scrollToPosition(data.size-1)
    }


}