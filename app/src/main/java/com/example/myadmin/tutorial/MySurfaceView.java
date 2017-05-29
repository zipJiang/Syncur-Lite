package com.example.myadmin.tutorial;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
//import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;


import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;


/**
 * Created by myadmin on 18/05/2017.
 */


/*
class GameView extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder surfaceHolder;
    public GameView(Context context) {
        super(context);
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
        this.setFocusable(true);
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
    public void surfaceCreated(SurfaceHolder holder) {
    }
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:   // 点击屏幕后 半径设为0,alpha设置为255
                MyBean bean = new MyBean();
                bean.radius = 0; // 点击后 半径先设为0
                bean.alpha = 255; // alpha设为最大值 255
                bean.width = bean.radius / 8; // 描边宽度 这个随意
                bean.X = (int) event.getX(); // 所绘制的圆的X坐标
                bean.Y = (int) event.getY(); // 所绘制的圆的Y坐标
                bean.paint = new Paint();
                bean.paint.setColor(Color.BLACK);
                bean.paint.setStyle(Style.STROKE);
                bean.paint.setStrokeWidth(bean.width);
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawCircle(bean.X,bean.Y,10,bean.paint);
                break;
        }
        return true;
    }
}*/

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


    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime = 0;
    private boolean secondTouch = false;
    private boolean scoll = false;
    private boolean drag = false;
    private float startX = 0;
    private float startY = 0;
    private float mX = 0;
    private float mY = 0;
    //private ImageView iv_canvas;
    //private Bitmap baseBitmap = null;
    //private Canvas canvas;
    //private  ImageView iv_canvas = (ImageView) findViewById(R.id.iv_canvas);

    private float mInitialRadius = 0;   // 初始波纹半径
    private float mMaxRadius = 50;     // 最大波纹半径
    private long mDuration = 500;      // 一个波纹从创建到消失的持续时间
    private int mSpeed = 200;          // 波纹的创建速度，每200ms创建一个
    private float mMaxRadiusRate = 0.85f;
    private boolean mMaxRadiusSet = true;

    private boolean mIsRunning;
    private long mLastCreateTime;
    private List<Circle> mCircleList = new ArrayList<Circle>();
    private Runnable mCreateCircle = new Runnable() {
        @Override
        public void run() {
            if (mIsRunning) {
                newCircle();
                postDelayed(mCreateCircle, mSpeed);
            }
        }
    };
    private Interpolator mInterpolator = new LinearInterpolator();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (!mMaxRadiusSet) {
            mMaxRadius = Math.min(w, h) * mMaxRadiusRate / 2.0f;
        }
    }

    //public void setMaxRadiusRate(float maxRadiusRate) {
    //    mMaxRadiusRate = maxRadiusRate;
   // }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    /**
     * 开始
     */
    public void start() {
        if (!mIsRunning) {
            mIsRunning = true;
            mCreateCircle.run();
        }
    }

    /**
     * 缓慢停止
     */
    public void stop() {
        mIsRunning = false;
    }

    /**
     * 立即停止
     */
    public void stopImmediately() {
        mIsRunning = false;
        mCircleList.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.BLACK);
        //mPaint.setColor(0xff8bc5ba);
        Iterator<Circle> iterator = mCircleList.iterator();
        while (iterator.hasNext()) {
            Circle circle = iterator.next();
            float radius = circle.getCurrentRadius();
            //mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStyle(Style.STROKE);
            mPaint.setStrokeWidth(radius/3);
            if (System.currentTimeMillis() - circle.mCreateTime < mDuration) {
                mPaint.setAlpha(circle.getAlpha());
                canvas.drawCircle(mX, mY, radius, mPaint);
            } else {
                iterator.remove();
            }
        }
        //if (mCircleList.size() > 0) {
        //    postInvalidateDelayed(10);
        //}

    }

    //public void setInitialRadius(float radius) {
   //     mInitialRadius = radius;
   // }

    //public void setDuration(long duration) {
    //    mDuration = duration;
    //}

    //public void setMaxRadius(float maxRadius) {
   //     mMaxRadius = maxRadius;
   //     mMaxRadiusSet = true;
   // }

    //public void setSpeed(int speed) {
    //    mSpeed = speed;
   // }

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

   // public void setInterpolator(Interpolator interpolator) {
    //    mInterpolator = interpolator;
   //     if (mInterpolator == null) {
   //         mInterpolator = new LinearInterpolator();
   //     }
  //  }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);
        mX = (int)event.getX();
        mY = (int) event.getY();
        start();
        invalidate();

        switch(action) {
            case MotionEvent.ACTION_DOWN:

                //mX = (int)event.getX();
                //mY = (int) event.getY();
                //start();
                //invalidate();

                //SurfaceView  surfaceView = new SurfaceView() ;         //创建一个Surface对象
                //GameView gameView = new GameView(getContext());
                //SurfaceHolder surfaceHolder = surfaceView. getHolder() ;  //获得SurfaceHolder对象
                //Canvas   canvas  = gameView.surfaceHolder.lockCanvas() ;          //获得canvas对象
                //进行绘图操作
                //surfaceHolder.unlockCanvasAndPost(canvas) ;            //释放canvas锁，并且显示视图
                //利用canvas进行绘图操作
                //iv_canvas = (ImageView) findViewById(R.id.iv_canvas);
                //if(baseBitmap == null){
                    //baseBitmap = Bitmap.createBitmap(iv_canvas.getWidth(),iv_canvas.getHeight(),Bitmap.Config.ARGB_8888);
                    //baseBitmap = Bitmap.createBitmap(getHeight(),getWidth(),Bitmap.Config.ARGB_8888);
                    //System.out.println("???!!!" + getHeight() + " " + getWidth());
                    //canvas = new Canvas(baseBitmap);
                    //canvas.drawColor(Color.WHITE);
               // }
                //canvas.drawCircle(bean.X,bean.Y,10,bean.paint);
                //gameView.surfaceHolder.unlockCanvasAndPost(canvas);
                //iv_canvas.setImageBitmap(baseBitmap);

                startClickTime = Calendar.getInstance().getTimeInMillis();
                if(mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                }
                else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(event);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                //startX = event.getX();
                //startY = event.getY();
                secondTouch = true;
                break;
            case MotionEvent.ACTION_MOVE:
                //if(secondTouch == true ) {
                //    scoll = true;
                //    break;
                //}
                mVelocityTracker.addMovement(event);
                // When you want to determine the velocity, call
                // computeCurrentVelocity(). Then call getXVelocity()
                // and getYVelocity() to retrieve the velocity for each pointer ID.
                mVelocityTracker.computeCurrentVelocity(1000);
                // Log velocity of pixels per second
                // Best practice to use VelocityTrackerCompat where possible.
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
                /* And I should send data to the server */
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                if(secondTouch ==true && clickDuration <= MAX_CLICK_DURATION ||(scoll == true)){
                    scoll = true;
                    try{
                        dStream.writeBytes("SCOLL:" + fXV.toString() + " " + fYV.toString() + "\n");
                    } catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                else if(secondTouch == true && clickDuration > MAX_CLICK_DURATION && drag == false){
                    drag = true;
                    try{
                        System.out.println("DRAGING~~~");
                        dStream.writeBytes("DRAG:" + fXV.toString() + " " + fYV.toString() + "\n");
                    } catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                else {
                    try {
                        dStream.writeBytes("MOVE:" + fXV.toString() + " " + fYV.toString() + "\n");
                    } catch (IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                secondTouch = false;
                /* Generate a right click */
                long semiDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                /*if( scoll == true ){
                    float DistanceX = event.getX() - startX;
                    float DistanceY = event.getY() - startY;
                    try{
                        dStream.writeBytes("SCOLL:" + String.valueOf(DistanceX) + " " + String.valueOf(DistanceY) + "\n");
                        scoll = false;
                        startX = 0; startY = 0;
                    }
                    catch(IOException e){
                        System.out.println("write to dStream failed.");
                    }
                }*/
                if(scoll = true) scoll = false;

                if(semiDuration < MAX_CLICK_DURATION){
                    try {
                        dStream.writeBytes("RIGHT:" + semiDuration + "\n");
                    }
                    catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                /*if( scoll == true ){
                    float DistanceX = event.getX() - startX;
                    float DistanceY = event.getY() - startY;
                    try{
                        dStream.writeBytes("SCOLL:" + String.valueOf(DistanceX) + " " + String.valueOf(DistanceY) + "\n");
                        scoll = false;
                        startX = 0; startY = 0;
                    }
                    catch(IOException e){
                        System.out.println("write to dStream failed.");
                    }
                }*/
                if(scoll == true) scoll = false;
                if(drag == true){
                    drag= false;
                    try {
                        dStream.writeBytes("RELEASE:" + "\n");
                    }
                    catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                if(clickDuration < MAX_CLICK_DURATION && !secondTouch) {
                    //click event has occurred
                    try {
                        dStream.writeBytes("CLICK:" + clickDuration + "\n");
                    }
                    catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                else if(clickDuration < MAX_CLICK_DURATION && secondTouch) {
                    try {
                        dStream.writeBytes("RIGHT:" + clickDuration + "\n");
                    }
                    catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                else {
                    ;
                }

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