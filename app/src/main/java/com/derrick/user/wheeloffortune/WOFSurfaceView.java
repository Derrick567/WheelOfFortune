package com.derrick.user.wheeloffortune;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by user on 2015/9/5.
 */
public class WOFSurfaceView extends SurfaceView implements SurfaceHolder.Callback,Runnable{
    String TAG="WOFSurfaceView";
    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    //use draw
    private Thread t;
    private boolean isRunning;

    //The word of each item block
    private String [] mString ={"相機","IPAD","恭喜發財","IPHONE","服裝一套","恭喜發財"};
    //The image of each item block
    private int[]mImages ={R.mipmap.danfan,R.mipmap.ipad,R.mipmap.f040,R.mipmap.iphone,R.mipmap.meizi,R.mipmap.f040};

    private double[] prizeProb= new double[]{5.0, 2.5, 40.0,2.5, 10.0, 40.0};
    private int  mItemCount = 6;

    private Bitmap[] mImgsBitmap ;
    //background
    private  Bitmap mBgBitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.bg2);
    //The color of each item block
    private int [] mColors ={0xffffc300,0xfff17e01,0xffffc300,0xfff17e01,0xffffc300,0xfff17e01};

    //the paint used to drawing wheel
    private Paint mArcPaint;
    //the paint used to drawing text
    private Paint mTextPaint;

    //The range of each item block
    private RectF mRange ;
   //  radius of  the wheel
    private int mRadius;

    //the center of wheel
    private int mCenter;

    //this padding 直接以paddingLeft 為準
    private int mPadding;
    // the speed of scrolling
    private double mSpeed;
    //
    private volatile float mStartAngle=0;

    //check whether clicked the stop button
    private boolean isShouldEnd;





    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,20,getResources().getDisplayMetrics());

    public WOFSurfaceView(Context context) {
        this(context, null);
    }
    public WOFSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder= getHolder();
        mHolder.addCallback(this);

        //can get focus
        setFocusable(true);
        setFocusableInTouchMode(true);

        setKeepScreenOn(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = Math.min(getMeasuredWidth(),getMeasuredHeight());
        mPadding = getPaddingLeft();
        mRadius= width-mPadding*2;
        mCenter= width/2;
        //設置輪盤區塊為正方形
        setMeasuredDimension(width, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //init paint
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);
        mRange = new RectF(mPadding,mPadding,mPadding+mRadius,mPadding+mRadius);
        mImgsBitmap = new Bitmap[mItemCount];
        for(int i=0;i<mItemCount;i++){
            mImgsBitmap[i] =BitmapFactory.decodeResource(getResources(),mImages[i]);
        }

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
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            if(end-start<50){
                try {
                    Thread.sleep(50-(end-start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void  start(){
        int index = getPrizeIndex();
        int angle = 360/mItemCount;

        //caculate the probability of each prize
        /*
        0 :210-270
        1 :150-210
        */
        float from=270-(index+1)*angle;
        float end =from+angle;

        //設置停下須旋轉的距離
        //停下來位置介於 targetFrom 到targetEnd 之間
        float targetFrom = 4*360+from;
        float targetEnd =4*360+end;

        /**
         * <pre>
         *  設 v1 為起始速度 0為中止速度
         *  v1 ->0
         *  每次-1
         *
         *  （v1+0）*(v1+1)/2=targetFrom
         *  =v1^2+v1-2*targetFrom (1元二次方程式)
         *  v1=(-1*Math.sqrt(1+8*targetFrom))/2
         *  </pre>
         */
            float v1 = (float) ((-1+Math.sqrt(1+8*targetFrom))/2);
            float v2 = (float) ((-1+Math.sqrt(1+8*targetEnd))/2);
       mSpeed=v1+Math.random()*(v2-v1);

        //mSpeed=50;
        isShouldEnd=false;
    }

    private int getPrizeIndex() {
        //產生一個亂數1~1000
        int index=-1;
        int r= (int)(Math.random()*1000+1);
        int pp[] = new int[mItemCount];
        for(int i=0;i<mItemCount;i++){
            for(int j=0;j<=i;j++){
                pp[i]+=prizeProb[j]*10;
            }
        }
        /*
        for(int i=0;i<mItemCount;i++){
            Log.d(TAG,"pp ["+i+"]="+pp[i]);
        }
        */
       for(int i=0;i<mItemCount;i++){
           if(r<=pp[i]){
               index=i;
               break;
           }
       }
        Log.d(TAG, "r=" + r + ",index=" + index);
     return index;
    }

    public void end(){
        mStartAngle=0;
        isShouldEnd=true;
    }

    public boolean isRolling(){
        return mSpeed!=0;

    }

    public boolean isShouldEnd(){
        return isShouldEnd;
    }
    private void draw() {
        try{

            mCanvas = mHolder.lockCanvas();
            if(mCanvas!=null){
                //draw bg
                drawBg();

                //draw block
                float tmpAngle = mStartAngle;
                float sweepAngle =360/mItemCount;
                for(int i =0;i<mItemCount;i++){
                    mArcPaint.setColor(mColors[i]);
                    mCanvas.drawArc(mRange, tmpAngle, sweepAngle, true, mArcPaint);
                    drawText(tmpAngle, sweepAngle, mString[i]);
                    drawIcon(tmpAngle, mImgsBitmap[i]);
                    tmpAngle+=sweepAngle;
                }
                mStartAngle +=mSpeed;

                if(mSpeed>0){
                //Log.d(TAG,"mStartAngle="+mStartAngle);
                   // Log.d(TAG, "mSpeed=" + mSpeed);
                }
                //if click stop button
                if(isShouldEnd){
                    //let wheel stop slowly
                    mSpeed-=1;
                }
                if(mSpeed<=0){
                    mSpeed=0;
                    isShouldEnd=false;
                }
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



    private void drawText(float tmpAngle, float sweepAngle, String s) {
        Path path = new Path();
        path.addArc(mRange,tmpAngle,sweepAngle);

        float textWidth =  mTextPaint.measureText(s);

        //calulate  horizontal offset
        int hOffset = (int) (mRadius*Math.PI/mItemCount/2 - textWidth/2);
        int VOffset = mRadius/ 2 / 6;
        mCanvas.drawTextOnPath(s, path, hOffset, VOffset, mTextPaint);


    }
    private void drawIcon(float tmpAngle, Bitmap bitmap) {
        //set width of image is divided by 8
       int imgWidth =mRadius/8;

        //radians  = degrees * (Math.PI / 180)
        float angle = (float)((tmpAngle+360/mItemCount/2)*Math.PI/180);

        //coordinates of center point of image
        int x= (int) (mCenter +mRadius/2/2*Math.cos(angle));
        int y = (int) (mCenter +mRadius/2/2*Math.sin(angle));

        //
        Rect rect = new Rect(x-imgWidth/2,y-imgWidth/2 ,x+imgWidth/2,y+imgWidth/2);
        mCanvas.drawBitmap(bitmap,null,rect,null);
    }
    private void drawBg() {
        mCanvas.drawColor(Color.WHITE);
        mCanvas.drawBitmap(mBgBitmap,null,
                new Rect(mPadding/2,mPadding/2,getMeasuredWidth()-mPadding/2,getMeasuredWidth()-mPadding/2),
        null);
    }
}
