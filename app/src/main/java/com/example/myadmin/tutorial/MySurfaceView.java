package com.example.myadmin.tutorial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.support.v4.view.VelocityTrackerCompat;
import android.util.Log;
import android.view.VelocityTracker;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;


/**
 * Created by myadmin on 18/05/2017.
 */

public class MySurfaceView extends View {
    private DataOutputStream dStream;
    private int port;
    private VelocityTracker mVelocityTracker = null;
    public MySurfaceView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public void setOutputStream(DataOutputStream d) {
        dStream = d;
    }

    public void setPort(int p) {
        port = p;
    }

    /**
     *  触屏模式的UI设计
     *  当你触碰到手机屏幕时，手机屏幕上会在你触碰的位置显示一段波纹
     *  波纹是由一系列不断生成的圆环构成的
     */
    private float mInitialRadius = 0;   // 初始波纹半径
    private float mMaxRadius = 50;     //  最大波纹半径
    private long mDuration = 500;      //  波纹最大持续时间
    private int mSpeed = 200;          //  波纹扩散速度
    private float mMaxRadiusRate = 0.85f;
    private boolean mMaxRadiusSet = true;

    private boolean mIsRunning;
    private long mLastCreateTime;
    private List<Circle> mCircleList = new ArrayList<Circle>();
    /**
     *   不断生成圆
     */
    private Runnable mCreateCircle = new Runnable() {
        @Override
        public void run() {
            if (mIsRunning) {
                newCircle();
                postDelayed(mCreateCircle, mSpeed);  //每隔mSpeed毫秒创建一个圆
            }
        }
    };
    private Interpolator mInterpolator = new LinearInterpolator();    //创建差值器 用来计算圆的透明度与半径

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (!mMaxRadiusSet) {
            mMaxRadius = Math.min(w, h) * mMaxRadiusRate / 2.0f;
        }
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    /**
     *   启动扩散
     */
    public void start() {
        if (!mIsRunning) {
            mIsRunning = true;
            mCreateCircle.run();
        }
    }

    /**
     *  缓慢停止
     */
    public void stop() {
        mIsRunning = false;
    }

    /**
     *  立刻停止
     */
    public void stopImmediately() {
        mIsRunning = false;
        mCircleList.clear();
        invalidate();
    }

    /**
     *  绘制波纹
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.BLACK);                             // 设置波纹颜色
        Iterator<Circle> iterator = mCircleList.iterator();
        while (iterator.hasNext()) {
            Circle circle = iterator.next();
            float radius = circle.getCurrentRadius();
            mPaint.setStyle(Style.STROKE);                        // 设置波纹样式(圆环)
            mPaint.setStrokeWidth(radius/3);
            /**
             *  该圆环的扩散时间没有超过最大扩散时间
             *  计算当前时刻圆环的透明度与半径
             *  并绘制
             */
            if (System.currentTimeMillis() - circle.mCreateTime < mDuration) {
                mPaint.setAlpha(circle.getAlpha());
                canvas.drawCircle(mX, mY, radius, mPaint);
            } else {
                iterator.remove();                    //移除
            }
        }

    }


    /**
     *  创建新的圆
     *  并将新创建的圆加入圆列表
     */
    private void newCircle() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastCreateTime < mSpeed) {
            return;
        }
        Circle circle = new Circle();
        mCircleList.add(circle);
        invalidate();
        mLastCreateTime = currentTime;
    }

    /**
     *  圆类
     *  可以计算当前圆的透明度与半径
     */
    private class Circle {
        private long mCreateTime;

        Circle() {
            mCreateTime = System.currentTimeMillis();
        }

        int getAlpha() {
            float percent = (getCurrentRadius() - mInitialRadius) / (mMaxRadius - mInitialRadius);
            return (int) (255 - mInterpolator.getInterpolation(percent) * 255);
        }

        float getCurrentRadius() {
            float percent = (System.currentTimeMillis() - mCreateTime) * 1.0f / mDuration;
            return mInitialRadius + mInterpolator.getInterpolation(percent) * (mMaxRadius - mInitialRadius);
        }
    }


    /**
     *  手势部分使用的一些变量
     */
    private static final int MAX_CLICK_DURATION = 400;     // 一个动作的最大持续时间
    private long startClickTime = 0;                       // 一个动作的开始时间
    private boolean secondTouch = false;                   // 是否多指触控
    private boolean scoll = false;                         // 是否为滚轮操作
    private boolean drag = false;                          // 是否为拖拽操作
    private float mX = 0;                                  // 手机屏幕的X坐标
    private float mY = 0;                                  // 手机屏幕的Y坐标

    /**
     *  对手势进行判断
     *  并将数据传递到电脑端
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);
        /**
         *  按下手机屏幕一个位置时在该处绘制波纹
         */
        mX = (int)event.getX();
        mY = (int) event.getY();
        start();
        invalidate();

        switch(action) {

            /**
             *  如果事件是按下屏幕
             */
            case MotionEvent.ACTION_DOWN:

                startClickTime = Calendar.getInstance().getTimeInMillis();            //记录按下的时间
                /**
                 *  关于速度追踪器的一些设置
                 */
                if(mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();  //获得一个新的速度追踪器的对象
                }
                else {
                    mVelocityTracker.clear(); // 重置到初始状态
                }
                mVelocityTracker.addMovement(event);  // 将该事件添加到速度追踪器的动作中
                break;

            /**
             *  如果有更多的手指按下了屏幕
             */
            case MotionEvent.ACTION_POINTER_DOWN:
                secondTouch = true;
                break;

            /**
             *  如果事件是移动
             */
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                /**
                 *  计算当前移动速度
                 *  将X、Y方向上的速度记录到fXV,fYV中
                 */
                mVelocityTracker.computeCurrentVelocity(1000);

                Log.d("", "X velocity: " +
                        VelocityTrackerCompat.getXVelocity(mVelocityTracker,
                                pointerId));
                Float fXV =
                        new Float(VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId));
                Log.d("", "Y velocity: " +
                        VelocityTrackerCompat.getYVelocity(mVelocityTracker,
                                pointerId));
                Float fYV =
                        new Float(VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId));

                /**
                 *  判断事件类型
                 *  并将相应数据传递至服务器端
                 */
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;                // 计算动作持续时间
                if(secondTouch ==true && clickDuration <= MAX_CLICK_DURATION ||(scoll == true)){               // 判断为滚轮操作
                    scoll = true;
                    try{
                        dStream.writeBytes("SCOLL:" + fXV.toString() + " " + fYV.toString() + "\n");
                    } catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                else if(secondTouch == true && clickDuration > MAX_CLICK_DURATION && drag == false && scoll == false){     // 判断为拖拽操作
                    drag = true;
                    try{
                        System.out.println("DRAGING~~~");
                        dStream.writeBytes("DRAG:" + fXV.toString() + " " + fYV.toString() + "\n");
                    } catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                else {                                                                                          // 其余为单纯的移动操作
                    try {
                        dStream.writeBytes("MOVE:" + fXV.toString() + " " + fYV.toString() + "\n");
                    } catch (IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                break;

            /**
             *  如果事件是抬起后面落下的指头
             */
            case MotionEvent.ACTION_POINTER_UP:

                long semiDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;

                if(semiDuration < MAX_CLICK_DURATION){                            // 判断是否为右键
                    try {
                        dStream.writeBytes("RIGHT:" + semiDuration + "\n");
                    }
                    catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                break;

            /**
             *  如果事件是抬起最先落下的指头
             */
            case MotionEvent.ACTION_UP:
                clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;    //计算持续时间
                if(scoll == true) scoll = false;                                               //  取消滚轮
                else if(drag == true){                                                         //  处理拖拽
                    drag= false;
                    try {
                        dStream.writeBytes("RELEASE:" + "\n");
                    }
                    catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                else if(clickDuration < MAX_CLICK_DURATION && !secondTouch) {                 //  否则认为是单击
                    //click event has occurred
                    try {
                        dStream.writeBytes("CLICK:" + clickDuration + "\n");
                    }
                    catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                else {
                    ;
                }

                /**
                 *   将各类参数回复默认值
                 *   准备下一轮手势
                  */
            case MotionEvent.ACTION_CANCEL:
                secondTouch = false;
                drag = false;
                // Return a VelocityTracker object back to be re-used by others.
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                break;
        }
        return true;
    }
}