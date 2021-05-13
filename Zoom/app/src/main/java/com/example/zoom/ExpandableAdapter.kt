package com.example.zoom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpandableAdapter(
    private var QuestionList: List<Question>
) : RecyclerView.Adapter<ExpandableAdapter.MyViewHolder>() {
    class MyViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        
        fun bind(question: Question) {
            val content = itemView.findViewById<TextView>(R.id.Question_content)
            val commentcount = itemView.findViewById<TextView>(R.id.Question_commentcount)
            val comment = itemView.findViewById<TextView>(R.id.Question_comment)
            val comment_content = itemView.findViewById<EditText>(R.id.comment_content)
            val register = itemView.findViewById<Button>(R.id.Question_register)
            val layoutExpand = itemView.findViewById<LinearLayout>(R.id.layout_expand)
            content.text=question.content
            commentcount.text="댓글 "+question.comment_count+"개\n"
            comment.text=question.comment
            //댓글 등록버튼 클릭시
            register.setOnClickListener {
                comment.append(comment_content.text.toString() + "\n")
                comment_content.setText("")
                question.comment_count++
                commentcount.text="댓글 "+question.comment_count+"개\n"
            }
            //댓글 클릭시
            commentcount.setOnClickListener {
                // 1
                val show = toggleLayout(!question.isExpanded, it, layoutExpand)
                question.isExpanded = show

            }
        }

        private fun toggleLayout(isExpanded: Boolean, view: View, layoutExpand: LinearLayout): Boolean {
            // 2
            ToggleAnimation.toggleArrow(view, isExpanded)
            if (isExpanded) {
                ToggleAnimation.expand(layoutExpand)
            } else {
                ToggleAnimation.collapse(layoutExpand)
            }
            return isExpanded
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_question,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(QuestionList[position])
    }

    override fun getItemCount(): Int {
        return QuestionList.size
    }
}