package co.kr.skycall;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.io.InputStream;
import java.net.URL;

public class Image_Zoomer_acitivty extends PagerAdapter {
    Context context;
    private ImageView imageView;
    private String[] GalImages = null; //new String[] {} ;
            //
    /* {"http://jbbuller.kr/datas/chat_data/6173/up_10_0_20190529144911.jpg","http://jbbuller.kr/datas/chat_data/6173/up_10_0_20190529145123.jpg","http://jbbuller.kr/datas/chat_data/6173/up_10_0_20190529145155.jpg","http://jbbuller.kr/datas/chat_data/6173/up_10_0_20190529150419.jpg","http://jbbuller.kr/datas/chat_data/6173/MUP_10_appUp_05291518195176_main.jpg","http://jbbuller.kr/datas/chat_data/6173/MUP_10_appUp_05291518198191_main.jpg","http://jbbuller.kr/datas/chat_data/6173/MUP_10_appUp_05291518199512_main.jpg","http://jbbuller.kr/datas/chat_data/6173/MUP_10_appUp_05291518199236_main.jpg","http://jbbuller.kr/datas/chat_data/6173/MUP_10_appUp_05291624229789_main.jpg"
    };
    */

    float[] lastEvent = null;
    float d = 0f;
    float newRot = 0f;
    private boolean isZoomAndRotate;
    private boolean isOutSide;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    float oldDist = 1f;
    private float xCoOrdinate, yCoOrdinate;
    private int clickCnt = 0;
    private long startTime = 0;
    static final int MAX_DURATION = 200;
    private boolean enabled;
    private View TempView = null;
    private TextView tv;
    private int NowPage;
    Image_Zoomer_acitivty(Context context, String FF , int stnum, TextView tvv){
        this.context=context;
        this.enabled = true;
        //this.
        tv = tvv;
        this.GalImages = FF.split(",");
        NowPage = stnum;
        Log.e("시작", "사작점:" + stnum + "/ " + GalImages.toString() + "/" + tv);
    }


    @Override
    public int getCount() {
        return GalImages.length;
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ImageView) object);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public Object instantiateItem(ViewGroup container, int position) { //PagerAdapter
        Log.e("ISZOOM", "ZOOM :" + ZOOM + "/position:" + position);

        imageView = new ImageView(context);
        int padding = context.getResources().getDimensionPixelSize(R.dimen.padding_medium);
        imageView.setPadding(0, 0, 0, 0);
        //imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //imageView.setScaleX(1);
        //imageView.setScaleY(1);
        //imageView.animate().x(0).y(0).setDuration(0).start();
        //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);


        new LoadImage(imageView).execute(GalImages[position]);
        ((ViewPager) container).addView(imageView, 0);
        clickCnt = 0;
        NowPage = position;
        String ns = String.valueOf(NowPage) + "/" + String.valueOf (GalImages.length);
        //tv.setText(ns);
        Drawable drawable = context.getResources().getDrawable(R.drawable.loading_still_fit50);
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        imageView.setImageBitmap(bitmap);



        Log.e("Loading :" + position , GalImages[position] );
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                viewTransformation(v, event);
                //if( mode == ZOOM ) return false;
                return true;
            }
        });
        return imageView;
    }

    private void viewTransformation(View view, MotionEvent event) { //PagerAdapter
        if (event.getAction() == MotionEvent.ACTION_UP) {
            startTime = System.currentTimeMillis();
        }else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            long tts = System.currentTimeMillis() - startTime;
            if(System.currentTimeMillis() - startTime <= MAX_DURATION) {
                float ts = view.getScaleX();
                String ZoomSet = "NO";
                if (ts > 1 || ts < 1 ) {
                    view.setScaleX(1);
                    view.setScaleY(1);
                    mode = NONE;
                } else{
                    view.setScaleX(3);
                    view.setScaleY(3);
                    mode = ZOOM;
                }
                return;
            }
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xCoOrdinate = view.getX() - event.getRawX();
                yCoOrdinate = view.getY() - event.getRawY();
                start.set(event.getX(), event.getY());
                isOutSide = false;
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {midPoint(mid, event);mode = ZOOM;}
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
                isZoomAndRotate = false;
                if (mode == DRAG) {
                    float x = event.getX();
                    float y = event.getY();
                }
            case MotionEvent.ACTION_OUTSIDE:
                isOutSide = true;
                mode = NONE;
                lastEvent = null;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                float  Spp = 0;
                if(event.getPointerCount() == 2) Spp = spacing(event);
                if (!isOutSide) {
                    if (mode == DRAG) {
                        isZoomAndRotate = false;
                        float ts = view.getScaleX();
                        if( ts > 1) view.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                    }
                    if (mode == ZOOM && event.getPointerCount() == 2) {
                        float newDist1 = spacing(event);
                        if (newDist1 > 10f) {
                            float scale = newDist1 / oldDist * view.getScaleX();
                            view.setScaleX(scale);
                            view.setScaleY(scale);
                            //Log.e("크기조절",  "Scale :" + scale);
                        }
                        if (lastEvent != null) {
                            //newRot = rotation(event);
                            //view.setRotation((float) (view.getRotation() + (newRot - d)));
                        }
                    }
                }
                break;
        }

    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (int) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }



    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        ImageView img=null;
        public LoadImage(ImageView img){
            //Log.e("DownLoading" , "LoadImage");
            this.img=img;
            String ns = String.valueOf(NowPage) + "/" + String.valueOf (GalImages.length);
            //tv.setText(ns);

        }
        @Override
        protected void onPreExecute() {
            //Log.e("DownLoading" , "onPreExecute");
            super.onPreExecute();

        }
        protected Bitmap doInBackground(String... args) {
            Bitmap bitmap=null;
            //Log.e("DownLoading" , "doInBackground");
            try {
                bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        protected void onPostExecute(Bitmap image) {
            //Log.e("DownLoading" , "--------------onPostExecute");
            if(image != null){
                img.setImageBitmap(image);
                String ns = String.valueOf(NowPage) + "/" + String.valueOf (GalImages.length);
                //tv.setText(ns);
            }
        }
    }




    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
    }



}
