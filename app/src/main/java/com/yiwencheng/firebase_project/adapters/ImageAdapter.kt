package com.yiwencheng.firebase_project.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.yiwencheng.firebase_project.R
import com.yiwencheng.firebase_project.models.Image
import kotlinx.android.synthetic.main.activity_upload.view.*
import kotlinx.android.synthetic.main.activity_upload.view.image_view
import kotlinx.android.synthetic.main.row_image_adapter.view.*

class ImageAdapter(var myContext: Context,var imageList:ArrayList<Image>):RecyclerView.Adapter<ImageAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(myContext).inflate(R.layout.row_image_adapter,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var image = imageList[position]
        holder.bind(image)
    }

    override fun getItemCount(): Int {
       return imageList.size
    }

    inner class  MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(image:Image){
            var url = image.url
            Picasso.get().load(url).into(itemView.image_view)
        }
    }

}