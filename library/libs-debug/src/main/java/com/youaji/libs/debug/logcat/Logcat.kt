package com.youaji.libs.debug.logcat

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.youaji.libs.debug.R
import com.youaji.libs.debug.databinding.LibsDebugItemLogBinding
import com.youaji.libs.debug.util.selector
import com.youaji.libs.debug.widget.ScaleImage
import com.youaji.libs.debug.widget._float.FloatDisplayType
import com.youaji.libs.debug.widget._float.FloatWindow
import com.youaji.libs.ui.adapter.RecycleViewAdapter
import com.youaji.libs.util.logger.setLoggerOutput
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

fun logcatWindow(application: Application) {
    FloatWindow.with(application)
        .setTag("logcat_window")
        .setShowPattern(FloatDisplayType.OnlyForeground)
        .setLocation(100, 100)
        .setAnimator(null)
        .hasEditText(true)
        .setLayout(R.layout.libs_debug_layout_float_logcat_window) {
            val content = it.findViewById<ViewGroup>(R.id.content)
            val params = content.layoutParams as ViewGroup.LayoutParams
            val logAdapter = LogAdapter()
            val list = it.findViewById<RecyclerView>(R.id.list)
            list.animation = null
            list.postDelayed({
                list.adapter = logAdapter
                list.scrollToPosition(logAdapter.dataList.size - 1)
            }, 1000)
            setLoggerOutput { level, tag, message, _ ->
                logAdapter.addLog(LogInfo(level, tag, message))
                Log.i("------", message)
            }
            it.findViewById<ScaleImage>(R.id.icon_scale).onScaledListener =
                object : ScaleImage.OnScaledListener {
                    override fun onScaled(x: Float, y: Float, event: MotionEvent) {
                        params.width = max(params.width + x.toInt(), 400)
                        params.height = max(params.height + y.toInt(), 300)
                        // 更新悬浮窗的大小，可以避免在其他应用横屏时，宽度受限
                        FloatWindow.updateFloat("logcat_window", width = params.width, height = params.height)
                    }
                }

            val catch = it.findViewById<AppCompatImageView>(R.id.icon_catch)
            catch.setOnClickListener { catch.setCatch(logAdapter) }
            it.findViewById<AppCompatImageView>(R.id.icon_filter).setOnClickListener { v ->
//                val filters = mapOf(
//                    "Wtf" to 0,
//                    "verbose" to Log.VERBOSE,
//                    "debug" to Log.DEBUG,
//                    "info" to Log.INFO,
//                    "warn" to Log.WARN,
//                    "error" to Log.ERROR,
//                    "assert" to Log.ASSERT,
//                    "all" to -1,
//                )
//                val items = filters.keys.toList()
//                v.context.selector(items) { _, i ->
//                    logAdapter.setFilter(filters[items[i]] ?: -1)
//                }
            }
            it.findViewById<AppCompatImageView>(R.id.icon_clear).setOnClickListener { logAdapter.clear() }
            it.findViewById<AppCompatImageView>(R.id.icon_close).setOnClickListener {
                FloatWindow.dismiss("logcat_window")
            }
        }
        .show()
}

private fun AppCompatImageView.setCatch(adapter: LogAdapter) {
    adapter.setCatch(!adapter.isCatch)
    setImageResource(
        if (adapter.isCatch) R.drawable.libs_debug_ic_pause_24_white
        else R.drawable.libs_debug_ic_play_24_white
    )
}

private data class LogInfo(val level: Int, val tag: String, val message: String)
private class LogAdapter : RecycleViewAdapter<LogInfo, LibsDebugItemLogBinding>() {

    private val maxCount = 600
    private val removeCount = maxCount / 3
    private var recyclerView: RecyclerView? = null

    var isCatch = true
        private set
    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.CHINA)

    fun addLog(log: LogInfo) {
        if (!isCatch) return
        addData(mutableListOf(log), false)
        Log.i("------111", "${dataList.size}")
        if (dataList.size > maxCount) {
            val oldData = dataList.subList(0, removeCount)
            removeDataRange(oldData)
//            dataList.removeAll(dataList.subList(0, removeCount))
        }
        Log.i("------222", "${dataList.size}")
        recyclerView?.scrollToPosition(dataList.size - 1)
    }

    fun setCatch(enable: Boolean) {
        isCatch = enable
    }

    fun setFilter(level: Int) {

    }

    fun clear() {
        removeData()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<LibsDebugItemLogBinding> {
        val inflate = LibsDebugItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(inflate)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder<LibsDebugItemLogBinding>,
        position: Int,
        binding: LibsDebugItemLogBinding,
        bean: LogInfo
    ) {
        val date = dateFormat.format(Date())
        var color = Color.WHITE
        val level = when (bean.level) {
            0 -> {
                "Wtf"
            }

            Log.VERBOSE -> {
                "V"
            }

            Log.DEBUG -> {
                "D"
            }

            Log.INFO -> {
                color = Color.YELLOW
                "I"
            }

            Log.WARN -> {
                color = Color.YELLOW
                "W"
            }

            Log.ERROR -> {
                color = Color.RED
                "E"
            }

            Log.ASSERT -> {
                "A"
            }

            else -> {
                "Unknown"
            }
        }

        holder.binding.info.text = "$level $date ${bean.tag}"
        holder.binding.message.text = bean.message

        holder.binding.info.setTextColor(color)
        holder.binding.message.setTextColor(color)
    }
}
