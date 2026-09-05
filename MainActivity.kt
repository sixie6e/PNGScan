package com.distherapy.pngscan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var scanner: ImageOcrScanner
    private lateinit var searchInput: EditText
    private lateinit var adapter: OcrResultAdapter

    private val chooseAndScanLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { folderUri: Uri? ->
        if (folderUri != null) {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                scanFolder(folderUri, query)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanner = ImageOcrScanner(this)
        searchInput = findViewById(R.id.search_input)
        val scanButton: Button = findViewById(R.id.scan_button)
        val recyclerView: RecyclerView = findViewById(R.id.results_recycler_view)

        adapter = OcrResultAdapter { uri -> openImage(uri) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        scanButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(this, "Please enter search text before selecting a folder", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            chooseAndScanLauncher.launch(null)
        }
    }

    private fun scanFolder(folderUri: Uri, query: String) {
        adapter.clear()
        Toast.makeText(this, "Scanning folder...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val rootDirectory = DocumentFile.fromTreeUri(applicationContext, folderUri)
                if (rootDirectory != null && rootDirectory.isDirectory) {
                    processDirectory(rootDirectory, query)
                }
            }
            Toast.makeText(this@MainActivity, "Scan complete", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun processDirectory(directory: DocumentFile, query: String) {
        val files = directory.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                processDirectory(file, query)
            } else if (file.isFile) {
                val mimeType = file.type ?: ""
                val fileName = file.name ?: ""

                if (mimeType.startsWith("image/") ||
                    fileName.endsWith(".png", ignoreCase = true) ||
                    fileName.endsWith(".jpg", ignoreCase = true) ||
                    fileName.endsWith(".jpeg", ignoreCase = true)) {

                    val isFound = scanner.scanImageForText(file.uri, query)
                    if (isFound) {
                        withContext(Dispatchers.Main) {
                            adapter.addResult(OcrResultItem(file.uri, fileName))
                        }
                    }
                }
            }
        }
    }

    private fun openImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }
}
