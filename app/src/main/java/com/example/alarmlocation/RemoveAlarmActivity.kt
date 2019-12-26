package com.example.alarmlocation

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.alarmlocation.workers.RingtoneWorker
import com.example.alarmlocation.workers.VibrationWorker
import kotlinx.coroutines.GlobalScope


class RemoveAlarmActivity : AppCompatActivity(), View.OnTouchListener {

    private var gestureDetector: MyGestureDetector? = null
    private var workManager: WorkManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MaterialComponents_NoActionBar)
        setContentView(R.layout.show_alarm)
        findViewById<ImageView>(R.id.swipe_image).also { img ->
            img.setOnTouchListener(this)
            gestureDetector = MyGestureDetector(
                this, GestureListener(
                    img, this
                )
            )
        }

        workManager = WorkManager.getInstance(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager =
                getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        workManager?.beginUniqueWork(
            "VibrateWorker",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.from(VibrationWorker::class.java)
        )?.enqueue()
        workManager?.beginUniqueWork(
            "RingtoneWorker",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.from(RingtoneWorker::class.java)
        )?.enqueue()

    }

    override fun onDestroy() {
        workManager?.cancelUniqueWork("VibrateWorker")
        workManager?.cancelUniqueWork("RingtoneWorker")
        super.onDestroy()
    }

    override fun onTouch(p0: View?, p1: MotionEvent): Boolean {
        return gestureDetector?.onTouchEvent(p1) ?: false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if( event?.action == MotionEvent.ACTION_UP) gestureDetector?.myGestureListener?.onUp(event)
        return super.onTouchEvent(event)
    }

    class GestureListener(private val to_move : View, private val activity: Activity )
        : SimpleOnGestureListener(), MyGestureDetector.OnUp {

        val workManager = WorkManager.getInstance(activity)

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            val diffX = e2.x - e1.x
            val intArray = IntArray(2)
            to_move.getLocationInWindow(intArray)

                if (diffX > 0) {
                    if( -1080F + intArray[0] + 200F <= 0 )
                        onSwipeRight(diffX)
                    else {
                        val key: String = activity.intent.getStringExtra("key") ?: ""
                        workManager.cancelUniqueWork("VibrateWorker")
                        workManager.cancelUniqueWork("RingtoneWorker")
                        MainActivity.getRepository(activity.application).updateAlarmByKey(key, GlobalScope)
                        activity.finish()
                    }
                } else {
                    if( intArray[0] >= 16 ) onSwipeLeft(diffX)
                }


            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        private fun onSwipeRight(x: Float) {
            to_move.x = to_move.x.plus(x)
            to_move.invalidate()
        }

        private fun onSwipeLeft(x: Float) {
            to_move.x = to_move.x.plus(x)
            to_move.invalidate()

        }

        override fun onUp(event: MotionEvent) {
            ObjectAnimator.ofFloat(to_move, "x", to_move.x, 16F).start()
        }
    }

    open class MyGestureDetector( context: Context, val myGestureListener: GestureListener) : GestureDetector( context, myGestureListener){


        //u can write something more complex as long as u need
        override fun onTouchEvent(ev: MotionEvent): Boolean {
            if (ev.action == MotionEvent.ACTION_UP) {
                myGestureListener.onUp(ev)
            }
            return super.onTouchEvent(ev)
        }

        interface OnUp{
            fun onUp( event: MotionEvent )
        }
    }
}