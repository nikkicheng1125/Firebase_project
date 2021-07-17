package com.yiwencheng.firebase_project.activities

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Gallery
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.yiwencheng.firebase_project.R
import kotlinx.android.synthetic.main.activity_upload.*
import java.io.File
import java.io.IOException
import java.util.*


class UploadActivity : AppCompatActivity() {

    val storageRef = FirebaseStorage.getInstance().reference
    lateinit var currentPhotoPath: String
    val REQUEST_CAMERA_CODE = 100
    val REQUEST_GALLERY_CODE = 110
    lateinit var current_uri: Uri

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        init()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun init() {
        button_camera.setOnClickListener {
            requestCameraPermission()
        }

        button_gallery.setOnClickListener {
            requestGalleryPermissions()
        }

        button_upload.setOnClickListener {
            uploadImage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun uploadImage() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageRef = storageRef.child("images/IMG_${timeStamp}_${current_uri.lastPathSegment}")
        imageRef.putFile(current_uri)
            .addOnSuccessListener {
                Toast.makeText(applicationContext,"Upload Successfully",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(applicationContext,"Upload Failed",Toast.LENGTH_SHORT).show()
            }

    }

    private fun requestGalleryPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                     var intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.setType("image/*")
                        startActivityForResult(Intent.createChooser(intent,"Select Picture"),REQUEST_GALLERY_CODE)
                    }
                    if (report!!.isAnyPermissionPermanentlyDenied) {
                        showDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).onSameThread().check()
    }


    private fun requestCameraPermission() {
        Dexter.withContext(this)
            .withPermissions(Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted()){
                        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        var imageFile = createImageFile()
                        var uri = FileProvider.getUriForFile(this@UploadActivity, applicationContext.packageName + ".provider", imageFile)
                        current_uri = uri

                        var values = ContentValues()
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

                        values.put(MediaStore.Images.Media.TITLE,"IMG")
                        values.put(MediaStore.Images.Media.DESCRIPTION,timeStamp)

                        current_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)!!
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,current_uri)
                        startActivityForResult(intent, REQUEST_CAMERA_CODE)
                    }

                    if (report!!.isAnyPermissionPermanentlyDenied) {
                        showDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()

    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }

    }

    private fun showDialog() {
        var builder = AlertDialog.Builder(this)
        builder.setTitle("Request Permission")
            .setMessage("Please give permission to continue")
            .setPositiveButton("Go to settings", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                    openAppSettings()
                }
            })
            .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                }
            })
            .show()
    }

    private fun openAppSettings() {
        var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        var uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivityForResult(intent, REQUEST_CAMERA_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA_CODE) {
                galleryAddPic()
                image_view.setImageURI(current_uri)
            }else if(requestCode == REQUEST_GALLERY_CODE){
                current_uri = data?.data!!
                image_view.setImageURI(current_uri)
            }
        }
    }
}
