package com.legend.mc

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

private const val ACTION_STOP = "com.legend.mc.ACTION_STOP"

class FloatingCommandService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubble: View? = null
    private var panel: View? = null
    private lateinit var bubbleParams: WindowManager.LayoutParams

    private val commands = listOf(
        Pair("الانتقال إلى لاعب", "/tp @s Player"),
        Pair("تغيير الوقت إلى النهار", "/time set day"),
        Pair("تغيير الوقت إلى الليل", "/time set night"),
        Pair("إزالة المطر", "/weather clear"),
        Pair("الحصول على ألماس", "/give @s diamond 64"),
        Pair("تفعيل الطيران", "/ability @s mayfly true"),
        Pair("استدعاء قروي", "/summon villager"),
        Pair("الوضع الإبداعي", "/gamemode creative")
    )

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(1001, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !android.provider.Settings.canDrawOverlays(this)
        ) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (bubble == null) createBubble()
        return START_STICKY
    }

    private fun createBubble() {
        val view = TextView(this).apply {
            text = "MC"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            background = shape(0xFF00C878.toInt(), 100)
            elevation = 12f
        }

        bubbleParams = WindowManager.LayoutParams(
            dp(58),
            dp(58),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(20)
            y = dp(180)
        }

        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = bubbleParams.x
                    startY = bubbleParams.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()

                    if (kotlin.math.abs(dx) > dp(5) ||
                        kotlin.math.abs(dy) > dp(5)
                    ) moved = true

                    val metrics = resources.displayMetrics
                    val maxX = (metrics.widthPixels - bubbleParams.width).coerceAtLeast(0)
                    val maxY = (metrics.heightPixels - bubbleParams.height).coerceAtLeast(0)

                    bubbleParams.x = (startX + dx).coerceIn(0, maxX)
                    bubbleParams.y = (startY + dy).coerceIn(0, maxY)

                    windowManager.updateViewLayout(view, bubbleParams)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!moved) togglePanel()
                    true
                }

                else -> true
            }
        }

        bubble = view
        windowManager.addView(view, bubbleParams)
    }

    private fun togglePanel() {
        if (panel != null) {
            closePanel()
        } else {
            showPanel()
        }
    }

    private fun showPanel() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = shape(0xFF17221C.toInt(), 18)
            elevation = 16f
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "MC LEGEND • الأوامر"
            textSize = 17f
            setTextColor(0xFFFFC857.toInt())
            typeface = Typeface.DEFAULT_BOLD
        }

        val close = TextView(this).apply {
            text = "✕"
            textSize = 21f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(4), dp(4), dp(4))
            setOnClickListener { closePanel() }
        }

        header.addView(title, LinearLayout.LayoutParams(0, dp(42), 1f))
        header.addView(close)
        root.addView(header)

        val search = EditText(this).apply {
            hint = "ابحث عن أمر..."
            textSize = 14f
            setTextColor(Color.WHITE)
            setHintTextColor(0xFF9EAAA2.toInt())
            singleLine = true
            setPadding(dp(10), 0, dp(10), 0)
            background = shape(0xFF26352C.toInt(), 10)
        }

        root.addView(
            search,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
            ).apply { topMargin = dp(10) }
        )

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(list)
        }

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(300)
            ).apply { topMargin = dp(10) }
        )

        fun updateList(query: String) {
            list.removeAllViews()

            commands.filter {
                it.first.contains(query, ignoreCase = true) ||
                    it.second.contains(query, ignoreCase = true)
            }.forEach { item ->

                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(8), dp(7), dp(8), dp(7))
                    background = shape(0xFF26352C.toInt(), 10)
                }

                val commandText = TextView(this).apply {
                    text = "${item.first}\n${item.second}"
                    textSize = 12f
                    setTextColor(Color.WHITE)
                }

                val copy = Button(this).apply {
                    text = "نسخ"
                    textSize = 11f
                    setOnClickListener {
                        val clipboard =
                            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("Minecraft command", item.second)
                        )
                        Toast.makeText(
                            this@FloatingCommandService,
                            "تم نسخ الأمر",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                row.addView(
                    commandText,
                    LinearLayout.LayoutParams(0, dp(52), 1f)
                )
                row.addView(copy)

                list.addView(
                    row,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(7) }
                )
            }
        }

        updateList("")

        search.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {
                    updateList(s?.toString().orEmpty())
                }

                override fun afterTextChanged(s: android.text.Editable?) {}
            }
        )

        val params = WindowManager.LayoutParams(
            dp(320),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(90)
            softInputMode =
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        panel = root
        windowManager.addView(root, params)
    }

    private fun closePanel() {
        panel?.let {
            windowManager.removeView(it)
            panel = null
        }
    }

    private fun shape(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "mc_legend_floating",
                "الدائرة العائمة",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "mc_legend_floating")
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        val stopIntent = Intent(this, FloatingCommandService::class.java).apply {
            action = ACTION_STOP
        }

        val stopPendingIntent = PendingIntent.getService(
            this,
            1002,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return builder
            .setContentTitle("MC LEGEND")
            .setContentText("الدائرة العائمة للأوامر تعمل")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .addAction(android.R.drawable.ic_media_pause, "إيقاف", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        closePanel()
        bubble?.let {
            windowManager.removeView(it)
            bubble = null
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
