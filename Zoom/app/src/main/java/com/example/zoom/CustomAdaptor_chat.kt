package com.example.zoom

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.chat_item_me.view.*
import kotlinx.android.synthetic.main.chat_item_your.view.*
import kotlinx.android.synthetic.main.subject_item.view.*

class CustomAdaptor_chat : RecyclerView.Adapter<Holderr>() {
    var listData = mutableListOf<Chat>()
    var mAuth= FirebaseAuth.getInstance()
    val user=mAuth.currentUser
    var name=user.displayName
    override fun onBindViewHolder(holder: Holderr, position: Int) {
        var chat = listData.get(position)
        holder.setProfile(chat)

    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holderr {
        Log.d("name",name)
        if(viewType==1){
            var itemView= LayoutInflater.from(parent.context).inflate(
                R.layout.chat_item_me,
                parent,
                false
            )
            return Holderr(itemView)
        }else{
            var itemView= LayoutInflater.from(parent.context).inflate(
                R.layout.chat_item_your,
                parent,
                false
            )
            return Holderr(itemView)
        }

    }

    override fun getItemViewType(position: Int): Int {//여기서 뷰타입을 1, 2로 바꿔서 지정해줘야 내채팅 너채팅을 바꾸면서 쌓을 수 있음

        //내 아이디와 arraylist의 name이 같다면 내꺼 아니면 상대꺼
        return if (listData.get(position).name == name) {
            1
        } else {
            2
        }
    }

}
class Holderr(itemView: View) : RecyclerView.ViewHolder(itemView){
    var mAuth= FirebaseAuth.getInstance()
    val user=mAuth.currentUser
    var name=user.displayName
    init {
        Log.d("name",name)
        itemView.setOnClickListener {
            //여기서 처리

            var pos:Int=adapterPosition
            if(itemViewType==1){
                var sub_name=itemView.findViewById<TextView>(R.id.chat_name_me).text.toString()
                Log.d("number1", sub_name)
            }else{
                var sub_name=itemView.findViewById<TextView>(R.id.chat_name).text.toString()
                Log.d("number1", sub_name)
            }
        }
    }

    fun setProfile(chat: Chat){
        if(name==chat.name){
            itemView.chat_name_me.text=chat.name
            itemView.chat_content_me.text=chat.Content
            itemView.chat_time_me.text=chat.time
        }else{
            itemView.chat_name.text=chat.name
            itemView.chat_content.text=chat.Content
            itemView.chat_time.text=chat.time
        }
        //Glide.with(itemView).load("https://salonspace.s3.ap-northeast-2.amazonaws.com/1610097821332.jpg").error(R.drawable.cc).into(itemView.imgtest)

        //itemView.imgtest.setRectRadius(80f)
    }

}