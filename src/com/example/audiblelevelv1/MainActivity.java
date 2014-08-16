package com.example.audiblelevelv1;

/* 
 * author: bankbits
 * version: 1.1
 * */

import java.nio.Buffer;




import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.app.Activity;    
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;    
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;    
import android.graphics.Color;    
import android.graphics.Paint;    
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;    
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.Display;

import android.view.Menu;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;

public class MainActivity extends Activity implements SensorEventListener
{
	private Paint paintTouch = new Paint();
	private Path path = new Path();
	
	boolean CalibrateClicked = false;
	
	public int orientation;
	
	public int calibrateX = 0;
	public int calibrateY = 0;
	
	private DrawView drawView;
	private double xVol, yVol;
	
	//
	//
	// sensor variables
	//
	//
	private Handler mHandler;  
	SensorManager sensorManager = null;
	
	//
	//
	// paint variables
	//
	//
	Paint paintX1 = new Paint();
	Paint paintX2 = new Paint();
	Paint paintY1 = new Paint();
	Paint paintY2 = new Paint();
	Paint paintCircle = new Paint();
	Paint paintCircle2 = new Paint();
	
	Paint paintText = new Paint(); 
	Paint mPaint = new Paint(); 
	Paint paintcenter = new Paint();
	Paint paintButton = new Paint();
	
	Paint paintFill = new Paint();
	
	String tag = "Lifecycle Events";
	
	boolean isRunning;
	
	int cRadius = 100;
	
	//
	//
	// path for circle
	//
	//
	
	float scale = 20f;
	private int width;
	private int height;
	private int halfX;
	private int halfY;
	float x, y, z;
	public float distance;
	
	float circleY;
	float circleX;
	
	//
	//
	// audio variables
	//
	//
	
	public int modulo = 20;
	
	public int count = 0;
	
	static int BLOCKSIZE = 441;
	
	short[] mArr = new short[BLOCKSIZE];
	int iMinBufSize;
	AudioTrack mAudioTrackX;
	boolean bPaused;
	int volume = 90;
	
	float increment = (float)(2*Math.PI) * 2 / 44100;
	float angle = 0;
	float samples[] = new float[BLOCKSIZE];
	
	
	final int SAMPLING_RATE = 44100;            // Audio sampling rate
    final int SAMPLE_SIZE = 2;                  // Audio sample size in bytes--
    double fFreq = 440;                         // Frequency of sine wave in hz

    //Position through the sine wave as a percentage (i.e. 0 to 1 is 0 to 2*PI)
    double fCyclePosition1 = 0;        
    double fCyclePosition2 = 0;        
	
    int ctSamplesTotal = SAMPLING_RATE*5; 
    
    MediaPlayer mp;
	
    boolean playOnce = false;
    
    void makeSamples(double dFreq1, double dFreq2, double dVol1, double dVol2)
    {
		//On each pass main loop fills the available free space in the audio buffer
	      //Main loop creates audio samples for sine wave, runs until we tell the thread to exit
	      //Each sample is spaced 1/SAMPLING_RATE apart in time
	         double fCycleInc1 = dFreq1/SAMPLING_RATE;  // Fraction of cycle between samples
	         double fCycleInc2 = dFreq2/SAMPLING_RATE;  // Fraction of cycle between samples
	        
	    // Figure out how many samples we can add
	    //     int ctSamplesThisPass = mAudioTrackX.available()/SAMPLE_SIZE;   
	         
	        	 for (int i=0; i < BLOCKSIZE; i++)
		         {
	        		mArr[i] = 0;
	        		 // Math.ceil(Math.abs(z)) / 2 == 0
	        		// count % (Math.ceil(Math.abs(z)) / 20) == 0
	        		if(count % 5 == 0)
	        		{
	        			mArr[i] += (short)((dVol1 * Math.sin(2*Math.PI * fCyclePosition1)));
			            fCyclePosition1 += fCycleInc1;
			            fCyclePosition2 += fCycleInc2;
			            
			            if(count % 4 == 0)
			            {
			            	mArr[i] += (short)(dVol2 * Math.sin(2*Math.PI * fCyclePosition2));
			            }
	        		}
	        		
	        		// count % (Math.ceil(Math.abs(y)) / 2) == 0
	        		else if(count % 4 == 0)
	        		{
	        			mArr[i] += (short)(dVol2 * Math.sin(2*Math.PI * fCyclePosition2));
	        			fCyclePosition1 += fCycleInc1;
			            fCyclePosition2 += fCycleInc2;
	        		}
	        		else 
	        		{
	        			mArr[i] = 0;
	        			fCyclePosition1 += fCycleInc1;
			            fCyclePosition2 += fCycleInc2;
	        		}
	            
		         }
	         
    } /* makeSamples() */
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		 mp = MediaPlayer.create(this, R.raw.ding4);
		
		drawView = new DrawView(this); // drawView calculations <======================================================================>
        drawView.setBackgroundColor(Color.rgb(153, 197, 247));
        isRunning = true;
        startAudio();
        
        Display display = getWindowManager().getDefaultDisplay();  
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        
        halfX = width / 2;
        halfY = height / 2;
        
        // paint allocations <========================================================================================>
        
        /*
         * 
         * */
        paintButton.setColor(Color.GRAY);
        paintText.setColor(Color.WHITE); 
    	paintText.setTextSize(72); 
        
        /* 
         * 
         */
        
        paintTouch.setAntiAlias(true);
        paintTouch.setStrokeWidth(60f);
        paintTouch.setColor(Color.BLACK);
        paintTouch.setStyle(Paint.Style.STROKE);
        paintTouch.setStrokeJoin(Paint.Join.ROUND);
        
        paintCircle.setColor(Color.BLACK);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setStrokeWidth(5);
    	paintCircle2.setColor(Color.GREEN); 
        paintX1.setShader(new LinearGradient(halfX, 0, halfX, halfY, Color.RED, Color.GREEN, Shader.TileMode.MIRROR));
    	paintX1.setColor(Color.rgb(115, 125, 125));
        paintX1.setStrokeWidth(7);
        
        paintcenter.setColor(Color.WHITE);
        
        paintX2.setShader(new LinearGradient(halfX, halfY, halfX, height, Color.GREEN, Color.RED, Shader.TileMode.MIRROR));
       // paintX2.setColor(Color.rgb(115, 125, 125));
        paintX2.setStrokeWidth(7);
        
        paintY1.setShader(new LinearGradient(0, halfY, halfX, halfY, Color.RED, Color.GREEN, Shader.TileMode.MIRROR));
      //  paintY1.setColor(Color.rgb(115, 125, 125));
        paintY1.setStrokeWidth(7);
        
        paintY2.setShader(new LinearGradient(halfX, halfY, width, halfY, Color.GREEN, Color.RED, Shader.TileMode.MIRROR));
      //  paintY2.setColor(Color.rgb(115, 125, 125));
        paintY2.setStrokeWidth(7);
        
        paintFill.setColor(Color.BLACK);
        paintFill.setStyle(Paint.Style.STROKE);
        paintFill.setStrokeWidth(5);
        
        
        // orientation sensor <=====================================================================================>
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mHandler = new Handler();    
        mHandler.post(new Runnable(){    
            @Override    
            public void run() {    
                drawView.invalidate();  // Handler.post(Runnable    r),RunnableUI,View.invalidate()    
                mHandler.postDelayed(this, 5);    
            }    
         });
        setContentView(drawView);  
    
	}
	
	//
	// start functions
	//
	
	@Override
	 protected void onStart() 
	{
		super.onStart();
		isRunning = true;
	}
    	
	@Override
     protected void onRestart() 
	{
		super.onRestart();
		isRunning = true;
	}
	
	@Override
	 protected void onResume() 
	{
	    super.onResume();
	    isRunning = true;
	    sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), sensorManager.SENSOR_DELAY_GAME);
	 }
	
	//
	// stop functions
	//

	@Override
     protected void onPause() 
	{
		super.onPause();
		isRunning = false;
		bPaused = true;
	}


	@Override
     protected void onDestroy()
	{
		super.onDestroy();
		isRunning = false;
	}
	
	 @Override
	 protected void onStop() 
	 {
	    super.onStop();
	    isRunning = false;
	    sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));	   // sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
	 }
		 
	 // createWave <======================================================================================>
	 public void createWave()
	 {
			Math.sin(0);
	}
		 
	 /*
      * AUDIO THREAD <=====================================================================================>
      * 
      * 
      * 
      */
	 
	public void startAudio()
	{
		 
	     iMinBufSize = AudioTrack.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
	     
	     mAudioTrackX = new AudioTrack(AudioManager.STREAM_MUSIC,
	     SAMPLING_RATE,
	     AudioFormat.CHANNEL_OUT_MONO,
	     AudioFormat.ENCODING_PCM_16BIT,
	     iMinBufSize, // bytes, not samples (3 buffers)
	     AudioTrack.MODE_STREAM);
	     
	     mAudioTrackX.play();
	    	
	     if(isRunning) 
	     {
		     new Thread(new Runnable() 
		     {
		     	public void run() 
		     	{
		     		// use the audio to time the framerate
		     		while (true) // stay in a loop writing audio at 60hz
		     		{
		     				count++;
		     				
		     				
		     				makeSamples(440, 630, xVol, yVol);
		     				
					        if (isRunning == false) // game exit
					        {
							        mAudioTrackX.stop(); // game has ended
							        mAudioTrackX.release();
					        }
		
					        else 
					        {
					        	if (bPaused) 
					        	{
					        		for (int i=0; i<BLOCKSIZE; i++) 
					        		{
								        mArr[i] = 0; // silence                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         ArrY[i] = 0; // silence
					        		}
					        	}
					        	mAudioTrackX.write(mArr, 0, BLOCKSIZE);
					        }
		     		}
		     	}
		     }).start();    
	     }
	 
	 	else {
	    	 Log.d(tag, "App is no longer running, thread not run");
	     }
	 }
	 
	 public void onSensorChanged(SensorEvent event) 
	 {
		    synchronized (this) {
		        switch (event.sensor.getType()){
		            case Sensor.TYPE_ORIENTATION:
		            	x = event.values[0];
		            	y = event.values[1];
		            	z = event.values[2];
		            	
		            	//Log.d(tag, " " + xVol);
		            	//Log.d(tag, " " + yVol);
		            	
		            	
		            	xVol = Math.abs((double)z) * 300.0;
		            	yVol = Math.abs((double)y) * 300.0;
		            	
		            	
		            	
		            	
		            	modulo = (int) Math.abs(Math.ceil(z)) / 2;
		            //	Log.d(tag, " " + xVol);
		            	
		            	if(xVol > 16383 || yVol > 16383){
		            		xVol = 16382;
		            		yVol = 16382;
		            	}
		            break;
		        }
		    }
		 } 
	
	 @Override
	 public void onAccuracyChanged(Sensor sensor, int accuracy)  
	 {
	 
	 }
	
	class DrawView extends View {  
		private float touchX;
	    private float touchY;
	    private int touchwidth;
	    private int touchheight;
	    private CharSequence s;
	    
		
		
        private float x = 0f;    
        public DrawView(Context context) 
        {    
            super(context);
        }    
        
       
        
        protected void onDraw(Canvas canvas) 
        {    
    		if(circleX < 20 || circleX > -20 || circleY < 20 || circleY > -20)
        	{
    			
        	//	mp.start();
        	}
    		//mp.release();
        	
            super.onDraw(canvas);  
            
            setBackgroundColor(Color.rgb(212, 242, 191));
            
            circleX = z * scale;
            circleY = y * scale;
            
            //circleX -= calibrateX;
            //circleY -= calibrateY;
            
            // draw the crosshairs
            canvas.drawLine(halfX, 0, halfX, halfY, paintX1);
            canvas.drawLine(halfX, halfY, halfX, height, paintX2);
            canvas.drawLine(0, halfY - 50, halfX, halfY - 50, paintY1);
            canvas.drawLine(halfX, halfY - 50, width, halfY - 50, paintY2);
            
            canvas.translate(halfX, halfY);
            
            if (circleX < -halfX) circleX = -halfX;
            if (circleX > halfX - 150) circleX = halfX - 150;
            if (circleY < -halfY - 35) circleY = -halfY - 35;
            if (circleY > halfY - 300) circleY = halfY - 300;
            
            canvas.drawCircle(0, 0 - 50, 100, paintCircle);
            
            // draw the circle
        	//canvas.drawCircle(circleX, circleY, cRadius, paintCircle);
            Paint p = new Paint();
            Bitmap b=BitmapFactory.decodeResource(getResources(), R.drawable.bubble);
            
            Bitmap b2 = Bitmap.createScaledBitmap(b, 160, 160, false);
            
            p.setColor(Color.RED);
            canvas.drawBitmap(b2, circleX, circleY, p);
            
            canvas.drawCircle(0, 0 - 50, 300, paintFill);
        	
        	
        	canvas.drawRect(-300, 300, 300, 400, paintButton);
        	
        	canvas.drawCircle(0, 0 - 50, 30, paintButton);
        	
        	canvas.drawText("Calibrate Device", -260, 375, paintText); 
      
        	canvas.restore();
        	
        	canvas.drawPath(path, paintTouch);
        }
        
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
          float eventX = event.getX();
          float eventY = event.getY();

         
          switch (event.getAction()) {
          
          	case MotionEvent.ACTION_DOWN:
          		
        	  	Log.i(tag, " X:" + eventX);
	            Log.i(tag, " Y:" + eventY);
        	  
        	  if(eventX < 678 && eventX > 71 && eventY < 970 && eventY > 890) {
        		  // button only changes color when pressed down
    		  		paintButton.setColor(Color.CYAN);
    		  		paintText.setColor(Color.BLACK);
    		  		
    		  		calibrateX = (int)circleX - 0;
    		  		calibrateY = (int)circleY - -50;
    		  		
    		  		//circleX+=calibrateX;
    		  		//circleY+=calibrateY;
    		  		
    		  		// schedules a repaint
		            postInvalidate();
        	  }
        	  
          case MotionEvent.ACTION_MOVE:
        	  // nothing to do
        	  break;
          case MotionEvent.ACTION_UP:
        	  // action up reverse button back to normal color
        	 paintButton.setColor(Color.GRAY);
        	 paintText.setColor(Color.WHITE);
        	 
        	// schedules a repaint
        	 postInvalidate();
            break;
            
          default: 
            return false;
          }
          return true;
        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
