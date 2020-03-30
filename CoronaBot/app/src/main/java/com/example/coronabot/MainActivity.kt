package com.example.coronabot

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.SyncStateContract.Helpers.insert
import android.speech.RecognizerIntent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.coronabot.MessageServer
import com.example.coronabot.R
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    var image_uri: Uri? = null
    private val REQUEST_CODE_SPEECH_INPUT = 100

    var context = this
    var connectivity : ConnectivityManager? = null
    var info : NetworkInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //button click active camera
        btn_camera.setOnClickListener {
            //if system is Marshmallow or above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED  ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){
                    //PERMISSION was not enabled
                    val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    //SHOW permission
                    requestPermissions(permission, PERMISSION_CODE)
                }else{
                    //permission already granted
                    openCamera()
                }
            }else{
                //system is < Marshmallow
                openCamera()
            }

        }
        var el = findViewById(R.id.editText) as EditText

        fun send(v:View) {
            val messageServer = MessageServer()
            messageServer.execute(el.getText().toString())
        }

        btn.setOnClickListener{
            if (isConnected()){
                Toast.makeText(context, "Connected", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(context, "Not Connected", Toast.LENGTH_LONG).show()
            }
        }

        //button click to
        mic.setOnClickListener {
            speak();
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){//PERMISSION from popup WAS GRANTED
                    openCamera()
                }else{//permission was denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun send(v:View) {

    }

    fun isConnected() : Boolean{
        connectivity = context.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null){
            info = connectivity!!.activeNetworkInfo
            if (info != null){
                if (info!!.state == NetworkInfo.State.CONNECTED){
                    return true
                }
            }
        }
        return false
    }
    //recieve image and voice input
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            //SET IMAGE CAPTURED
            imageView4.setImageURI(image_uri)
        }
        when(requestCode){
            REQUEST_CODE_SPEECH_INPUT ->{
                if (resultCode == Activity.RESULT_OK && null != data){
                    //get txt from result
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    //SET TXT TO TEXTVIEW
                    txtMic.text = result[0]
                }
            }
        }
    }
    private fun speak() {
        //intent to show speechToText
        val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        mIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Enter the data")

        try {//if there isn't error show text dialog
            startActivityForResult(mIntent, REQUEST_CODE_SPEECH_INPUT)
        }catch (e: Exception){// is there any error shows error message
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }
}
