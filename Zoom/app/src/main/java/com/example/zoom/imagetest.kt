package com.example.zoom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_imagetest.*

class imagetest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imagetest)

        Glide.with(this)
            .load("https://lh3.googleusercontent.com/-6otQycdOGP8/AAAAAAAAAAI/AAAAAAAAAAA/AMZuuclHP0TfZtAhUYD8nIJBkiIS7ajupQ/s96-c/photo.jpg")
            .error(R.drawable.profile_default)
            .into(pro_test)


    }
}