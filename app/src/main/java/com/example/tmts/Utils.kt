package com.example.tmts

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

object Utils {
    fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun detectSwipe(context: Context, onSwipe: (String) -> Unit): View.OnTouchListener {
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 50
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null || e2 == null) return false

                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                return if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipe("MOVE_RIGHT")
                            Log.d("direction", "move right")
                        } else {
                            onSwipe("MOVE_LEFT")
                            Log.d("direction", "move left")

                        }
                        true
                    } else {
                        false
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipe("MOVE_DOWN")
                        } else {
                            onSwipe("MOVE_UP")
                        }
                        true
                    } else {
                        false
                    }
                }
            }
        })

        return View.OnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }


}