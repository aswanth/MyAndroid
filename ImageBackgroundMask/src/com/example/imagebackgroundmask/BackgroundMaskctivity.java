package com.example.imagebackgroundmask;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class BackgroundMaskctivity extends Activity {

	ImageView androidImg, botomImg;
	Button camButton, maskButton, prevButton;
	Bitmap bitmap, baseBitmap;
	Boolean mask = true;
	Thread Bitmaphandler;
	int threshold = 50;
	private SeekBar thresholdBar;
	private static final int CAMERA_REQUEST = 1888;
	private ArrayList<point> processedPixels = new ArrayList<BackgroundMaskctivity.point>();
	private ArrayList<Bitmap> bitmapStack = new ArrayList<Bitmap>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_maskctivity);
        botomImg = androidImg = (ImageView) findViewById(R.id.bottomLayer);
        androidImg = (ImageView) findViewById(R.id.topLayer);
//        BitmapFactory.Options opt = new BitmapFactory.Options();
//        opt. = true;
        
        camButton = (Button) findViewById(R.id.cameraButton);
        maskButton = (Button) findViewById(R.id.maskButton);
        prevButton = (Button) findViewById(R.id.prevButton);
        thresholdBar = (SeekBar) findViewById(R.id.thresholdBar);
        
        thresholdBar.setMax(100);
        thresholdBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					threshold = progress;
				}
				
			}
		});
        
//        Bitmap immutable = BitmapFactory.decodeResource(getResources() , R.drawable.android); 
//        bitmap = immutable.copy(Bitmap.Config.ARGB_8888, true);
//        androidImg.setImageBitmap(bitmap);
//        prevButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if (bitmapStack.size() > 1) {
//					bitmapStack.remove(bitmapStack.size() - 1);
//					bitmap = bitmapStack.get(bitmapStack.size() - 1);
//					androidImg.setImageBitmap(bitmap);
//					eraseMask(0, 0);
//				}
//			}
//		});
        camButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
			}
		});
        
        maskButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mask){
					maskButton.setText("Mask");
				}else{
					maskButton.setText("Erase");
				}
				mask = !mask;
			}
		});
        
        androidImg.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
//				int yVal = (int) (event.getY() - androidImg.getTop());
//				int xVal = (int) (event.getX() - androidImg.getLeft());
				
				int yVal = (int) event.getY();
				int xVal = (int) event.getX();
				if(bitmap == null)
						return false;
				if(mask && event.getAction() == MotionEvent.ACTION_DOWN){
					setEasyMask(xVal,yVal);
//					setMask(xVal, yVal);
				}else if(!mask){
					eraseMask(xVal,yVal);
				}
				
//				setEasyMask(xVal, yVal);
				try {
					Log.i("x-y", "y-" + event.getY() + "x-" + event.getX()
							+ "x-" + xVal + "y-" + yVal);

					Log.i("params",
							"top-" + androidImg.getTop() + ",left-"
									+ androidImg.getLeft() + ",heigth-"
									+ bitmap.getHeight() + "-"
									+ androidImg.getHeight() + ",width-"
									+ bitmap.getWidth() + "-"
									+ androidImg.getWidth());
				}catch (Exception e) {
					// TODO: handle exception
				}
//				int currentPixel = bitmap.getPixel(xVal, yVal);
//				
//				for(int i = 0; i < bitmap.getHeight(); i++)
//    				for (int j = 0; j < bitmap.getWidth(); j++) {
//    					if (bitmap.getPixel(j, i) == currentPixel)
//    							setMask(j, i);
//    				}
//				androidImg.setImageBitmap(bitmap);
				
				return true;
			}
		});
        
//        androidImg.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				white = !white;
//				for(int i = 0; i < bitmap.getHeight(); i++)
//    				for (int j = 0; j < bitmap.getWidth(); j++) {
//    					if (bitmap.getPixel(j, i) == Color.WHITE && white && bitmap.isMutable())
//    						bitmap.setPixel(j, i, Color.BLACK);
//    					if (bitmap.getPixel(j, i) == Color.BLACK && !white && bitmap.isMutable())
//    						bitmap.setPixel(j, i, Color.WHITE);
//    				}
//				androidImg.setImageBitmap(bitmap);
//			}
//		});
        
    }
    
    public void setEasyMask(int x, int y){
    	
    	Bitmap bmp = bitmap;
//    	int i = y,j = x;
    	Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
    	Canvas c = new Canvas(result);
    	Paint paint = new Paint();
    	paint.setAntiAlias(true);
    	paint.setXfermode(new PorterDuffXfermode(Mode.LIGHTEN));
//    	c.drawBitmap(bitmap, 0, 0, null);
//    	paint.setAlpha(200);
//    	paint.setStrokeWidth(10);
//    	paint.setColor(Color.BLACK);
    	
//    	try{
//    		for(; bmp.getPixel(j, i) == bmp.getPixel(x, y); i++ ){
//        		for(; bmp.getPixel(j, i) == bmp.getPixel(x, y); j++ ){
////        			c.drawCircle(j, i, 5,paint);
//        		}
//        		c.drawLine(x, i, x + j - 1, i, paint);
//        		j = x;
//    		}
//    	}catch (Exception e) {
//		}
//    	c.drawBitmap(bitmap, 0, 0, null);
//    	c.drawCircle(x, y, 25,paint);
//    	paint.setColor(color.black);
//    	c.drawCircle(100, 100, 25, paint);
//    	c.drawBitmap(mask, x, y, paint);
//    	paint.setXfermode(null);
//    	bitmap = result;
//    	int pix = baseBitmap.getPixel(x, y), pixCount = 0;
//    	int red = Color.red(pix), varRed;
//    	int green = Color.green(pix), varGreen;
//    	int blue = Color.blue(pix), varBlue;
//    	int varPix;
//    	int threshold = 50;
////    	float[] mesh = new float[bitmap.getHeight() * bitmap.getWidth()];
//		try {
//			for (int i = 0; i < bitmap.getHeight(); i++) {
//				for (int j = 0; j < bitmap.getWidth(); j++) {
//					// if(bitmap.getPixel(j, i) == bitmap.getPixel(x, y)){
//					// bitmap.setPixel(j, i, Color.BLACK);
//					// }
//					varPix = baseBitmap.getPixel(j, i);
//					varRed = Color.red(varPix);
//					varGreen = Color.green(varPix);
//					varBlue = Color.blue(varPix);
////					mesh[pixCount] = 
//					if (Math.abs(red - varRed) < threshold
//							&& Math.abs(green - varGreen) < threshold
//							&& Math.abs(blue - varBlue) < threshold) {
//						bitmap.setPixel(j, i, Color.BLACK);
////						c.drawPoint(j, i, paint);
//					}
//
//				}
//			}
//		}catch (Exception e) {
//    		
//		}
		try {
			int pix = baseBitmap.getPixel(x, y);
//			processedPixels.clear();
//			processPixel(new point(x, y), pix);
			processRegion1(pix, x, y);
			processRegion2(pix, x, y);
			processRegion3(pix, x, y);
			processRegion4(pix, x, y);
			c.drawBitmap(bitmap, 0, 0, null);
			// c.drawBitmapMesh(result, meshWidth, meshHeight, verts,
			// vertOffset, null, null, null);
			bitmap = result;
//			bitmapStack.add(result);
			androidImg.setImageBitmap(result);
		}catch (Exception e) {
			// TODO: handle exception
		}
    	
    }
    
    
	public void processRegion1(int pix, int x, int y) {
		try {
			int varPix, i = y, j = x;
			for (i = y; i < bitmap.getHeight()
					&& checkPixels(pix, baseBitmap.getPixel(j, i)); i++) {
				for (j = x; j < bitmap.getWidth()
						&& checkPixels(pix, baseBitmap.getPixel(j, i)); j++) {

					varPix = baseBitmap.getPixel(j, i);

					if (checkPixels(pix, varPix)) {
						bitmap.setPixel(j, i, Color.BLACK);
						// c.drawPoint(j, i, paint);
					}

				}
				j=x;
			}
		} catch (Exception e) {

		}
	}
	public void processRegion2(int pix, int x, int y) {
		try {
			int varPix, i = y, j = x;
			for (i = y; i < bitmap.getHeight()
					&& checkPixels(pix, baseBitmap.getPixel(j, i)); i++) {
				for (j = x; j >= 0
						&& checkPixels(pix, baseBitmap.getPixel(j, i)); j--) {

					varPix = baseBitmap.getPixel(j, i);

					if (checkPixels(pix, varPix)) {
						bitmap.setPixel(j, i, Color.BLACK);
						// c.drawPoint(j, i, paint);
					}

				}
				j=x;
			}
		} catch (Exception e) {

		}
	}
	
	public void processRegion3(int pix, int x, int y) {
		try {
			int varPix, i = y, j = x;
			for (i = y; i >=0
					&& checkPixels(pix, baseBitmap.getPixel(j, i)); i--) {
				for (j = x; j >= 0
						&& checkPixels(pix, baseBitmap.getPixel(j, i)); j--) {

					varPix = baseBitmap.getPixel(j, i);

					if (checkPixels(pix, varPix)) {
						bitmap.setPixel(j, i, Color.BLACK);
						// c.drawPoint(j, i, paint);
					}

				}
				j=x;
			}
		} catch (Exception e) {

		}
	}
	public void processRegion4(int pix, int x, int y) {
		try {
			int varPix, i = y, j = x;
			for (i = y; i >=0
					&& checkPixels(pix, baseBitmap.getPixel(j, i)); i--) {
				for (j = x; j < bitmap.getWidth()
						&& checkPixels(pix, baseBitmap.getPixel(j, i)); j++) {

					varPix = baseBitmap.getPixel(j, i);

					if (checkPixels(pix, varPix)) {
						bitmap.setPixel(j, i, Color.BLACK);
						// c.drawPoint(j, i, paint);
					}

				}
				j=x;
			}
		} catch (Exception e) {

		}
	}
    
    public void eraseMask(float x, float y) {
    	Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
    	Canvas c = new Canvas(result);
    	Paint paint = new Paint();
    	paint.setAntiAlias(true);
    	paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
    	c.drawBitmap(bitmap, 0, 0, null);
//    	paint.setAlpha(200);
//    	paint.setStrokeWidth(10);
    	paint.setColor(Color.BLACK);
    	c.drawCircle(x, y, 50,paint);
//    	paint.setColor(color.black);
//    	c.drawCircle(100, 100, 25, paint);
//    	c.drawBitmap(mask, x, y, paint);
//    	paint.setXfermode(null);
    	bitmap = result;
    	androidImg.setImageBitmap(result);
	}
    
    public void setMask(float x, float y) {
		
//    	Resources resources = this.getResources();
//    	Bitmap mask = BitmapFactory.decodeResource(getResources(),R.drawable.mask);
//    	int width=bitmap.getWidth();
//    	int height=bitmap.getHeight();
//    	Bitmap resizedbitmap=Bitmap.createBitmap(mask, width, height, true);

    	Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
    	Canvas c = new Canvas(result);
    	Paint paint = new Paint();
    	paint.setAntiAlias(true);
//    	paint.setXfermode(new PorterDuffXfermode(Mode.LIGHTEN));
    	c.drawBitmap(bitmap, 0, 0, null);
//    	paint.setAlpha(200);
//    	paint.setStrokeWidth(10);
    	paint.setColor(Color.BLACK);
    	c.drawCircle(x, y, 25,paint);
//    	paint.setColor(color.black);
//    	c.drawCircle(100, 100, 25, paint);
//    	c.drawBitmap(mask, x, y, paint);
//    	paint.setXfermode(null);
    	bitmap = result;
    	androidImg.setImageBitmap(result);


    	
	}
    private int stackSize = 0;
    public void processPixel(point p, int pix) {
    	
    	stackSize++;
    	Log.i("stack size-", ""+stackSize);
		
    	if(p.x >= bitmap.getWidth() || p.y >= bitmap.getHeight() || p.x < 0 ||p.y < 0){
    		return;
    	}else if(isProcessed(p) ){
    		Log.i("processPixel", "loop tracked");
    		return;
    	}else if(checkPixels(bitmap.getPixel(p.x, p.y) ,pix)){
    		Log.i("pos", ""+p.x + "-"+p.y+ "-width-" + bitmap.getWidth()+ "-height-"+bitmap.getHeight());
				bitmap.setPixel(p.x, p.y, Color.BLACK);
				processedPixels.add(p);
				processPixel(new point(p.x + 1, p.y), pix);
				processPixel(new point(p.x + 1, p.y + 1), pix);
				processPixel(new point(p.x + 1, p.y - 1), pix);
				processPixel(new point(p.x, p.y + 1), pix);
				processPixel(new point(p.x, p.y - 1), pix);
				processPixel(new point(p.x - 1, p.y), pix);
				processPixel(new point(p.x - 1, p.y + 1), pix);
				processPixel(new point(p.x - 1, p.y - 1), pix);
    	}
    	
	}
    
    public boolean isProcessed(point p) {
    	
    	for(int i = 0; i < processedPixels.size(); i++){
    		
    		if(p.x == processedPixels.get(i).x && p.y == processedPixels.get(i).y){
    			return true;
    		}
    		
    	}
    	
		return false;
	}
    
    public boolean checkPixels(int pix1, int pix2) {
		
    	
    	int varRed = Color.red(pix1);
    	int varGreen = Color.green(pix1);
    	int varBlue = Color.blue(pix1);
    	int red = Color.red(pix2);
    	int green = Color.green(pix2);
    	int blue = Color.blue(pix2);
		if (Math.abs(red - varRed) < threshold
				&& Math.abs(green - varGreen) < threshold
				&& Math.abs(blue - varBlue) < threshold) {
			return true;
		}
    	
    	return false;
	}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && data != null) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
			if (photo != null) {
				bitmap = photo.copy(Bitmap.Config.ARGB_8888, true);
				bitmap = Bitmap.createScaledBitmap(bitmap,
						androidImg.getWidth(), androidImg.getHeight(), true);
				baseBitmap = bitmap;
				bitmapStack.add(baseBitmap);
				botomImg.setImageBitmap(bitmap);
				androidImg.setImageBitmap(bitmap);
				eraseMask(50, 50);
				// setMask(50, 50);
			}
        }
}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.background_maskctivity, menu);
        return true;
    }
    class point{
    	public int x, y;
    	public point(int x, int y) {
    		
    		this.x = x;
    		this.y = y;
		}
    }
    
}
