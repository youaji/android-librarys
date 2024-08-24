package com.youaji.libs.picture.viewer

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RestrictTo
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.youaji.libs.picture.viewer.PictureViewerHelper.OnExitListener
import com.youaji.libs.picture.viewer.PictureViewerHelper.OnOpenListener
import com.youaji.libs.picture.viewer.interfaces.IFindThumbnailView
import com.youaji.libs.picture.viewer.interfaces.OnDismissListener
import com.youaji.libs.picture.viewer.interfaces.OnImageLongClickListener
import com.youaji.libs.picture.viewer.util.SpannableString
import com.youaji.libs.picture.viewer.util.Utils
import com.youaji.libs.picture.viewer.util.notch.CutOutMode
import com.youaji.libs.picture.viewer.util.notch.NotchAdapterUtils

import java.util.Objects

/**
 * 预览界面根布局
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class PictureViewerDialogFragment : DialogFragment() {
    companion object {
        const val FRAGMENT_TAG = "PictureViewer:2024-0628-1430-0000-000000000000"
    }

    var mRootView: FrameLayout? = null
    var mViewPager: NoTouchExceptionViewPager? = null

    private var mLlDotIndicator: LinearLayout? = null
    private var mIvSelectDot: ImageView? = null
    private var mTvTextIndicator: TextView? = null
    private var mLlCustom: FrameLayout? = null

    /**
     * 用于当前Fragment与预览Fragment之间的通讯
     */
    var mShareData: ShareData

    /**
     * 当前展示预览图下标
     */
    private var mCurrentPagerIndex = 0

    /**
     * 是否添加到Activity
     */
    private var mAdd = false

    /**
     * 是否已经Dismiss
     */
    private var mDismiss = false

    /**
     * 界面关闭时是否需要调用[OnDismissListener]
     */
    private var mCallOnDismissListener = true

    /**
     * 是否在当前界面OnDismiss调用[OnDismissListener]
     */
    private var mCallOnDismissListenerInThisOnDismiss = false

    /**
     * 是否自己主动调用Dismiss(包括用户主动关闭、程序主动调用dismiss相关方法)
     */
    private var mSelfDismissDialog: Boolean? = null
    private var mPictureViewerHelper: PictureViewerHelper? = null

    init {
        setCancelable(false)
        // 全屏处理
        setStyle(STYLE_NO_TITLE, 0)
        mShareData = ShareData()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("ObsoleteSdkInt")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            // 说明是被回收后恢复，此时不恢复
            super.onActivityCreated(savedInstanceState)
            return
        }

        if (dialog == null || dialog?.window == null) {
            super.onActivityCreated(null)
            return
        }

        val window = dialog?.window
        // 无论是否全屏显示，都允许内容绘制到耳朵区域
        NotchAdapterUtils.adapter(window, CutOutMode.ALWAYS)
        super.onActivityCreated(null)

        // 以下代码必须在super.onActivityCreated之后调用才有效
        val isParentFullScreen = isParentFullScreen
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            // 需要设置这个才能设置状态栏和导航栏颜色，此时布局内容可绘制到状态栏之下
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor = Color.TRANSPARENT
            window?.navigationBarColor = Color.TRANSPARENT
        }
        val lp = window?.attributes
        lp?.dimAmount = 0f
        lp?.flags = lp?.flags?.or(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        if (mShareData.config.fullScreen == null) {
            // 跟随父窗口
            if (isParentFullScreen) {
                lp?.flags = lp?.flags?.or(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                lp?.flags = lp?.flags?.or(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            }
        }
        lp?.width = WindowManager.LayoutParams.MATCH_PARENT
        lp?.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.setAttributes(lp)

        // 沉浸式处理
        // OPPO ANDROID P 之后的系统需要设置沉浸式配合异形屏适配才能将内容绘制到耳朵区域
        // 防止系统栏隐藏时内容区域大小发生变化
        var uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        // window全屏显示，但状态栏不会被隐藏，状态栏依然可见，内容可绘制到状态栏之下
        uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        // window全屏显示，但导航栏不会被隐藏，导航栏依然可见，内容可绘制到导航栏之下
        uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            // 对于OPPO ANDROID P 之后的系统,一定需要清除此标志，否则异形屏无法绘制到耳朵区域下面
            window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            // 设置之后不会通过触摸屏幕调出导航栏
            // uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE; // 通过系统上滑或者下滑拉出导航栏后不会自动隐藏
            uiFlags = uiFlags or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // 通过系统上滑或者下滑拉出导航栏后会自动隐藏
        }
        if (mShareData.config.fullScreen == null && isParentFullScreen) {
            // 隐藏状态栏
            uiFlags = uiFlags or View.INVISIBLE
        }
        val decorView = window?.decorView
        decorView?.systemUiVisibility = uiFlags
        decorView?.setPadding(0, 0, 0, 0)
        initEvent()
        initViewData()
    }

    /**
     * 初始化是否全屏展示
     */
    fun initFullScreen(start: Boolean) {
        if (mShareData.config.fullScreen == null) {
            return
        }

        val isParentFullScreen = isParentFullScreen
        if (isParentFullScreen == mShareData.config.fullScreen) {
            return
        }

        val dialog = dialog ?: return
        val window = dialog.window ?: return

        if (start) {
            if (mShareData.config.fullScreen == true) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                val decorView = window.decorView
                decorView.systemUiVisibility = decorView.systemUiVisibility or View.INVISIBLE
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            }
        } else if (this.isParentFullScreen) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            val decorView = window.decorView
            decorView.systemUiVisibility = decorView.systemUiVisibility or View.INVISIBLE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            // if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            //     // android13 之后需要主动显示dialog的statusBar，否则缩略图所处activity需要等到预览界面关闭才展示状态栏
            //     // 这样会出现强烈的顿挫感
            //     View decorView = window.getDecorView();
            //     WindowInsetsController controller = decorView.getWindowInsetsController();
            //     controller.show(WindowInsets.Type.statusBars());
            // }
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.view_viewer_root, null) as FrameLayout
            mViewPager = mRootView?.findViewById(R.id.viewpager)
            mLlDotIndicator = mRootView?.findViewById(R.id.ll_dot_indicator_photo_preview)
            mIvSelectDot = mRootView?.findViewById(R.id.iv_select_dot_photo_preview)
            mTvTextIndicator = mRootView?.findViewById(R.id.tv_text_indicator_photo_preview)
            mLlCustom = mRootView?.findViewById(R.id.fl_custom)
        }

        if (mSelfDismissDialog == null && savedInstanceState == null) {
            mDismiss = false
        } else if (savedInstanceState != null || mSelfDismissDialog == false) {
            // 被回收后恢复，则关闭弹窗
            dismissAllowingStateLoss()
        }

        return mRootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mLlCustom?.removeAllViews()
        mRootView?.let {
            val parent = it.parent
            if (parent is ViewGroup) {
                // 为了下次重用mRootView
                parent.removeView(it)
            }
        }
        if (mSelfDismissDialog == null) {
            mSelfDismissDialog = false
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mSelfDismissDialog = null
        mAdd = false
        mDismiss = true
        if (mCallOnDismissListenerInThisOnDismiss && mCallOnDismissListener) {
            mShareData.config.onDismissListener?.onDismiss()
        }
        mShareData.release()
    }

    /**
     * 父窗口是否全屏显示
     */
    private val isParentFullScreen: Boolean
        get() {
            return if (activity == null || activity?.window == null) true
            else activity?.window?.attributes?.flags?.and(WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0 // 跟随打开预览界面的显示状态
        }

    /**
     * 父窗口是否高亮状态栏，此时字体是黑色的
     */
    @get:SuppressLint("ObsoleteSdkInt")
    private val isParentLightStatusBar: Boolean
        get() {
            if (Build.VERSION.SDK_INT < VERSION_CODES.M) {
                return false
            }
            if (activity == null || activity?.window == null) {
                return false
            }
            val decorView = activity?.window?.decorView ?: return false
            // 跟随打开预览界面的显示状态
            return decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR != 0
        }

    /**
     * 父窗口是否高亮状态栏，此时字体是黑色的
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun setLightStatusBar() {
        if (Build.VERSION.SDK_INT < VERSION_CODES.M) {
            return
        }
        val dialog = dialog ?: return
        val window = dialog.window ?: return
        val decorView = window.decorView
        decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    fun show(context: Context, fragmentManager: FragmentManager, config: Config?, thumbnailView: View?) {
        mShareData.applyConfig(config)
        mShareData.findThumbnailView = null
        mShareData.thumbnailView = thumbnailView
        showInner(context, fragmentManager)
    }

    fun show(context: Context, fragmentManager: FragmentManager, config: Config?, findThumbnailView: IFindThumbnailView?) {
        mShareData.applyConfig(config)
        mShareData.thumbnailView = null
        mShareData.findThumbnailView = findThumbnailView
        showInner(context, fragmentManager)
    }

    private fun showInner(context: Context, fragmentManager: FragmentManager) {
        // 预加载启动图图片
        val imageView = PreloadImageView(context)
        val displayMetrics = context.resources.displayMetrics
        val params = ViewGroup.LayoutParams(displayMetrics.widthPixels, displayMetrics.heightPixels)
        imageView.setLayoutParams(params)
        imageView.setDrawableLoadListener { drawable: Drawable? ->
            mShareData.preLoadDrawable = drawable
            val listener = mShareData.preDrawableLoadListener
            listener?.onLoad(drawable)
        }
        loadImage(imageView)
        mSelfDismissDialog = null
        mShareData.showNeedAnim = dialog == null || dialog?.isShowing == false
        if (isStateSaved()) {
            dismissAllowingStateLoss()
        } else if (isAdded || mAdd) {
            // isAdded()并不一定靠谱，可能存在一定的延时性，为此当前对象在创建时，已经优先返回fragmentManager存在的对象
            // 对象获取逻辑查看PhotoPreview getDialog相关方法
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                dismissAllowingStateLoss()
            } else if (mRootView != null) {
                initViewData()
                initEvent()
                return
            }
        }
        mAdd = true
        showNow(fragmentManager, FRAGMENT_TAG)
    }

    /**
     * 加载图片
     */
    private fun loadImage(imageView: ImageView) {
        if (mShareData.config.imageLoader != null) {
            val mPosition = mShareData.config.defaultShowPosition
            val sources = mShareData.config.sources
            if (sources != null && mPosition < sources.size && mPosition >= 0) {
                mShareData.config.imageLoader?.onLoadImage(mPosition, sources[mPosition], imageView)
            } else {
                mShareData.config.imageLoader?.onLoadImage(mPosition, null, imageView)
            }
        }
    }

    /**
     * 退出预览
     *
     * @param callBack 是否需要执行[OnDismissListener]回调
     */
    fun dismiss(callBack: Boolean) {
        if (mSelfDismissDialog != null || mDismiss || !lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            return
        }

        mSelfDismissDialog = true
        mCallOnDismissListener = callBack
        if (mPictureViewerHelper == null) {
            mCallOnDismissListenerInThisOnDismiss = true
            dismissAllowingStateLoss()
        } else {
            if (mPictureViewerHelper?.exit() == false) {
                mCallOnDismissListenerInThisOnDismiss = true
                dismissAllowingStateLoss()
            }
        }
    }

    private fun initViewData() {
        mCurrentPagerIndex = mShareData.config.defaultShowPosition
        mPictureViewerHelper = PictureViewerHelper(this, mCurrentPagerIndex)
        mLlDotIndicator?.visibility = View.GONE
        mIvSelectDot?.setVisibility(View.GONE)
        mTvTextIndicator?.visibility = View.GONE
        setIndicatorVisible(false)
        prepareIndicator()
        prepareViewPager()
    }

    private fun initEvent() {
        mShareData.onOpenListener = object : OnOpenListener {
            override fun onStartPre() {
                if (java.lang.Boolean.TRUE == mShareData.config.fullScreen) {
                    if (isParentLightStatusBar) {
                        setLightStatusBar()
                    }
                }
                if (mShareData.config.openAnimStartHideOrShowStatusBar) {
                    initFullScreen(true)
                }
            }

            override fun onStart() {
                // 对于强制指定是否全屏，需要此处初始化状态栏隐藏逻辑，否则在MIUI系统上，从嵌套多层的Fragment预览会出现卡顿
                mViewPager?.setTouchEnable(false)
            }

            override fun onEnd() {
                if (!mShareData.config.openAnimStartHideOrShowStatusBar) {
                    initFullScreen(true)
                }
                setIndicatorVisible(true)
                mViewPager?.setTouchEnable(true)
            }
        }
        mShareData.onExitListener = object : OnExitListener {
            override fun onStartPre() {
                if (isParentLightStatusBar) {
                    setLightStatusBar()
                }
                if (mShareData.config.exitAnimStartHideOrShowStatusBar) {
                    initFullScreen(false)
                }
            }

            override fun onStart() {
                setIndicatorVisible(false)
                mViewPager?.setTouchEnable(false)
            }

            override fun onExit() {
                if (!mShareData.config.exitAnimStartHideOrShowStatusBar) {
                    initFullScreen(false)
                }
                mViewPager?.setTouchEnable(true)
                if (mSelfDismissDialog != null) {
                    return
                }
                mSelfDismissDialog = true
                val onDismissListener = mShareData.config.onDismissListener
                dismissAllowingStateLoss()
                if (mCallOnDismissListener) {
                    onDismissListener?.onDismiss()
                }
            }
        }
        mShareData.onLongClickListener = OnImageLongClickListener { pos, v ->
            return@OnImageLongClickListener mLlCustom?.let { custom -> mShareData.config.onLongClickListener?.onLongClick(pos, custom, v) ?: false } ?: false
        }
    }

    /**
     * 准备用于展示预览图的ViePager数据
     */
    private fun prepareViewPager() {
        // 每次预览的时候，如果不动态修改每个ViewPager的Id
        // 那么预览多张图片时，如果第一次点击位置1预览然后关闭，再点击位置2，预览图片打开的还是位置1预览图
        mViewPager?.setTouchEnable(false)

        if (mViewPager?.id == R.id.view_pager_id) {
            mViewPager?.setId(R.id.view_pager_id_next)
        } else {
            mViewPager?.setId(R.id.view_pager_id)
        }

        val adapter = mPictureViewerHelper?.let { helper -> ImagePagerAdapter(helper, mShareData) } ?: return
        mViewPager?.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                mLlDotIndicator?.let { view ->
                    if (view.visibility == View.VISIBLE) {
                        val childAt0 = view.getChildAt(0)
                        val childAt1 = view.getChildAt(1)
                        val dx = childAt1.x - childAt0.x
                        mIvSelectDot?.translationX = position * dx + positionOffset * dx
                    }
                }
                mShareData.config.onPageChangeListener?.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                mCurrentPagerIndex = position
                mPictureViewerHelper?.setPosition(position)

                // 设置文字版本当前页的值
                if (mTvTextIndicator?.visibility == View.VISIBLE) {
                    updateTextIndicator()
                }
                mShareData.config.onPageChangeListener?.onPageSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                mShareData.config.onPageChangeListener?.onPageScrollStateChanged(state)
            }
        })
        mViewPager?.setAdapter(adapter)
        mViewPager?.setCurrentItem(mCurrentPagerIndex)
    }

    /**
     * 准备滑动指示器数据
     */
    private fun prepareIndicator() {
        val sourceSize = mShareData.config.sources?.size ?: 0
        if (sourceSize >= 2 && sourceSize <= mShareData.config.maxIndicatorDot && IndicatorType.DOT == mShareData.config.indicatorType) {
            mLlDotIndicator?.removeAllViews()
            val context = requireContext()
            if (mShareData.config.selectIndicatorColor != -0x1) {
                val drawable = mIvSelectDot?.getDrawable()
                val gradientDrawable =
                    if (drawable is GradientDrawable) drawable
                    else ContextCompat.getDrawable(context, R.drawable.shape_dot_selected) as GradientDrawable
                Objects.requireNonNull(gradientDrawable).setColorFilter(mShareData.config.selectIndicatorColor, PorterDuff.Mode.SRC_OVER)
                mIvSelectDot?.setImageDrawable(gradientDrawable)
            }
            val dotParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // 未选中小圆点的间距
            dotParams.rightMargin = Utils.dp2px(context, 12)

            // 创建未选中的小圆点
            for (i in 0 until sourceSize) {
                val iv = AppCompatImageView(context)
                val shapeDrawable = ContextCompat.getDrawable(context, R.drawable.shape_dot_unselected) as GradientDrawable?
                if (mShareData.config.normalIndicatorColor != -0x555556) {
                    shapeDrawable?.setColorFilter(mShareData.config.normalIndicatorColor, PorterDuff.Mode.SRC_OVER)
                }
                iv.setImageDrawable(shapeDrawable)
                iv.setLayoutParams(dotParams)
                mLlDotIndicator?.addView(iv)
            }
            mLlDotIndicator?.post {
                val childAt = mLlDotIndicator?.getChildAt(0) ?: return@post
                val params = mIvSelectDot?.layoutParams as FrameLayout.LayoutParams
                // 设置选中小圆点的左边距
                params.leftMargin = childAt.x.toInt()
                mIvSelectDot?.setLayoutParams(params)
                val tx = (dotParams.rightMargin * mCurrentPagerIndex + childAt.width * mCurrentPagerIndex).toFloat()
                mIvSelectDot?.translationX = tx
            }
        } else if (sourceSize > 1) {
            updateTextIndicator()
        }
    }

    private fun setIndicatorVisible(visible: Boolean) {
        val sourceSize = mShareData.config.sources?.size ?: 0
        if (sourceSize >= 2 && sourceSize <= mShareData.config.maxIndicatorDot && IndicatorType.DOT == mShareData.config.indicatorType) {
            val visibility = if (visible) View.VISIBLE else View.INVISIBLE
            mLlDotIndicator?.visibility = visibility
            mIvSelectDot?.setVisibility(visibility)
            mTvTextIndicator?.visibility = View.GONE
        } else if (sourceSize > 1) {
            mLlDotIndicator?.visibility = View.GONE
            mIvSelectDot?.setVisibility(View.GONE)
            mTvTextIndicator?.visibility = if (visible) View.VISIBLE else View.GONE
        } else {
            mLlDotIndicator?.visibility = View.GONE
            mIvSelectDot?.setVisibility(View.GONE)
            mTvTextIndicator?.visibility = View.GONE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (mLlDotIndicator?.visibility == View.VISIBLE) {
            mLlDotIndicator?.getViewTreeObserver()?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mLlDotIndicator?.getViewTreeObserver()?.removeOnGlobalLayoutListener(this)
                    val childAt0 = mLlDotIndicator?.getChildAt(0) ?: return
                    val params = mIvSelectDot?.layoutParams as FrameLayout.LayoutParams
                    // 设置选中小圆点的左边距
                    params.leftMargin = childAt0.x.toInt()
                    mIvSelectDot?.setLayoutParams(params)
                    val layoutParams = childAt0.layoutParams as LinearLayout.LayoutParams
                    val tx = (layoutParams.rightMargin * mCurrentPagerIndex + childAt0.width * mCurrentPagerIndex).toFloat()
                    mIvSelectDot?.translationX = tx
                }
            })
        }
    }

    private fun updateTextIndicator() {
        val sourceSize = mShareData.config.sources?.size ?: 0
        mTvTextIndicator?.let { textView ->
            SpannableString.Builder.appendMode()
                .addSpan((mCurrentPagerIndex + 1).toString())
                .color(mShareData.config.selectIndicatorColor)
                .addSpan(" / $sourceSize")
                .color(mShareData.config.normalIndicatorColor)
                .apply(textView)
        }
    }
}
