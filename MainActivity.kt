package com.example.pngscan
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.pngscan.ImageOcrScanner
import com.example.pngscan.R

class MainActivity : AppCompatActivity() {

    private lateinit var scanner: ImageOcrScanner
    private lateinit var searchInput: EditText
    private lateinit var resultTextView: TextView

    private val selectImagesLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        val query = searchInput.text.toString()
        if (query.isNotEmpty() && uris.isNotEmpty()) {
            runScan(uris, query)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanner = ImageOcrScanner(this)

        searchInput = findViewById(R.id.search_input)
        val scanButton: Button = findViewById(R.id.scan_button)
        resultTextView = findViewById(R.id.result_text)

        scanButton.setOnClickListener {
            selectImagesLauncher.launch(arrayOf("image/png", "image/jpeg"))
        }
    }

    private fun runScan(uris: List<Uri>, query: String) {
        lifecycleScope.launch {
            resultTextView.text = "Scanning ${uris.size} images...\n"
            var foundCount = 0

            for (uri in uris) {
                val isFound = scanner.scanImageForText(uri, query)
                if (isFound) {
                    foundCount++
                    resultTextView.append("[+] Found in: ${uri.lastPathSegment}\n")
                }
            }
            resultTextView.append("\nScan complete. Found $foundCount matches.")
        }
    }
}
