package com.example.filemanagement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.io.File
import java.nio.charset.StandardCharsets


class TextFragment : Fragment() {

    lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView = view.findViewById(R.id.textView)

        val filePath = arguments?.getString(FILE_PATH_KEY)
        if (filePath != null) {
            displayTextContent(File(filePath))
        }
    }

    private fun displayTextContent(file: File) {
        val inputStream = file.inputStream()
        val reader = inputStream.reader()
        val content = reader.readText()
        reader.close()

        textView.text = content
    }

    companion object {
        private const val FILE_PATH_KEY = "file_path_key"
        @JvmStatic
        fun newInstance(filePath: String): TextFragment {
            val fragment = TextFragment()
            val args = Bundle()
            args.putString(FILE_PATH_KEY, filePath)
            fragment.arguments = args
            return fragment
        }
    }
}