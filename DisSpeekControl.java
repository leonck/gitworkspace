package com.hubsan.drone.detectionControl;

import android.os.Handler;
import android.os.Message;
import com.csk.hbsdrone.utils.LogX;
import com.csk.hbsdrone.utils.Utils;
import com.hubsansdk.drone.HubsanDrone;

/**
 * Author: 李豪
 * Time: 2019/10/12 11:33
 * Description: 通过摄像头，计算人移动距离 117P
 */
public class DisSpeekControl {
    private final double F = 3.44; //焦距 mm
    private final double P = 1.12 / 1000;//每个像素的 尺寸 mm
    //    static int sWidth = 3840; //sensor横向像素数
    private final int sHeight = 3120;//sensor纵向像素数
    private final double cHeight = sHeight * P;//sensor物理宽度
    private double cAngle = 30;//云台角度
    private double droneHeight = 5 * 1000;
    private final double picCutScale = 0.09;
    private final int picWidth = 480;
    private final int centerPosition = picWidth / 2;
    private double centerDistance = 0;
    private double forwardThreshold = 240 / 4;
    private double backThreshold = 240 / 4;
    double v1;
    Handler mHandler;

    public DisSpeekControl(Handler mHandler) {
        this.mHandler = mHandler;
        centerDistance = 0;
    }

    public double getElevator(int tPosition,  HubsanDrone drone, double gzCurrentServoRaw) {
        droneHeight = drone.airBaseParameters.getAltitude() * 1000;
        if (droneHeight < 4800) {
            droneHeight = 4800;
        }
        cAngle = gzCurrentServoRaw / 100.0;
        if (cAngle < 25) cAngle = 25;
        double elevator = 0;

        int tempP = tPosition;
        if (tPosition < 240) {
            tempP = tPosition + 50;
            if (tempP > 240) tempP = 240;
        } else if (tPosition > 240) {
            tempP = tPosition - 30;
            if (tempP < 240) tempP = 240;
        }
        double diff = getDistance(tempP);

        v1 = diff / 1000;

        elevator = v1 * 1000 / 8;
        if(elevator>0) {
            elevator = elevator / 1.2;
        }else {
            elevator = elevator / 1.4;
        }
        String str = "tP：" + tPosition + " tempP:" + tempP +
                "\nv：" + Utils.formateDouble(v1) + " E:" + Utils.formateDouble(elevator) +
                "\ncP:" + Utils.formateDouble(centerDistance) + " diff:" + Utils.formateDouble(diff) +
                "\ncA:" + cAngle + " dH:" + Utils.formateDouble(droneHeight);
//        System.out.println("tP：" + tPosition + "  cP:" + centerDistance + "  diff:" + diff + " v：" + v1 + "  E:" + elevator + "  cAngle:" + cAngle + "  dH:" + droneHeight);
        Message msg = new Message();
        msg.what = 109;
        msg.obj = str;
        mHandler.sendMessage(msg);

        return elevator;
    }

    private double getDistance(int tPosition) {
        double centerDistance = onUpDistance(centerPosition, cAngle);
        double d1 = onUpDistance(tPosition, cAngle);
        double diff = d1 - centerDistance;

        return diff;
    }

    private double onUpDistance(int tPosition, double cAngle) {
        double realHeight = picWidth / (1 - picCutScale * 2);
        double dis2Center = centerPosition - tPosition;//距离中心的距离
        double hTemp = sHeight / realHeight * dis2Center;
        double p1 = hTemp * P;
        double pi_angle = Math.atan(p1 / F);
        double mAngle = pi_angle * 180 / Math.PI;
        double tAngle;
        tAngle = 90 - cAngle + mAngle;
        double d1 = Math.tan(Math.toRadians(tAngle)) * droneHeight;

        return d1;
    }

    public double angle2v(double gzBCloundRate, double gzCurrentServoRaw) {
        int mgzBCloundRate = (int) (gzBCloundRate / 100.0);
        int mgzCurrentServoRaw = (int) (gzCurrentServoRaw / 100.0);

        double bgCenterDis = onUpDistance(centerPosition, mgzBCloundRate);
        double cCenterDis = onUpDistance(centerPosition, mgzCurrentServoRaw);
        double diffDis = cCenterDis - bgCenterDis;

        double v1 = diffDis / 1000;
        double aElevator = v1 * 1000 / 8;
        aElevator = aElevator/0.8;
        LogX.e("gzCRaw:" + mgzCurrentServoRaw + " cDif:" + (mgzBCloundRate - mgzCurrentServoRaw) + "    bgCenterDis:" + bgCenterDis + " cCenterDis:" + cCenterDis + " diffDis:" + diffDis + "  ve：" + aElevator);
        return aElevator;
    }

    public void getNeedTurnAngle(int ctPosition,int ctAngle){
        double cDistance = onUpDistance(ctPosition, ctAngle);

//        double realHeight = picWidth / (1 - picCutScale * 2);
//        double dis2Center = centerPosition - centerPosition;//距离中心的距离
//        double hTemp = sHeight / realHeight * dis2Center;
//        double p1 = hTemp * P;
//        double pi_angle = Math.atan(p1 / F);
//        double mAngle = pi_angle * 180 / Math.PI;
//        double  tAngle = 90 - cAngle + mAngle;
//        double d1 = Math.tan(Math.toRadians(tAngle)) * droneHeight;

        double ncAngle = Math.toDegrees(Math.atan( cDistance/droneHeight));
        double cmAngle = 90 - ncAngle;
        System.out.println("=============ncAngle=" +ncAngle+"  需要转动的角度cmAngle"+ cmAngle);
    }

    private double distance2Angle() {

        return cAngle;
    }

    private void yuntaiAngle() {
        double p1 = sHeight * P / 2;
        double pi_angle = Math.atan(p1 / F);
        double mAngle = pi_angle * 180 / Math.PI;
        System.out.println("云台物理角度：" + mAngle + "   p1=" + p1);
        double d1 = Math.tan(Math.toRadians(45 + mAngle)) * droneHeight;
        System.out.println("角度：" + (45 + mAngle) + "  最上方距离：" + d1 + "  dif：" + (d1 - 5000));
        double d2 = Math.tan(Math.toRadians(45 - mAngle)) * droneHeight;
        System.out.println("角度：" + (45 - mAngle) + "  最下方距离：" + d2 + "  dif：" + (d2 - 5000));
    }

    public static void main(String[] args) {
        DisSpeekControl control = new DisSpeekControl(null);
        control.getNeedTurnAngle(0,90); //5m
//        control.getElevator(0.0, 183, 0, null); //5m
//        getDistance(480); //最小值
//        getDistance(0); //最大值
//        getDistance(220); //最大值
//        getDistance(260); //最大值
//        System.out.println("======================");
//        yuntaiAngle();
    }
}
