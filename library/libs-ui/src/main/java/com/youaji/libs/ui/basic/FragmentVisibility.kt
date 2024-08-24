package com.youaji.libs.ui.basic

interface FragmentVisibility {
    /**
     * 当 Fragment 可见时调用。
     */
    fun onVisible() {}

    /**
     * 当 Fragment 不可见时调用。
     */
    fun onInvisible() {}

    /**
     * 当 Fragment 第一次可见时调用。
     */
    fun onVisibleFirst() {}

    /**
     * 当 Fragment 除第一次外可见时调用。
     */
    fun onVisibleExceptFirst() {}

    /**
     * 如果 Fragment 当前对用户可见，则返回 true。
     */
    fun isVisibleToUser(): Boolean

    /**
     * 设置 Fragment 可见
     */
    fun setFragmentVisible()

    /**
     * 设置 Fragment 不可见
     */
    fun setFragmentInvisible()
}