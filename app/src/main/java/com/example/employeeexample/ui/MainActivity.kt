package com.example.employeeexample.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.employeeexample.R
import com.example.employeeexample.data.EmployeeOfTheDayWorker
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun createFile(context: Context, folder: String, ext: String): File {
    val timeStamp: String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val filesDir: File? = context.getExternalFilesDir(folder)
    val newFile = File(filesDir, "$timeStamp.$ext")
    newFile.createNewFile()
    return newFile
}


fun Activity.showToast(msg: String, duration: Int = Toast.LENGTH_SHORT){
    Toast.makeText(this, msg, duration).show()
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       // runWorker()
    }
//    private fun runWorker(){
//        val work = OneTimeWorkRequest.Builder(EmployeeOfTheDayWorker::class.java).build()
//        WorkManager.getInstance(this).enqueue(work)
//    }

    override fun onBackPressed() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }

    }

}
