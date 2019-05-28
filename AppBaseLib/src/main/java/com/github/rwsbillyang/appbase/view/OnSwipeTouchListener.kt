package com.github.rwsbillyang.appbase.view

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.github.rwsbillyang.appbase.util.logw

/**
<pre>
myView.setOnTouchListener(object : OnSwipeTouchListener(context) {

override fun onSwipeTop() {
super.onSwipeTop()
}

override fun onSwipeBottom() {
super.onSwipeBottom()
}

override fun onSwipeLeft() {
super.onSwipeLeft()
}

override fun onSwipeRight() {
super.onSwipeRight()
}
})
</pre>
 *
 * https://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
 * */
open class OnSwipeTouchListener(ctx: Context) : View.OnTouchListener {

    private val gestureDetector: GestureDetector

    companion object {
        private val SWIPE_THRESHOLD = 200
        private val SWIPE_VELOCITY_THRESHOLD = 200
    }

    init {
        gestureDetector = GestureDetector(ctx, GestureListener())
    }
    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *        the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
    open fun onSwipeRight() = false

    open fun onSwipeLeft() = false

    open fun onSwipeTop() = false

    open fun onSwipeBottom() = false



    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        /**
         *  @return true if the event is consumed, else false
         * */
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            result = onSwipeRight()
                        } else {
                            result = onSwipeLeft()
                        }
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        result = onSwipeBottom()
                    } else {
                        result = onSwipeTop()
                    }
                }
            } catch (e: Exception) {
                //e.printStackTrace()
                logw("exception: ${e.message}")
            }

            return result
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        // Return false if we're scrolling in the x direction
//        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY:Float):Boolean {
//            return try {
//                Math.abs(distanceY) > Math.abs(distanceX)
//            } catch (e: Exception) {
//                // nothing
//                false
//            }
//        }
    }


}