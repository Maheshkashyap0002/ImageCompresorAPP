package com.example.imagecompressor

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var progressBar: ProgressBar

    private var selectedBitmap: Bitmap? = null
    private var compressedUri: Uri? = null
    private var quality = 30
    private var targetKB = 100

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { loadImage(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🌈 ROOT LAYOUT (MODERN CLEAN UI)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 80, 50, 50)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // 📌 TITLE (APP STYLE)
        val title = TextView(this).apply {
            text = "Image Compressor"
            textSize = 22f
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Compress • Save • Share"
            textSize = 14f
            alpha = 0.7f
            gravity = Gravity.CENTER
        }

        // 🖼️ IMAGE PREVIEW
        imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(600, 600)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(0xFFEFEFEF.toInt())
            setPadding(10, 10, 10, 10)
        }

        resultText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
        }

        // 🎚️ QUALITY CONTROL
        val qualityText = TextView(this).apply {
            text = "Quality: 30%"
            textSize = 16f
        }

        seekBar = SeekBar(this).apply {
            max = 100
            progress = 30
        }

        seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                quality = value
                qualityText.text = "Quality: $quality%"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // 🎯 TARGET KB INPUT
        val targetInput = EditText(this).apply {
            hint = "Target KB (e.g. 100)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        // ⏳ PROGRESS BAR
        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }

        // 🔘 MODERN ROUNDED BUTTONS
        val selectBtn = MaterialButton(this).apply {
            text = "Select Image"
            icon = getDrawable(android.R.drawable.ic_menu_gallery)
            iconPadding = 12
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            cornerRadius = 60
        }
        val compressBtn = MaterialButton(this).apply {
            text = "Compress & Save"
            icon = getDrawable(android.R.drawable.stat_sys_download)
            iconPadding = 12
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            cornerRadius = 60
        }

        val shareBtn = MaterialButton(this).apply {
            text = "Share Image"
            icon = getDrawable(android.R.drawable.ic_menu_share)
            iconPadding = 12
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            cornerRadius = 60
        }

        // 📏 SPACING FUNCTION
        fun space(h: Int = 25) = Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, h)
        }

        // 📦 ADD UI
        root.addView(title)
        root.addView(subtitle)
        root.addView(space(40))

        root.addView(imageView)
        root.addView(space())

        root.addView(resultText)
        root.addView(space())

        root.addView(qualityText)
        root.addView(seekBar)
        root.addView(targetInput)
        root.addView(progressBar)
        root.addView(space())

        root.addView(selectBtn)
        root.addView(space(15))
        root.addView(compressBtn)
        root.addView(space(15))
        root.addView(shareBtn)

        setContentView(root)

        // 📌 ACTIONS
        selectBtn.setOnClickListener {
            imagePicker.launch("image/*")
        }

        compressBtn.setOnClickListener {
            targetKB = targetInput.text.toString().toIntOrNull() ?: 100
            compressAndSave()
        }

        shareBtn.setOnClickListener {
            shareImage()
        }
    }

    // 📥 LOAD IMAGE
    private fun loadImage(uri: Uri) {
        val input = contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply { inSampleSize = 4 }
        selectedBitmap = BitmapFactory.decodeStream(input, null, options)
        imageView.setImageBitmap(selectedBitmap)
    }

    // 🗜️ COMPRESS
    private fun compressAndSave() {

        val bitmap = selectedBitmap ?: run {
            Toast.makeText(this, "Select image first", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        Thread {

            var q = quality
            var bytes: ByteArray

            do {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, q, stream)
                bytes = stream.toByteArray()
                q -= 5
            } while (bytes.size / 1024 > targetKB && q > 5)

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }

            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

            uri?.let {
                val out = contentResolver.openOutputStream(it)
                out?.write(bytes)
                out?.close()
                compressedUri = it
            }

            runOnUiThread {
                progressBar.visibility = View.GONE

                imageView.setImageBitmap(
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                )

                resultText.text =
                    "Target: ${targetKB}KB\nFinal: ${bytes.size / 1024}KB\nSaved ✔"

                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
            }

        }.start()
    }

    // 📤 SHARE
    private fun shareImage() {

        val uri = compressedUri ?: run {
            Toast.makeText(this, "No image", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        startActivity(Intent.createChooser(intent, "Share Image"))
    }
}