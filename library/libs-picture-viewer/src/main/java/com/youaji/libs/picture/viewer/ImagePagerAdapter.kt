package com.youaji.libs.picture.viewer

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.RectF
import android.os.Build
import android.os.Build.VERSION_CODES
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import com.youaji.libs.picture.viewer.PictureViewerHelper.OnExitListener
import com.youaji.libs.picture.viewer.PictureViewerHelper.OnOpenListener
import kotlin.math.ceil

/**
 * 预览图片适配器
 */
class ImagePagerAdapter(
    private val mHelper: PictureViewerHelper,
    private val ShareData: ShareData
) : PagerAdapter() {
    override fun getCount(): Int =
        ShareData.config.sources?.size ?: 0

    override fun isViewFromObject(view: View, any: Any): Boolean =
        any is ViewHolder && view === any.root

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return ViewHolder(mHelper, ShareData, container, position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        val holder = any as ViewHolder
        holder.destroy()
        container.removeView(holder.root)
    }

    override fun getItemPosition(any: Any): Int =
        if (getCount() == 0) POSITION_NONE
        else POSITION_UNCHANGED

    internal class ViewHolder @SuppressLint("InflateParams") constructor(
        private val helper: PictureViewerHelper,
        private val shareData: ShareData,
        container: ViewGroup,
        position: Int
    ) {

        var root: View
        val pictureView: PictureView
        val loading: ProgressBar

        // 记录预览界面图片缩放倍率为1时图片真实绘制大小
        val noScaleImageActualSize = FloatArray(2)

        private var openListener: OnOpenListener? = null
        private var exitListener: OnExitListener? = null

        init {
            root = LayoutInflater.from(container.context).inflate(R.layout.fragment_viewer, container, false)
            container.addView(root)
            root.tag = position
            root.setTag(R.id.view_holder, this)
            pictureView = root.findViewById(R.id.photoView)
            loading = root.findViewById(R.id.loading)
            setPhotoViewVisibility()
            pictureView.setPhotoPreviewHelper(helper)
            pictureView.setStartView(position == 0)
            val sources = shareData.config.sources
            val size = sources?.size ?: 0
            pictureView.setEndView(position == size - 1)
            initEvent(position)
            initLoading()
            loadImage(pictureView, position)
        }

        /**
         * 根据预览动画设置大图显示与隐藏
         */
        private fun setPhotoViewVisibility() {
            if (helper.isOpenAnimEnd) {
                pictureView.setVisibility(View.VISIBLE)
            }
            openListener = object : OnOpenListener {
                override fun onStartPre() {}
                override fun onStart() {
                    pictureView.setVisibility(View.INVISIBLE)
                }

                override fun onEnd() {
                    pictureView.setVisibility(View.VISIBLE)
                }
            }
            helper.addOnOpenListener(openListener)
            exitListener = object : OnExitListener {
                override fun onStartPre() {}
                override fun onStart() {
                    pictureView.setVisibility(View.INVISIBLE)
                }

                override fun onExit() {}
            }
            helper.addOnExitListener(exitListener)
        }

        internal fun destroy() {
            root.tag = null
            helper.removeOnOpenListener(openListener)
            helper.removeOnExitListener(exitListener)
        }

        private fun initEvent(position: Int) {
            pictureView.setOnLongClickListener { shareData.onLongClickListener?.onLongClick(position, pictureView) ?: true }
            pictureView.setOnClickListener { helper.exit() }
        }

        /**
         * 初始化loading
         */
        @SuppressLint("ObsoleteSdkInt")
        private fun initLoading() {
            pictureView.setOnMatrixChangeListener { rectF -> getPreviewDrawableSize(rectF) }
            pictureView.setImageChangeListener { drawable -> loading.isGone = drawable != null }

            if (shareData.config.delayShowProgressTime < 0) {
                loading.isGone = true
                return
            }

            if (shareData.config.progressDrawable != null) {
                loading.indeterminateDrawable = shareData.config.progressDrawable
            }

            if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                shareData.config.progressColor?.let { color -> loading.setIndeterminateTintList(ColorStateList.valueOf(color)) }
            }

            // loading.setVisibility(!helper.isAnimStart() && shareData.config.delayShowProgressTime == 0 ? View.VISIBLE : View.GONE);
            loading.isVisible = shareData.config.delayShowProgressTime == 0L

            if (shareData.config.delayShowProgressTime > 0) {
                // 监听指定延迟后图片是否加载成功
                pictureView.postDelayed({ loading.isVisible = pictureView.getDrawable() == null }, shareData.config.delayShowProgressTime)
            }
        }

        /**
         * 获取预览图片真实大小，由于图片刚进入时，需要等待绘制，所以可能不能及时获取到准确的大小
         */
        private fun getPreviewDrawableSize(rectF: RectF) {
            if (pictureView.scale != 1f) {
                return
            }

            // 用于退出时计算移动后最终图像坐标使用
            // 刚设置图片就获取，此时可能获取不成功
            noScaleImageActualSize[0] = rectF.width()
            noScaleImageActualSize[1] = rectF.height()
            if (noScaleImageActualSize[0] > 0) {
                // 计算最大缩放倍率，屏幕大小的三倍
                val ceil = ceil((root.width / noScaleImageActualSize[0]).toDouble())
                val maxScale = (ceil * 3f).toFloat()
                if (maxScale < pictureView.maximumScale) {
                    return
                }
                val midScale = (maxScale + pictureView.minimumScale) / 2
                pictureView.setScaleLevels(pictureView.minimumScale, midScale, maxScale)
            }
        }

        /**
         * 加载图片
         */
        private fun loadImage(imageView: ImageView, position: Int) {
            shareData.config.imageLoader?.let { loader ->
                val sources = shareData.config.sources
                if (sources != null && position < sources.size && position >= 0) {
                    loader.onLoadImage(position, sources[position], imageView)
                } else {
                    loader.onLoadImage(position, null, imageView)
                }
            }
        }
    }
}
