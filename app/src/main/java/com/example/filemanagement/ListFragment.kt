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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.ListFragment
import java.io.File

class ListFragment : Fragment() {

    private lateinit var listView: ListView
    private var fileList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var currentDirectory: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView)
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, fileList)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            handleItemClick(position)
        }

        val filePath = arguments?.getString(FILE_PATH_KEY)
        currentDirectory = File(filePath)
        listFilesAndDirectories(currentDirectory)

        registerForContextMenu(listView)

        listView.setOnCreateContextMenuListener { menu, v, menuInfo ->
            activity?.menuInflater?.inflate(R.menu.menu, menu)

            val info = menuInfo as AdapterView.AdapterContextMenuInfo
            val selectedItem = fileList[info.position]
            val selectedFile = File(currentDirectory, selectedItem)

            if (selectedFile.isDirectory) menu?.findItem(R.id.menu_copy)?.isVisible = false
        }
    }

    private fun listFilesAndDirectories(directory: File) {
        fileList.clear()
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                fileList.add(file.name)
            }
        }
        updateListView()
    }

    fun handleItemClick(position: Int) {
        val itemName = fileList[position]
        val clickedFile = File(currentDirectory, itemName)

        if (clickedFile.isDirectory) {
            (activity as MainActivity).createListFragment(clickedFile)
        } else {
            openFile(clickedFile)
        }
    }

    fun openFile(file: File) {
        val fileName = file.name.lowercase()
        when {
            fileName.endsWith(".txt") -> textFile(file)
            fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".png") -> displayImageFile(file)
        }
    }

    fun textFile(file: File) {
        (activity as MainActivity).displayTextFile(file)
    }

    private fun displayImageFile(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.filemanagement.provider",
            file
        )

        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val selectedItem = fileList[info.position]
        val selectedFile = File(currentDirectory, selectedItem)

        when (item.itemId) {
            R.id.menu_rename -> {
                val editText = EditText(requireContext())
                editText.setText(selectedItem)

                AlertDialog.Builder(requireContext())
                    .setTitle("Rename")
                    .setMessage("Enter the new name:")
                    .setView(editText)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val newName = editText.text.toString()
                        val newFile = File(selectedFile.parent, newName)
                        val renamed = selectedFile.renameTo(newFile)
                        if (renamed) {
                            Toast.makeText(requireContext(), "Renamed to $newName", Toast.LENGTH_SHORT).show()
                            listFilesAndDirectories(currentDirectory)
                        } else {
                            Toast.makeText(requireContext(), "Failed to rename $selectedItem", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                return true
            }
            R.id.menu_delete -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete $selectedItem?")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        if (selectedFile.isDirectory) {
                            deleteDirectory(selectedFile)
                        } else {
                            selectedFile.delete()
                        }
                        listFilesAndDirectories(currentDirectory)
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
                return true
            }
            R.id.menu_copy -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Copy")
                    .setMessage("Are you sure you want to copy $selectedItem?")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        Toast.makeText(requireContext(), "Copy $selectedItem", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    fun deleteDirectory(directory: File) {
        val files = directory.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                deleteDirectory(file)
            } else {
                file.delete()
            }
        }
        directory.delete()
    }

    private fun updateListView() {
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, fileList)
        listView.adapter = adapter
    }

    companion object {
        private const val FILE_PATH_KEY = "file_path_key"
        @JvmStatic
        fun newInstance(filePath: String): com.example.filemanagement.ListFragment{
            val fragment = com.example.filemanagement.ListFragment()
            val args = Bundle()
            args.putString(FILE_PATH_KEY, filePath)
            fragment.arguments = args
            return fragment
        }
    }
}