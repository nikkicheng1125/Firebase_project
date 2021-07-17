package com.yiwencheng.firebase_project.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.yiwencheng.firebase_project.R
import com.yiwencheng.firebase_project.adapters.ImageAdapter
import com.yiwencheng.firebase_project.models.Image
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val storageRef = FirebaseStorage.getInstance().reference
    var imageList:ArrayList<Image> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    private fun init(){
        button_add.setOnClickListener {
            startActivity(Intent(this,UploadActivity::class.java))
        }

        getImages()
    }

    private fun getImages(){
        imageList.clear()
        storageRef.child("images").listAll()
            .addOnSuccessListener { listResult ->
                listResult.items.forEach { storageRef ->
                    storageRef.downloadUrl.addOnSuccessListener {
                        Log.d("main",it.toString())
                        imageList.add(Image(it.toString()))
                        Log.d("images",imageList.toString())
                    }.addOnCompleteListener {
                        recycler_view_image.adapter = ImageAdapter(this,imageList)
                        //recycler_view_image.layoutManager = LinearLayoutManager(this)
                        recycler_view_image.layoutManager = GridLayoutManager(this,2)
                    }
                }
            }
    }
    override fun onStart() {
        super.onStart()
        init()
    }
}




