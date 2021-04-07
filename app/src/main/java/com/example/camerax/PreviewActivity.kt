package com.example.camerax

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_preview.*
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

class PreviewActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        imageUri = intent.getParcelableExtra(IMAGE_URI)

        Log.d("QAQ", "imageUri: $imageUri")

        imageUri?.also {
            Picasso.get()
                .load(it)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imageView)
        }

        okButton.setOnClickListener {
            //handle media store
            val resolver = this.contentResolver
            val filename = SimpleDateFormat(
                FILENAME_FORMAT,
                Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            // set this value, system will put photo at Pictures default.
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

            val uri = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri == null) {
                Toast.makeText(baseContext, "content resolver fail", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val outputStream = resolver.openOutputStream(uri)
                    outputStream?.let {
                        imageView.drawToBitmap().compress(Bitmap.CompressFormat.JPEG, 100, it)
                        it.flush()
                        it.close()
                    }
                    resolver.update(uri, contentValues, null, null)
                    Toast.makeText(baseContext, "content resolver success: $uri", Toast.LENGTH_SHORT).show()
                } catch (e: FileNotFoundException) {
                    Toast.makeText(baseContext, "content resolver update fail", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val IMAGE_URI = "IMAGE_URI"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        fun start(activity: Activity, uri: Uri) {
            activity.startActivity(Intent(activity, PreviewActivity::class.java).apply {
                putExtra(IMAGE_URI, uri)
            })
        }
    }
}