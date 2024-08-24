package com.youaji.libs.picture.viewer.util

import android.graphics.Typeface
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt

/**
 * 构建富文本
 */
class SpannableString(source: CharSequence?) : android.text.SpannableString(source) {
    open class AppendBuilder {
        private var spanList: MutableList<AppendSpan> = ArrayList()

        /**
         * 添加一个Span
         *
         * @param spanStr 需要处理的文本，根据 addSpan 顺序拼接到整个文本内容中
         * @return 如果数据无效，则返回一个无效的 Span节点，仅仅为了流式调用，实际执行时跳过
         */
        open fun addSpan(spanStr: String): AppendSpan {
            val span: AppendSpan
            if (spanStr.isNotEmpty()) {
                span = AppendSpan(this, spanStr)
                spanList.add(span)
            } else {
                span = AppendSpan(this)
            }
            return span
        }

        /**
         * 应用SpannableString至目标
         *
         * @param textView 展示内容的 TextView 或子对象，如果设置的 span 中包含点击项，
         * 则将调用 [TextView.setMovementMethod],参数为 LinkMovementMethod 实例
         */
        open fun <T : TextView?> apply(textView: T?) {
            if (textView == null) {
                return
            }
            if (spanList.size == 0) {
                textView.text = null
                return
            }
            val builder = StringBuilder()
            for (span in spanList) {
                builder.append(span.spanStr)
            }
            val ss = SpannableString(builder.toString())
            var needClick = false
            var start = 0
            for (span in spanList) {
                if (span.couldClick) {
                    needClick = true
                }
                val end = start + span.spanStr.length
                ss.setSpan(SpanImpl(span), start, end, SPAN_INCLUSIVE_EXCLUSIVE)
                start = end
            }
            if (needClick) {
                textView.movementMethod = LinkMovementMethod.getInstance()
            }
            textView.text = ss
            spanList.clear()
        }

        /**
         * 构建 SpannableString，如果构建对象中有可点击 Span，请确保应用该文本的 [TextView] 或子类设置了可点击的 MovementMethod。
         * 否则推荐使用 [apply] 方法
         */
        open fun create(): SpannableString {
            if (spanList.size == 0) {
                return SpannableString("")
            }
            val builder = StringBuilder()
            for (span in spanList) {
                builder.append(span.spanStr)
            }
            val ss = SpannableString(builder.toString())
            var start = 0
            for (span in spanList) {
                val end = start + span.spanStr.length
                ss.setSpan(SpanImpl(span), start, end, SPAN_INCLUSIVE_EXCLUSIVE)
                start = end
            }
            spanList.clear()
            return ss
        }
    }

    /**
     * 追加模式，所有Span以拼接方式最终展示到目标View上
     */
    open class AppendSpan internal constructor(
        protected open val builder: AppendBuilder,
        var spanStr: String = ""
    ) {

        companion object {
            const val INVALID_VALUE = -1
        }

        var size = INVALID_VALUE
        var color = INVALID_VALUE
        var couldClick = false
        var clickListener: OnSpanClickListener? = null
        var underLine = false
        var typeface: Typeface? = null

        /**
         * 文本字体大小,不指定不调用此方法或传 [AppendSpan.INVALID_VALUE]
         */
        fun size(size: Int): AppendSpan {
            this.size = size
            return this
        }

        fun typeface(typeface: Typeface?): AppendSpan {
            this.typeface = typeface
            return this
        }

        /**
         * 文本字体颜色，不指定不调用此方法或传 [AppendSpan.INVALID_VALUE]
         */
        fun color(@ColorInt color: Int): AppendSpan {
            this.color = color
            return this
        }

        /**
         * 文本是否可点击
         */
        fun couldClick(cloudClick: Boolean): AppendSpan {
            couldClick = cloudClick
            return this
        }

        /**
         * 文本是否需要展示下划线
         */
        fun underLine(underLine: Boolean): AppendSpan {
            this.underLine = underLine
            return this
        }

        /**
         * 文本点检监听，需要 [couldClick] 设置为 true
         */
        fun clickListener(listener: OnSpanClickListener?): AppendSpan {
            clickListener = listener
            return this
        }

        /**
         * 添加一个 Span
         *
         * @param spanStr 需要处理的文本，根据 addSpan 顺序拼接到整个文本内容中
         * @return 如果数据无效，则返回一个无效的 Span 节点，仅仅为了流式调用，实际执行时跳过
         */
        open fun addSpan(spanStr: String): AppendSpan {
            return builder.addSpan(spanStr)
        }

        /**
         * 应用SpannableString至目标
         *
         * @param textView 展示内容的 TextView 或子对象，如果设置的span中包含点击项，
         * 则将调用 [TextView.setMovementMethod],参数为 LinkMovementMethod 实例
         */
        fun <T : TextView?> apply(textView: T) {
            builder.apply<T>(textView)
        }

        /**
         * 构建 SpannableString，如果构建对象中有可点击 Span，请确保应用该文本的 [TextView] 或子类设置了可点击的 MovementMethod。
         * 否则推荐使用 [apply] 方法
         */
        fun create(): SpannableString {
            return builder.create()
        }
    }

    /**
     * SpannableString构建器
     * @param source 构建的 SpannableString 需要展示的原始文本，还未进行处理过.如果为空串""，则不处理
     */
    class Builder(
        private var source: String = "",
        vararg args: Any,
    ) : AppendBuilder() {

        companion object {
            /**
             * 以追加模式进行构建
             */
            fun appendMode(): AppendBuilder {
                return AppendBuilder()
            }

            /**
             * @param source 构建的SpannableString需要展示的原始文本，还未进行处理过.如果为空串""，则不处理
             */
            fun string(source: String): Builder {
                return Builder(source)
            }

            /**
             * @param source 构建的SpannableString需要展示的原始文本，还未进行处理过.如果为空串""，则不处理
             * @param args   source格式化参数
             */
            fun string(source: String, vararg args: Any): Builder {
                return Builder(source, *args)
            }
        }

        private var spans = mutableListOf<Span>()

        init {
            if (isSourceValid && args.isNotEmpty()) {
                this.source = String.format(this.source, *args)
            }
        }

        private val isSourceValid: Boolean
            get() = source.isNotEmpty()

        /**
         * 添加一个Span，描述 [string] 或 [string] 参数 source 中某一段文本应该怎么显示
         *
         * @param start 文本在 source 中的开始位置，如果 < 0 || > （end || source.length()) 则不处理
         * @param end   文本在 source 中的结束位置，如果 < 0 || > (start || source.length()) 则不处理
         * @return 如果数据无效，则返回一个无效的 Span 节点，仅仅为了流式调用，实际执行时跳过
         */
        fun addSpan(start: Int, end: Int): Span {
            val span: Span
            if (isSourceValid && start >= 0 && start <= end && start <= source.length) {
                val spanStr = source.substring(start, end)
                span = Span(this, spanStr, start, end)
                spans.add(span)
            } else {
                span = Span(this, "", AppendSpan.INVALID_VALUE, AppendSpan.INVALID_VALUE)
            }
            return span
        }

        /**
         * 添加一个Span，描述[.string] 或 [.Builder]参数source中某一段文本应该怎么显示
         *
         * @param spanStr source文本中指定需要处理的片段文本内容，如果 source 中不存在 spanStr，则不处理。
         * 请确保 source 不会多次出现 spanStr 文本，否则请使用 [addSpan] 明确指明区间
         * @return 如果数据无效，则返回一个无效的 Span 节点，仅仅为了流式调用，实际执行时跳过
         */
        override fun addSpan(spanStr: String): Span {
            val span: Span
            if (isSourceValid && spanStr.isNotEmpty() && source.contains(spanStr)) {
                val index = source.indexOf(spanStr)
                span = Span(this, spanStr, index, index + spanStr.length)
                span.spanStr = spanStr
                spans.add(span)
            } else {
                span = Span(this, "", AppendSpan.INVALID_VALUE, AppendSpan.INVALID_VALUE)
            }
            return span
        }

        /**
         * 应用SpannableString至目标
         *
         * @param textView 展示内容的TextView或子对象，如果设置的span中包含点击项，
         * 则将调用[TextView.setMovementMethod],参数为LinkMovementMethod实例
         */
        override fun <T : TextView?> apply(textView: T?) {
            if (textView == null) {
                return
            }
            if (!isSourceValid || spans.size == 0) {
                textView.text = null
                return
            }
            val ss = SpannableString(source)
            var needClick = false
            for (span in spans) {
                if (span.couldClick) {
                    needClick = true
                }
                ss.setSpan(SpanImpl(span), span.start, span.end, SPAN_INCLUSIVE_EXCLUSIVE)
            }
            if (needClick) {
                textView.movementMethod = LinkMovementMethod.getInstance()
            }
            textView.text = ss
            spans.clear()
            source = ""
        }

        /**
         * 构建SpannableString，如果构建对象中有可点击Span，请确保应用该文本的[TextView]或子类设置了可点击的MovementMethod。
         * 否则推荐使用[.apply]方法
         */
        override fun create(): SpannableString {
            if (!isSourceValid) {
                return SpannableString("")
            }
            val ss = SpannableString(source)
            for (span in spans) {
                ss.setSpan(SpanImpl(span), span.start, span.end, SPAN_INCLUSIVE_EXCLUSIVE)
            }
            spans.clear()
            source = ""
            return ss
        }
    }

    class Span internal constructor(
        override val builder: Builder,
        spanStr: String,
        val start: Int,
        val end: Int,
    ) : AppendSpan(builder, spanStr) {

        /**
         * 添加一个Span，描述[Builder.string] 或 [Builder.string]参数 source 中某一段文本应该怎么显示
         *
         * @param start 文本在 source 中的开始位置，如果 < 0 || > （end || source.length()) 则不处理
         * @param end   文本在 source 中的结束位置，如果 < 0 || > (start || source.length()) 则不处理
         * @return 如果数据无效，则返回一个无效的 Span 节点，仅仅为了流式调用，实际执行时跳过
         */
        fun addSpan(start: Int, end: Int): Span {
            return builder.addSpan(start, end)
        }

        /**
         * 添加一个Span，描述 [Builder.string] 或 [Builder.string] 参数 source 中某一段文本应该怎么显示
         *
         * @param spanStr source 文本中指定需要处理的片段文本内容，如果 source 中不存在 spanStr，则不处理.
         * 请确保 source 不会多次出现 spanStr 文本，否则请使用 [addSpan] 明确指明区间
         * @return 如果数据无效，则返回一个无效的Span节点，仅仅为了流式调用，实际执行时跳过
         */
        override fun addSpan(spanStr: String): Span {
            return builder.addSpan(spanStr)
        }
    }

    /**
     * 用于实现 [AppendSpan] 中设置的内容
     */
    internal class SpanImpl(private val span: AppendSpan) : ClickableSpan() {
        override fun onClick(widget: View) {
            if (span.couldClick) {
                span.clickListener?.onClick(widget, span.spanStr)
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            if (span.size != AppendSpan.INVALID_VALUE) {
                ds.textSize = span.size.toFloat()
            }
            if (span.typeface != null) {
                ds.setTypeface(span.typeface)
            }
            if (span.color != AppendSpan.INVALID_VALUE) {
                ds.setColor(span.color)
            }
            ds.isUnderlineText = span.underLine
        }
    }

    /**
     * Span点击监听
     */
    interface OnSpanClickListener {
        /**
         * @param view    被点击文本当前应用的View对象
         * @param spanStr 点检区域文本
         */
        fun onClick(view: View, spanStr: String)
    }
}
