package com.csk.hbsdrone.hubsan501M.view.optionFragment

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.csk.hbsdrone.HubsanDroneApplication
import com.csk.hbsdrone.R
import com.csk.hbsdrone.hubsan501M.bean.AirModeConstantH501M
import com.csk.hbsdrone.hubsan501M.bean.Common
import com.csk.hbsdrone.utils.Constants
import com.csk.hbsdrone.utils.PreferenceUtils
import com.csk.hbsdrone.utils.Utils
import com.csk.hbsdrone.widgets.notice.AnimMessage
import com.csk.hbsdrone.widgets.notice.NoticeAnimationManager
import com.hubsansdk.application.HubsanApplication
import com.hubsansdk.drone.HubsanDrone
import com.hubsansdk.drone.bean.Joystick
import com.hubsansdk.utils.AirType
import kotlinx.android.synthetic.main.h501m_option_currentplace.view.*
import kotlinx.android.synthetic.main.h501m_option_maptype.view.*
import kotlinx.android.synthetic.main.h501m_option_right_locationshow_fragment.view.*
import java.lang.ref.WeakReference

/**
 *  @author Leon
 *  @time 2017/7/19  18:23
 *  @describe  12334
 */
class RockMapDataFragment : Fragment() {
    internal var view: View? = null
    var listener: RockMapDataListener? = null

    private var drone: HubsanDrone? = null
    private lateinit var app: HubsanDroneApplication
    private var mondStatus: Boolean = false  //在航�?跟随环绕模式�?右上角摇杆栏不显�?
    /**
     * 弹窗模式操作�?
     */
    private var isFullScreen = false
    /**
     * 弹窗选择模式框地图操作栏不能显示
     */
    private var isShowModeView = false
    private var airName = AirType.H117A
    private var joystick: Joystick? = null

    interface RockMapDataListener {
        fun editorToolChanged(tool: Common.EditorTools)
        fun maptype(type: Int) //1普�?2 卫星 3夜间
        fun findLocationClick(type: Int) //我的位置 2飞机位置
        fun toSettingCamera()
    }

    private fun setTool(tool: Common.EditorTools) {
        if (tool == Common.EditorTools.NONE) {
            listener?.editorToolChanged(tool)
        } else {
            listener?.editorToolChanged(tool)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.h501m_option_right_locationshow_fragment, container, false)
        mMyHandler = MyHandler(this)
        listener = activity as RockMapDataListener
        app = activity!!.application as HubsanDroneApplication
        this.drone = app.drone
        joystick = drone?.joystick

        airName = drone!!.airBaseParameters.airSelectMode
        PreferenceUtils.setPrefBoolean(HubsanApplication.getApplication(), "isCalibration", false) //默认校准没选中
        setTopMapCalibration(false)
        onclickOption()
        val airName = app.drone.airBaseParameters.airSelectMode
        if (AirType.H117A == airName || AirType.H117Pro == airName) {
            view?.CSLay?.visibility = View.VISIBLE
        } else {
            view?.CSLay?.visibility = View.GONE
        }
        return view
    }

    private fun onclickOption() {
        view?.setheadLay?.setOnClickListener {
            //有头无头
            if (!Utils.isCameraFastClick(1000)) {
                setTool(Common.EditorTools.NONE)
                setTool(Common.EditorTools.hubsanHead)
                headEnable(false)
            }
        }
        view?.topFind?.setOnClickListener {
            //定位
            setTool(Common.EditorTools.NONE)
            view?.hubsanSelectMapLocationLay?.visibility = View.VISIBLE
        }
        view?.mapModelBtn?.setOnClickListener {
            //地图类型
            setTool(Common.EditorTools.NONE)
            view?.hubsanSelectMapLay?.visibility = View.VISIBLE
        }
        view?.topMapCalibration?.setOnClickListener {
            //校准
            setTool(Common.EditorTools.NONE)
            if (drone?.airMode?.getMotorStatus() == 3) {
                //设置里设置可校准地图打开 则一直可以校�?
                val longpresstocalibration = PreferenceUtils.getPrefBoolean(activity, Constants.SETTING_MAP_CALIBRATION_OPEN_CLOSE_207s, false)
                if (!longpresstocalibration) {
                    val isCalibration = PreferenceUtils.getPrefBoolean(HubsanApplication.getApplication(), Constants.ISCALIBRATION207s, false)// 判断校准是否处于选中状�?
                    if (isCalibration) {
                        setTopMapCalibration(false)
                    } else {
                        setTopMapCalibration(true)
                        NoticeAnimationManager.addAnimalMessage(AnimMessage(resources.getString(R.string.hubsan_501_cail_top_notify), 0, 3000, 10))
                    }
                }
            } else {
                NoticeAnimationManager.addAnimalMessage(AnimMessage(resources.getString(R.string.h501m_coordinates_false_tip1), 1, 3000, 10))
            }
        }
        //定位
        view?.hubsanMapMy?.setOnClickListener {
            setTool(Common.EditorTools.NONE)
            listener?.findLocationClick(1)
        }
        view?.hubsanMapAir?.setOnClickListener {
            setTool(Common.EditorTools.NONE)
            listener?.findLocationClick(2)
        }
        //地图类型
        view?.hubsanMapNormal?.setOnClickListener {
            setTool(Common.EditorTools.NONE)
            listener?.maptype(1)
        }
        view?.hubsanMapSatellite?.setOnClickListener {
            setTool(Common.EditorTools.NONE)
            listener?.maptype(2)
        }
        view?.hubsanMapNight?.setOnClickListener {
            setTool(Common.EditorTools.NONE)
            listener?.maptype(3)
        }
        view?.modeImageLay?.setOnClickListener {
            var rockerDoing = PreferenceUtils.getPrefBoolean(HubsanDroneApplication.getApplication(), Constants.H501M_ROCKER_DOING, false)
            if (!rockerDoing) {
                setTool(Common.EditorTools.NONE)
                setTool(Common.EditorTools.showModeDialog)
            }
        }
        view?.modeText?.setOnClickListener {
            var rockerDoing = PreferenceUtils.getPrefBoolean(HubsanDroneApplication.getApplication(), Constants.H501M_ROCKER_DOING, false)
            if (!rockerDoing) {
                setTool(Common.EditorTools.NONE)
                setTool(Common.EditorTools.showModeDialog)
            }
        }
        view?.CSImage?.setOnClickListener {
            //相机设置
            listener?.toSettingCamera()
        }
    }

    fun setTopMapCalibration(enable: Boolean) {
        //设置里校准开关打开
        val longpresstocalibration = PreferenceUtils.getPrefBoolean(activity, Constants.SETTING_MAP_CALIBRATION_OPEN_CLOSE_207s, false)
        if (enable || longpresstocalibration) {
            view?.topMapCalibration?.setImageResource(R.drawable.h501m_top_compass_pressed)
            PreferenceUtils.setPrefBoolean(HubsanApplication.getApplication(), Constants.ISCALIBRATION207s, true)
        } else {
            view?.topMapCalibration?.setImageResource(R.drawable.h501m_top_compass_normal)
            PreferenceUtils.setPrefBoolean(HubsanApplication.getApplication(), Constants.ISCALIBRATION207s, false)
        }
    }

    fun showMapHalp(show: Boolean) {
        if (show && (!isShowModeView)) {
            view?.topLeftTools?.visibility = View.VISIBLE
            view?.hubsanHeadRight?.visibility = View.VISIBLE
            showCameraSetting(false)
        } else {
            view?.topLeftTools?.visibility = View.GONE
            view?.hubsanHeadRight?.visibility = View.GONE
            dismissRockMapOption()
            showCameraSetting(true)
        }
    }

    /**
     * 显示遥感操作�?
     */
    fun showRockData(show: Boolean) {
        if (show && (!mondStatus)) {
            view?.hubsanHeadLay?.visibility = View.VISIBLE
            view?.hubsanHeadRightTwo?.visibility = View.VISIBLE
        } else {
            view?.hubsanHeadLay?.visibility = View.GONE
            view?.hubsanHeadRightTwo?.visibility = View.GONE
        }
    }

    fun isFullScreen(fstatus: Boolean) {
        isFullScreen = fstatus
    }

    fun isShowModeView(fstatus: Boolean) {
        isShowModeView = fstatus
    }

    /**
     * 处于模式�?模式操作框弹出隐藏右上角模式 否则显示
     * 1F模式 2航点编辑模式 3 跟随 4环绕 5航点飞行模式  0x0E：射线模�?
     *
     */
    fun setModeImage(type: Int) {
        if ((!isFullScreen) && (!isShowModeView)) {
            if (type == 3) {
                view?.modeImageLay?.visibility = View.VISIBLE
                view?.modeText?.visibility = View.GONE
                view?.modeImage?.visibility = View.VISIBLE
                view?.modeImage?.setImageDrawable(activity?.let { ContextCompat.getDrawable(it, R.drawable.h501m_followme_02) })
                view?.modeText?.visibility = View.GONE
            } else if (type == 4) {
                view?.modeImageLay?.visibility = View.VISIBLE
                view?.modeText?.visibility = View.GONE
                view?.modeImage?.visibility = View.VISIBLE
                view?.modeImage?.setImageDrawable(activity?.let { ContextCompat.getDrawable(it, R.drawable.h501m_surround_02) })
                view?.modeText?.visibility = View.GONE
            } else if (type == 5) {
                view?.modeImageLay?.visibility = View.VISIBLE
                view?.modeText?.visibility = View.GONE
                view?.modeImage?.visibility = View.VISIBLE
                view?.modeImage?.setImageDrawable(activity?.let { ContextCompat.getDrawable(it, R.drawable.h501m_waypoint_02) })
                view?.modeText?.visibility = View.GONE
            } else if (type == AirModeConstantH501M.HUBSAN_V2_LINE_MODE.toInt()) {
                view?.modeImageLay?.visibility = View.VISIBLE
                view?.modeText?.visibility = View.VISIBLE
                view?.modeImage?.visibility = View.GONE
                if (drone?.airMode?.lineModeBean?.status == 1) { //暂停
                    view?.modeText?.text = getString(R.string.h501m_recovery)
                } else {  //执行�?
                    view?.modeText?.text = getString(R.string.h501m_suspend)
                }
            } else {
                view?.modeImageLay?.visibility = View.INVISIBLE
            }
        } else {
            view?.modeImageLay?.visibility = View.GONE
        }
    }

    /**
     * 设置显示摇杆数据
     */
    fun setRockerData() {
        val msg = Message()
        msg.what = 1
        mMyHandler?.sendMessage(msg)
    }

    private fun handlerSetRockerData(throttleRaw: Int, rudderRaw: Int, elevatorRaw: Int, aileronRaw: Int) {
        val airName = app.drone.airBaseParameters.airSelectMode
        if (AirType.H117A == airName || AirType.H117Pro == airName) {
            view?.hubsanRockerThrottle?.text = "" + (throttleRaw * 0.1).toInt() + "%"

            if (Math.abs(drone?.joystick?.rudderRaw!!) < 50) {
                view?.hubsanRockerRudder?.text = "0" + "%"
            } else {
                view?.hubsanRockerRudder?.text = "" + (rudderRaw!! * 0.1).toInt() + "%"
            }

            if (Math.abs(drone?.joystick?.elevatorRaw!!) < 50) {
                view?.hubsanRockerElevator?.text = "0" + "%"
            } else {
                view?.hubsanRockerElevator?.text = "" + (elevatorRaw * 0.1).toInt() + "%"
            }

            if (Math.abs(drone?.joystick?.aileronRaw!!) < 50) {
                view?.hubsanRockerAileron?.text = "0" + "%"
            } else {
                view?.hubsanRockerAileron?.text = "" + (aileronRaw * 0.1).toInt() + "%"
            }

            if (app.drone.airBaseParameters.isDetectioning()) { //开启跟随时后不更新遥杆显示，处理不显示0%
                view?.hubsanRockerThrottle?.text = "0" + "%"
                view?.hubsanRockerRudder?.text = "0" + "%"
                view?.hubsanRockerElevator?.text = "0" + "%"
                view?.hubsanRockerAileron?.text = "0" + "%"
            }
        } else {
            view?.hubsanRockerThrottle?.text = "" + (throttleRaw * 0.1).toInt() + "%"
            view?.hubsanRockerRudder?.text = "" + (rudderRaw * 0.1).toInt() + "%"
            view?.hubsanRockerElevator?.text = "" + (elevatorRaw * 0.1).toInt() + "%"
            view?.hubsanRockerAileron?.text = "" + (aileronRaw * 0.1).toInt() + "%"
        }
    }

    fun dismissRockMapOption() {
        view?.hubsanSelectMapLay?.visibility = View.GONE
        view?.hubsanSelectMapLocationLay?.visibility = View.GONE
    }

    private fun headEnable(status: Boolean) {
        if (!status) {
            view?.hubsanHeadImage?.setAlpha(80)
            view?.hubsanHeadImage?.setEnabled(false)
        } else {
            //可点�?
            view?.hubsanHeadImage?.setAlpha(255)
            view?.hubsanHeadImage?.setEnabled(true)
        }
    }

    fun showhubsanHeadImage(status: Int) {
        if (status == 1) {
            view?.hubsanHeadImage?.setImageResource(R.drawable.hubsan_501_no_head_normal)
            headEnable(true)
        } else {
            view?.hubsanHeadImage?.setImageResource(R.drawable.hubsan_501_head_normal)
            headEnable(true)
        }
    }

    var showCount = 0  //降低摇杆显示频率

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
                    var throttleRaw = joystick?.throttleRaw
                    var rudderRaw = joystick?.rudderRaw
                    var elevatorRaw = joystick?.elevatorRaw
                    var aileronRaw = joystick?.aileronRaw
                    if (showCount % 3 == 0 || (throttleRaw == 0 && rudderRaw == 0 && elevatorRaw == 0 && aileronRaw == 0)) {
                        handlerSetRockerData(throttleRaw!!, rudderRaw!!, elevatorRaw!!, aileronRaw!!)
//                        LogX.e("---------------show $showCount  $throttleRaw $rudderRaw  $elevatorRaw $aileronRaw ")
                        showCount = 0
                    } else {
//                        LogX.e("---------------show-----cut $showCount  $throttleRaw $rudderRaw  $elevatorRaw $aileronRaw" )
                    }
                    showCount++
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMyHandler?.removeCallbacksAndMessages(null)
        mMyHandler = null
    }

    fun commNotify(message: String, duration: Int) {
//        HubsanToast.getHubsanToast().showToastMessage(HubsanApplication.getApplication(), message, duration)
    }

    fun showCameraSetting(show: Boolean) {
        if (show && (!isShowModeView)) {

            if (AirType.H117A == airName || AirType.H117Pro == airName) {
                view?.CSLay?.visibility = View.VISIBLE
            }
        } else {
            view?.CSLay?.visibility = View.GONE
        }
    }

}