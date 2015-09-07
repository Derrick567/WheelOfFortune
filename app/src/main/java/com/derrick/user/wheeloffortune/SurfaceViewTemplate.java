package com.derrick.user.wheeloffortune;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by user on 2015/9/5.
 */
public class SurfaceViewTemplate extends SurfaceView implements SurfaceHolder.Callback,Runnable{

    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    //use draw
    private Thread t;
    private boolean isRunning;

    public SurfaceViewTemplate(Context context) {
        this(context, null);
    }
    public SurfaceViewTemplate(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder= getHolder();
        mHolder.addCallback(this);

        //can get focus
        setFocusable(true);
        setFocusableInTouchMode(true);

        setKeepScreenOn(true);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning=true;
        t= new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning=false;
    }

    @Override
    public void run() {
        //不斷進行繪製
        while(isRunning){
            draw();
        }
    }

    private void draw() {
        try{

            mCanvas = mHolder.lockCanvas();
            if(mCanvas!=null){

            }
        }
        catch(Exception e){

        }
        finally{
            if(mCanvas!=null){
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }

    }
}
