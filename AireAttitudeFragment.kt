package com.csk.hbsdrone.hubsan501M.view.optionFragment

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.csk.hbsdrone.R
import com.csk.hbsdrone.hubsan501M.hutils.CommonTools
import com.csk.hbsdrone.utils.Constants
import com.csk.hbsdrone.utils.PreferenceUtils
import com.csk.hbsdrone.utils.Utils
import kotlinx.android.synthetic.main.h501m_option_head_data_fragment.view.*
import java.lang.ref.WeakReference
1111111111111111
/**
 *  @author Leon
 *  @time 2018/8/10  14:05
 *  @describe  1234
 */
class AireAttitudeFragment : Fragment() {
    internal var view: View? = null
    var parameterUnit = Constants.H117_METRIC

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.h501m_option_head_data_fragment, container, false)
        mMyHandler = MyHandler(this)
        parameterUnit = PreferenceUtils.getPrefString(activity, Constants.H117_PARAMETER_UNIT, Constants.H117_METRIC)
        updateUnit()

        return view
    }

    override fun onResume() {
        super.onResume()
        var parameterUnitTemp = PreferenceUtils.getPrefString(activity, Constants.H117_PARAMETER_UNIT, Constants.H117_METRIC)
        if (!parameterUnitTemp.equals(parameterUnit)) {
            parameterUnit = parameterUnitTemp
            updateUnit()
        }
    }

    fun updateUnit() {
        setSpeakView("0.0")
        setAirhightView("0.0")
        setHNAValueView("0.0")
    }

    var roll: Float? = null
    var pitch: Float? = null
    var yaw: Float? = null
    fun setAttitude(roll: Float, pitch: Float, yaw: Float) {
        this.roll = roll
        this.pitch = pitch
        this.yaw = yaw
        val msg = Message()
        msg.what = 1
        mMyHandler?.sendMessage(msg)
    }

    fun setHubsanTopAirLatLon(data: String) {
        val msg = Message()
        msg.what = 2
        msg.obj = data
        mMyHandler?.sendMessage(msg)
    }

    fun setHubsanTopRockerLatLon(data: String) {
        val msg = Message()
        msg.what = 3
        msg.obj = data
        mMyHandler?.sendMessage(msg)
    }

    fun setSpeak(speed: String) {
        val msg = Message()
        msg.what = 6
        msg.obj = speed
        mMyHandler?.sendMessage(msg)
    }

    fun setSpeakView(speed: String?) {
        if (Constants.H117_METRIC.equals(parameterUnit)) {
            view?.HSNAValue?.text = speed + "m/s"
        } else {
            view?.HSNAValue?.text = "${CommonTools.floatDouble_OnePoint(Utils.meters2Feet(speed?.toDouble()!!))}ft/s"
        }
    }

    fun setAirhight(dist: String) {
        val msg = Message()
        msg.what = 7
        msg.obj = dist
        mMyHandler?.sendMessage(msg)
    }

    fun setAirhightView(dist: String?) {
        if (Constants.H117_METRIC.equals(parameterUnit)) {
            view?.VSNAValue?.text = dist + "m"
        } else {
            view?.VSNAValue?.text = "${CommonTools.floatDouble_OnePoint(Utils.meters2Feet(dist?.toDouble()!!))}ft"
        }
    }

    fun setHNAValue(dist: String) {
        val msg = Message()
        msg.what = 8
        msg.obj = dist
        mMyHandler?.sendMessage(msg)
    }

    fun setHNAValueView(dist: String?) {
//        view?.HNAValue?.text = dist + "m"

        if (Constants.H117_METRIC.equals(parameterUnit)) {
            view?.HNAValue?.text = dist + "m"
        } else {
            view?.HNAValue?.text = "${CommonTools.floatDouble_OnePoint(Utils.meters2Feet(dist?.toDouble()!!))}ft"
        }
    }

    internal var mMyHandler: MyHandler? = null

    internal inner class MyHandler(activity: Fragment) : Handler() {
        val mActivityReference: WeakReference<Fragment>

        init {
            mActivityReference = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val activity = mActivityReference.get()
            if (activity != null) when (msg.what) {
                1 -> {
                    view?.hubsanPitch?.text = "" + pitch?.toInt() + "°" // 俯仰
                    view?.hubsanRoll?.text = "" + roll?.toInt() + "°" // 翻滚
                    view?.hubsanYaw?.text = "" + yaw?.toInt() + "°"// 偏航
                }
                2 -> {
                    view?.hubsanTopAirLatLon?.text = msg.obj.toString()
                }
                3 -> {
                    view?.hubsanTopRockerLatLon?.text = msg.obj.toString()
                }
                6 -> {
                    setSpeakView(msg.obj?.toString())
                }
                7 -> {
                    setAirhightView(msg.obj?.toString())
                }
                8 -> {
                    setHNAValueView(msg.obj?.toString())
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMyHandler?.removeCallbacksAndMessages(null)
        mMyHandler = null
    }
}