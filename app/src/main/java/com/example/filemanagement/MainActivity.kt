package com.example.filemanagement

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.filemanagement.databinding.ActivityMainBinding
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files


class MainActivity : AppCompatActivity() {
    private lateinit var currentDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT < 30) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                Log.v("TAG", "Permission Denied")
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1234)
            } else
                Log.v("TAG", "Permission Granted")
        } else {
            if (!Environment.isExternalStorageManager()) {
                Log.v("TAG", "Permission Denied")
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            } else {
                Log.v("TAG", "Permission Granted")
            }
        }

        currentDirectory = Environment.getExternalStorageDirectory()
        val listFragment = ListFragment.newInstance(currentDirectory.absolutePath)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, listFragment)
            .commit()

    }

    fun displayTextFile(file: File) {
        val textFileFragment = TextFragment.newInstance(file.absolutePath)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, textFileFragment)
            .addToBackStack(null)
            .commit()
    }

    fun createListFragment(file: File) {
        val listFragment = ListFragment.newInstance(file.absolutePath)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, listFragment)
            .addToBackStack(null)
            .commit()
    }

}