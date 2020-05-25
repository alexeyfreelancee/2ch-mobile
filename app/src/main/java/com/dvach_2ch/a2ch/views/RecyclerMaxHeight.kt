package com.dvach_2ch.a2ch.views

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.dvach_2ch.a2ch.R
import java.security.MessageDigest

class RecyclerMaxHeight : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        init(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyle:Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    var maxHeight = 0

    private fun init(context: Context,attrs: AttributeSet?){
        val arr = context.obtainStyledAttributes(attrs, R.styleable.RecyclerMaxHeight)
        maxHeight = arr.getLayoutDimension(R.styleable.RecyclerMaxHeight_maxHeight, maxHeight)
        arr.recycle()
    }
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
       if(maxHeight > 0){
           super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(maxHeight,MeasureSpec.AT_MOST))
       } else {
           super.onMeasure(widthSpec, heightSpec)
       }
    }
}