package com.dvach_2ch.a2ch.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.log
import kotlin.math.max
import kotlin.math.roundToInt

class RecyclerFastScroll(private val rv: RecyclerView,
                         private val colorNormal: Int,
                         private val colorPressed: Int)
    : RecyclerView.ItemDecoration(), RecyclerView.OnItemTouchListener{
    companion object {
        // Scroll thumb not showing
        private const val STATE_HIDDEN = 0
        // Scroll thumb visible and moving along with the scrollbar
        private const val STATE_VISIBLE = 1
        // Scroll thumb being dragged by user
        private const val STATE_DRAGGING = 2
        private const val ANIMATION_STATE_OUT = 0
        private const val ANIMATION_STATE_FADING_IN = 1
        private const val ANIMATION_STATE_IN = 2
        private const val ANIMATION_STATE_FADING_OUT = 3

        private const val SHOW_DURATION_MS = 200
        private const val HIDE_DURATION_MS = 500
        private const val HIDE_DELAY_AFTER_VISIBLE_MS = 1500
        private const val HIDE_DELAY_AFTER_DRAGGING_MS = 3000
    }

    private fun convertDpToPx(dp: Int) = (dp.toFloat() * rv.resources.displayMetrics.density).roundToInt()

    private val thumbWidth = convertDpToPx(8)
    private val touchWidth = convertDpToPx(24)
    private val touchMinHeight = convertDpToPx(48)

    private var thumbY = 0
    private var thumbHeight = 0
    private var thumbScrollRange = 0f

    private var rvWidth = 0
    private var rvHeight = 0
    private var scrollableRange = 0 // real scroll range without rv height

    private var state = STATE_HIDDEN
    private var needRecompute = true

    private val layoutManager = rv.layoutManager as LinearLayoutManager

    private var alpha = 1f
    private val showHideAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        addListener(AnimatorListener())
        addUpdateListener(AnimatorUpdater())
    }
    private var animationState = ANIMATION_STATE_OUT
    private val hideRunnable = Runnable {
        hide(HIDE_DURATION_MS)
    }

    private inner class AnimatorUpdater : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(a: ValueAnimator) {
            alpha = a.animatedValue as Float
            requestRedraw()
        }
    }

    init {
        rv.addItemDecoration(this)
        rv.addOnItemTouchListener(this)
        rv.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                computeScroll()
            }
        })
        rv.adapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            private fun update(){
                needRecompute = true
            }
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = update()
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = update()
        })
    }

    private fun requestRedraw() = rv.invalidate()

    private fun setState(st: Int) {
        if (st == STATE_HIDDEN) {
            requestRedraw()
        } else {
            show()
        }
        if (state == STATE_DRAGGING && st != STATE_DRAGGING) {
            resetHideDelay(HIDE_DELAY_AFTER_DRAGGING_MS)
        } else if (st == STATE_VISIBLE) {
            resetHideDelay(HIDE_DELAY_AFTER_VISIBLE_MS)
        }
        state = st
    }

    private fun show() {
        with(showHideAnimator) {
            if (animationState == ANIMATION_STATE_FADING_OUT) {
                cancel()
                animationState = ANIMATION_STATE_OUT
            }
            if (animationState == ANIMATION_STATE_OUT) {
                animationState = ANIMATION_STATE_FADING_IN
                setFloatValues(alpha, 1f)
                duration = SHOW_DURATION_MS.toLong()
                startDelay = 0
                start()
            }
        }
    }

    private fun hide(delay: Int) {
        with(showHideAnimator) {
            if (animationState == ANIMATION_STATE_FADING_IN) {
                cancel()
                animationState = ANIMATION_STATE_IN
            }
            if (animationState == ANIMATION_STATE_IN) {
                animationState = ANIMATION_STATE_FADING_OUT
                setFloatValues(alpha, 0f)
                duration = delay.toLong()
                start()
            }
        }
    }

    private fun cancelHide() {
        rv.removeCallbacks(hideRunnable)
    }

    private fun resetHideDelay(delay: Int) {
        cancelHide()
        rv.postDelayed(hideRunnable, delay.toLong())
    }


    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, st: RecyclerView.State) {
        if (rvWidth != rv.width || rvHeight != rv.height) {
            rvWidth = rv.width
            rvHeight = rv.height
            // This is due to the different events ordering when keyboard is opened or
            // retracted vs rotate. Hence to avoid corner cases we just disable the
            // scroller when size changed, and wait until the scroll position is recomputed
            // before showing it back.
            setState(STATE_HIDDEN)
            return
        }
        if(needRecompute) computeScroll()
        if (animationState!=ANIMATION_STATE_OUT && state!=STATE_HIDDEN) {
            with(canvas){
                save()
                val left = rvWidth - thumbWidth
                val top = thumbY
                clipRect(left, top, left+thumbWidth, top+thumbHeight)


                var color = if (state == STATE_DRAGGING) colorPressed else colorNormal
                if(alpha!=1f){
                    color = (color and 0xffffff) or (((color ushr 24)*alpha).toInt() shl 24)
                }

                drawColor(color)
                restore()
            }
        }
    }

    private var lastTouchY = 0f

    private fun computeScroll() {
        needRecompute = false
        val scrollOffset = rv.computeVerticalScrollOffset()
        val scrollRange = rv.computeVerticalScrollRange()
        scrollableRange = scrollRange - rvHeight
        if(scrollableRange>0) {
            val ratio = scrollOffset.toFloat() / scrollableRange
            thumbHeight = max((rvHeight.toFloat() / scrollRange * rvHeight).toInt(), touchMinHeight)

            val tr = rvHeight - thumbHeight
            if (tr > 0) {
                thumbScrollRange = tr.toFloat()
                thumbY = (ratio * thumbScrollRange).toInt()
                if (state == STATE_HIDDEN || state == STATE_VISIBLE) {
                    setState(STATE_VISIBLE)
                }
                return
            }
        }
        setState(STATE_HIDDEN)
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, me: MotionEvent): Boolean {
        return when (me.action) {
            MotionEvent.ACTION_DOWN-> if (state == STATE_VISIBLE) {
                val insideVerticalThumb = isPointInsideVerticalThumb(me.x, me.y)
                if (insideVerticalThumb) {
                    cancelHide()
                    setState(STATE_DRAGGING)
                    requestRedraw()
                    lastTouchY = me.y
                    true
                } else {
                    false
                }
            }else false
            MotionEvent.ACTION_MOVE-> state == STATE_DRAGGING
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if(state == STATE_DRAGGING){
                setState(STATE_VISIBLE)
                requestRedraw()
                true
            }else false
            else-> false
        }
    }

    override fun onTouchEvent(rv: RecyclerView, me: MotionEvent) {
        if (state == STATE_HIDDEN) {
            return
        }
        when(me.action){
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onInterceptTouchEvent(rv, me)
            MotionEvent.ACTION_MOVE-> if(state == STATE_DRAGGING && thumbScrollRange>0) {
                show()
                val y = me.y
                val ratio = (y - lastTouchY) / thumbScrollRange
                lastTouchY = y
//                scrollItems(ratio)
                scrollPixels(ratio)
            }
        }
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    private fun scrollItems(ratio: Float){
        val topI = layoutManager.findFirstVisibleItemPosition()
        val visRange = layoutManager.findLastVisibleItemPosition() + 1 - topI
        val posRange = rv.adapter!!.itemCount - visRange
        val d = (posRange * ratio).roundToInt()
        layoutManager.scrollToPositionWithOffset(topI + d, 0)
    }

    private fun scrollPixels(ratio: Float) {
        val d = (ratio * scrollableRange).toInt()
//        profile("scrollBY $d") {
        if (abs(d) > rvHeight * 1){
            scrollItems(ratio)
        }else
            rv.scrollBy(0, d)
//        }
    }

    private fun isPointInsideVerticalThumb(x: Float, y: Float): Boolean {
        return ((x >= rvWidth - touchWidth)
                && y>=thumbY
                && y<(thumbY+thumbHeight))
    }

    private inner class AnimatorListener : AnimatorListenerAdapter() {
        private var canceled = false
        override fun onAnimationEnd(animation: Animator) {
            // cancel is always followed by a new directive, so don't update state
            if (canceled) {
                canceled = false
                return
            }
            if (showHideAnimator.animatedValue as Float == 0f) {
                animationState = ANIMATION_STATE_OUT
                setState(STATE_HIDDEN)
            } else {
                animationState = ANIMATION_STATE_IN
                requestRedraw()
            }
        }
        override fun onAnimationCancel(animation: Animator) {
            canceled = true
        }
    }
}