package com.spintracker
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var totalSpins = 0
    private var currentCount = 0
    private val bigwinIntervals = mutableListOf<Int>()
    private lateinit var tvSpinCount: TextView
    private lateinit var tvAvg: TextView
    private lateinit var tvLastEvent: TextView
    companion object { const val CHANNEL_ID = "SpinTrackerChannel" }
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildNotification())
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        setupOverlay()
    }
    private fun setupOverlay() {
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        tvSpinCount = overlayView.findViewById(R.id.tvSpinCount)
        tvAvg = overlayView.findViewById(R.id.tvAvg)
        tvLastEvent = overlayView.findViewById(R.id.tvLastEvent)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END; x = 10; y = 150 }
        overlayView.findViewById<Button>(R.id.btnBigwin).setOnClickListener { recordEvent("BigWin 🏆") }
        overlayView.findViewById<Button>(R.id.btnSuper).setOnClickListener { recordEvent("Super ✨") }
        overlayView.findViewById<Button>(R.id.btnBonus).setOnClickListener { recordEvent("Bonus 🎁") }
        overlayView.findViewById<Button>(R.id.btnSpin).setOnClickListener {
            totalSpins++; currentCount++; updateUI()
        }
        overlayView.findViewById<Button>(R.id.btnReset).setOnClickListener {
            totalSpins=0; currentCount=0; bigwinIntervals.clear()
            tvLastEvent.text="শেষ: —"; updateUI()
        }
        windowManager.addView(overlayView, params)
        updateUI()
    }
    private fun recordEvent(name: String) {
        totalSpins++; currentCount++
        bigwinIntervals.add(currentCount)
        currentCount = 0
        tvLastEvent.text = "শেষ: $name"
        updateUI()
    }
    private fun updateUI() {
        tvSpinCount.text = "স্পিন: $currentCount"
        val avg = if (bigwinIntervals.isNotEmpty()) bigwinIntervals.average().toInt() else 0
        tvAvg.text = "গড়: ${if (avg > 0) "$avg" else "—"}"
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Spin Tracker", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Spin Tracker চলছে")
        .setSmallIcon(android.R.drawable.ic_menu_view).build()
    override fun onBind(intent: Intent?) = null
    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) try { windowManager.removeView(overlayView) } catch (_: Exception) {}
    }
}
