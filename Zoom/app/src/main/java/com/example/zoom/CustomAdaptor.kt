package com.example.zoom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.subject_item.view.*

class CustomAdaptor : RecyclerView.Adapter<Holder>() {
    var listData = mutableListOf<Subject>()
    override fun onBindViewHolder(holder: Holder, position: Int) {
        var subject = listData.get(position)
        holder.setProfile(subject)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        var itemView= LayoutInflater.from(parent.context).inflate(
            R.layout.subject_item,
            parent,
            false
        )
        return Holder(itemView)
    }
}
class Holder(itemView: View) : RecyclerView.ViewHolder(itemView){

    init {
        itemView.setOnClickListener {
            //여기서 처리
            var pos:Int=adapterPosition

            var sub_name=itemView.findViewById<TextView>(R.id.subject_name).text.toString()
            var pro_name=itemView.findViewById<TextView>(R.id.professor_name).text.toString()
            var sub_code=itemView.findViewById<TextView>(R.id.subject_day).text.toString()
            Log.d("number1", sub_name)
            val intent = Intent(itemView.context, Subject_Detail::class.java)
            intent.putExtra("subname", sub_name)
            intent.putExtra("proname",pro_name)
            intent.putExtra("code",sub_code)
            startActivityForResult(itemView.context as Activity,intent,101,null)



        }
    }

    fun setProfile(subject: Subject){
        //Glide.with(itemView).load("https://salonspace.s3.ap-northeast-2.amazonaws.com/1610097821332.jpg").error(R.drawable.cc).into(itemView.imgtest)
        itemView.subject_name.text=subject.name
        itemView.professor_name.text=subject.pro_name
        itemView.subject_day.text=subject.code
        //itemView.imgtest.setRectRadius(80f)
    }
}