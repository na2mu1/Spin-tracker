package com.spintracker
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        btnStart.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(this)) {
                startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")), 1001)
                tvStatus.text = "অনুমতি দিন, তারপর আবার চাপুন"
            } else {
                startService(Intent(this, OverlayService::class.java))
                tvStatus.text = "✅ ট্র্যাকার চলছে..."
                btnStart.isEnabled = false
                btnStop.isEnabled = true
            }
        }
        btnStop.setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
            tvStatus.text = "⏹ ট্র্যাকার বন্ধ"
            btnStart.isEnabled = true
            btnStop.isEnabled = false
        }
        btnStop.isEnabled = false
    }
}
