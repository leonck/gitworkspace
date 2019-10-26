package com.csk.hbsdrone.hubsan501M.view.optionFragment

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.csk.hbsdrone.HubsanDroneApplication
import com.csk.hbsdrone.R
import com.csk.hbsdrone.hubsan501M.bean.Common
import com.csk.hbsdrone.utils.Constants
import com.csk.hbsdrone.utils.PreferenceUtils
import com.hubsansdk.application.HubsanApplication
import com.hubsansdk.utils.AirType
import kotlinx.android.synthetic.main.h501m_option_left_fragment.view.*
import java.lang.ref.WeakReference

/**
 *  @author Leon
 *  @time 2018/7/19  18:23
 *  @describe  左侧操作
 */
class FlyOptionFragment : Fragment() {
    internal var view: View? = null
    var listener: FlyOptionListener? = null
    internal lateinit var app: HubsanDroneApplication

    interface FlyOptionListener {
        fun editorToolChanged(tool: Common.EditorTools)
    }

    private fun setTool(tool: Common.EditorTools) {
        if (tool == Common.EditorTools.NONE) {
            listener?.editorToolChanged(tool)
        } else {
            listener?.editorToolChanged(tool)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.h501m_option_left_fragment, container, false)
        mMyHandler = MyHandler(this)
        app = HubsanApplication.baseApp as HubsanDroneApplication
        listener = activity as FlyOptionListener
        onclickOption()
        if (AirType.H117Pro == app?.drone?.airBaseParameters?.airSelectMode) {
            view?.hubsanLeftRocker?.visibility = View.GONE
        }
        return view
    }

    private fun onclickOption() {
        view?.hubsanLeftRocker?.setOnClickListener {
            var rockerDoing = PreferenceUtils.getPrefBoolean(HubsanDroneApplication.getApplication(), Constants.H501M_ROCKER_DOING, false)
            if (!rockerDoing) {
                setTool(Common.EditorTools.NONE)
                //打开关闭摇杆
                setTool(Common.EditorTools.ROCKER)
            }
        }
        view?.leftIsUnLock?.setOnClickListener {
            var rockerDoing = PreferenceUtils.getPrefBoolean(HubsanDroneApplication.getApplication(), Constants.H501M_ROCKER_DOING, false)
            if (!rockerDoing) {
                setTool(Common.EditorTools.NONE)
                //一键起飞
                setTool(Common.EditorTools.UnLock)
            }
        }
    }

    /**
     * 模式操作框弹出的时候隐藏左边栏
     */
    fun hintLeft(show: Boolean) {
        var msg = Message()
        msg.what = 1
        msg.obj = show
        mMyHandler?.sendMessage(msg)
    }

    var isModeTime = false
    /**
     * 模式按钮
     * 1正常 2模式开启
     */
    fun modeButtonStatu(type: Int) {
        if (type == 1) {
            isModeTime = false
//            view?.hubsanFollowModeMenu?.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.h501m_left_mode_button_bg))
        } else if (type == 2) {
            isModeTime = true
//            view?.hubsanFollowModeMenu?.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.h501m_mode_stop))
        }
    }

    /**
     * 遥控器状态
     *  1 正常状态
     *  2 蓝牙遥控器
     *  3 中继(普通中继 图标按正常显示)
     *  4 遥控器中继（HT011A 显示wifi图标）
     */
    var currentRockerType = 1

    fun setRockerType(type: Int) {
        currentRockerType = type
        var msg = Message()
        msg.what = 2
        mMyHandler?.sendMessage(msg)
    }

    /**
     * 摇杆是否可用
     * true 打开
     */
    var status = false

    fun openRocker(status: Boolean) {
        this.status = status

        var msg = Message()
        msg.what = 3
        mMyHandler?.sendMessage(msg)
    }

    /**
     * 是否可点击
     */
    private fun setLockButtonEnable(isBack: Boolean) {
        if (isBack) {
            view?.leftIsUnLock?.isClickable = true
            view?.leftIsUnLock?.isEnabled = true
            view?.leftIsUnLock?.imageAlpha = 255
        } else {
            view?.leftIsUnLock?.isClickable = false
            view?.leftIsUnLock?.isEnabled = false
            view?.leftIsUnLock?.imageAlpha = 100
        }
    }

    /**
     *
     * 一键起飞或降落图标
     * 1 起飞 2降落 3返航 4:着陆模式下不可点击 5：怠速不可点击
     */
    fun setLockFly(fly: Int) {
        var msg = Message()
        msg.what = 4
        msg.arg1 = fly
        mMyHandler?.sendMessage(msg)
    }

    var running = false
    private var frameAnim: AnimationDrawable? = null
    /**
     * 开启返航动画
     */
    private fun startAnimation(opent: Boolean) {
        if (opent) {
            if (!running) { //动图
                view?.leftIsUnLock?.setImageResource(R.drawable.h501m_turnback_anim_btn)
                frameAnim = view?.leftIsUnLock?.drawable as AnimationDrawable
                if (!frameAnim?.isRunning!!) {
                    frameAnim?.start()
                    running = true
                } else {
                    stopAnimation()
                    startAnimation(true)
                }
            }
        } else {
            stopAnimation()
        }
    }

    /**
     * 停止播放返航动画
     */
    private fun stopAnimation() {
        running = false
        if (frameAnim != null && frameAnim?.isRunning!!) {
            frameAnim?.stop()
        }
    }

    internal var mMyHandler: MyHandler? = null

    internal inner class MyHandler(activity: Fragment) : Handler() {
        var mActivityReference: WeakReference<Fragment>

        init {
            mActivityReference = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val activity = mActivityReference.get()
            if (activity != null) when (msg.what) {
                1 -> {
                    var show = msg.obj as Boolean
                    try {
                        if (show) {
                            view?.leftBtn?.visibility = View.GONE
                            view?.leftBtn?.animation = AnimationUtils.makeOutAnimation(getActivity(), false)
                        } else {
                            //            SwitchLayout.getSlideFromLeft(view?.leftBtn, null, 300)
                            view?.leftBtn?.visibility = View.VISIBLE
                            view?.leftBtn?.animation = AnimationUtils.makeInAnimation(getActivity(), true)
                        }
                    } catch (e: Exception) {
                        //加载动画使用了getActivity
                        e.printStackTrace()
                    }

                }
                2 -> {
                    if (status) {
                        when (currentRockerType) {
                            1 -> view?.hubsanLeftRocker?.setImageResource(R.drawable.h501m_off)
                            2 -> view?.hubsanLeftRocker?.setImageResource(R.drawable.h501m_bluetooth)
                            3 -> view?.hubsanLeftRocker?.setImageResource(R.drawable.h501m_off)
                            4 -> {
                                view?.hubsanLeftRocker?.setImageResource(R.drawable.h117s_wifi)
                            }
                        }
                    } else {
                        view?.hubsanLeftRocker?.setImageResource(R.drawable.h501m_on)
                    }
                }
                3 -> {
                    if (status) {
                        when (currentRockerType) {
                            1 -> view?.hubsanLeftRocker?.setImageResource(R.drawable.h501m_off)
                            2 -> view?.hubsanLeftRocker?.setImageResource(R.drawable.h501m_bluetooth)
                            3 -> view?.hubsanLeftRocker?.setImageResource(R.drawable.h501m_off)
                            4 -> view?.hubsanLeftRocker?.setImageResource(R.drawable.h117s_wifi)
                        }
                    } else {
                        view?.hubsanLeftRocker?.setImageResource(R.drawable.h501m_on)
                    }
                }
                4 -> {
                    var fly = msg.arg1
                    when (fly) {
                        1 -> {
                            startAnimation(false)
                            view?.leftIsUnLock?.setImageResource(R.drawable.h501m_unlocks)
                            setLockButtonEnable(true)
                        }
                        2 -> {
                            startAnimation(false)
                            view?.leftIsUnLock?.setImageResource(R.drawable.h501m_locks)
                            setLockButtonEnable(true)
                        }
                        3 -> {
                            startAnimation(true)
                            setLockButtonEnable(true)
                        }
                        4 -> {
                            startAnimation(false)
                            view?.leftIsUnLock?.setImageResource(R.drawable.h501m_locks)
                            setLockButtonEnable(false)
                        }
                        5 -> {
                            startAnimation(false)
                            view?.leftIsUnLock?.setImageResource(R.drawable.h501m_unlocks)
                            setLockButtonEnable(false)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMyHandler?.removeCallbacksAndMessages(null)
    }
}