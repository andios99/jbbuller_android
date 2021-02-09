package co.kr.skycall;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class MultiImageChooserActivity extends FragmentActivity implements OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "Collage";
    public static final String COL_WIDTH_KEY = "COL_WIDTH";
    public static final String FLURRY_EVENT_ADD_MULTIPLE_IMAGES = "Add multiple images";
    private static final int DEFAULT_COLUMN_WIDTH = 120;
    public static final int NOLIMIT = -1;
    public static final String MAX_IMAGES_KEY = "MAX_IMAGES";
    private ImageAdapter ia;
    private Cursor imagecursor, actualimagecursor;
    private int image_column_index, actual_image_column_index;
    private int colWidth;
    private static final int CURSORLOADER_THUMBS = 0;
    private static final int CURSORLOADER_REAL = 1;
    private Set<String> fileNames = new HashSet<String>();
    private SparseBooleanArray checkStatus = new SparseBooleanArray();
    private Button acceptButton;
    private Button cancelButton;
    private TextView freeLabel = null;
    private int maxImages;
    private boolean unlimitedImages = false;
    private GridView gridView;
    private ExecutorService executor;

    private String requestUploadJob = "";
    private String userID = "";
    private String ordernum = "";
    private String phototype = "";
    private String mb_no = "";
    private String camera_job6 =  "";
    private Share_utils utilApp = new Share_utils(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiselectorgrid);
        executor = Executors.newCachedThreadPool();
        fileNames.clear();
        maxImages = getIntent().getIntExtra(MAX_IMAGES_KEY, NOLIMIT);
        unlimitedImages = maxImages == NOLIMIT;
        if (!unlimitedImages) {
            freeLabel = (TextView) findViewById(R.id.label_images_left);
            freeLabel.setVisibility(View.VISIBLE);
            updateLabel();
        }
        Bundle b = getIntent().getExtras();
        requestUploadJob = b.getString("requestUploadJob");
        userID = b.getString("userID");
        ordernum = b.getString("ordernum");
        phototype = b.getString("phototype");
        mb_no = b.getString("mb_no");

        /*
        b.putString("requestUploadJob", requestUploadJob );
        b.putString("userID", userID ); //회원아이디 전번
        b.putString("ordernum", camera_job3 ); //오더번호
        b.putString("phototype", camera_job4 ); //문서 또는 현장사진
        b.putString("mb_no", camera_job5 ); //전송하는 사람 고유번호
        */
        new Thread(new Runnable() {
            @Override
            public void run() {runOnUiThread(new Runnable(){
                @Override
                public void run() {findViewById(R.id.loadingPanel).setVisibility(View.GONE);}
            });}
        }).start();
        acceptButton = (Button) findViewById(R.id.btn_select);
        cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent launchIntent = new Intent(MultiImageChooserActivity.this, MainActivity.class);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Bundle b = new Bundle();
                b.putString("IntentJob", "multiuploadcancel" );
                launchIntent.putExtras(b);
                startActivityForResult(launchIntent, 34);
                finish();
            }
        });

        acceptButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectClicked(v);
            }
        });
        colWidth = getIntent().getIntExtra(COL_WIDTH_KEY, DEFAULT_COLUMN_WIDTH);
        Display display = getWindowManager().getDefaultDisplay();
        @SuppressWarnings("deprecation")
        int width = display.getWidth();
        int testColWidth = width / 3;
        if (testColWidth > colWidth) {
            colWidth = width / 4;
        }
        int bgColor = getIntent().getIntExtra("BG_COLOR", Color.BLACK);
        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setColumnWidth(colWidth);
        gridView.setOnItemClickListener(this);
        gridView.setBackgroundColor(bgColor);
        ia = new ImageAdapter(this);
        gridView.setAdapter(ia);
        LoaderManager.enableDebugLogging(false);
        getSupportLoaderManager().initLoader(CURSORLOADER_THUMBS, null, this);
        getSupportLoaderManager().initLoader(CURSORLOADER_REAL, null, this);
    }

    public String UPLOAD_Fname = "";
    public String SERVER_Upload_URL = "http://jbbuller.kr/uploade_images.php";
    public int UpDoneCnt = 0;
    public ArrayList<String> al;
    public void selectClicked(View ignored) {
        al = new ArrayList<String>();
        al.addAll(fileNames);

        //Log.e("받은값:" , "requestUploadJob :" + requestUploadJob + " / userID:" + userID+ "/ordernum:" + ordernum+ " /phototype:" + phototype + "/mb_no:"+ mb_no );
        //UpDoneCnt = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {runOnUiThread(new Runnable(){
                @Override
                public void run() {findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);}
            });}
        }).start();

        Activity mActivity = MultiImageChooserActivity.this;
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                new UpdateTask().execute();
            }
        });

        //Bundle res = new Bundle();
        //res.putStringArrayList("MULTIPLEFILENAMES", al);
        // if (imagecursor != null) {
        //   res.putInt("TOTALFILES", imagecursor.getCount());
        //}
        //Intent data = new Intent();
        //data.putExtras(res);
        //this.setResult(1, data);

        // finish();
    }

    private void uploadDone(){
        final Intent launchIntent = new Intent(MultiImageChooserActivity.this, MainActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle b = new Bundle();
        b.putString("IntentJob", "multiupload" );
        b.putString("actionjob", "multiuploadDoneIntent" );

        launchIntent.putExtras(b);
        startActivityForResult(launchIntent, 33);
        finish();
    }
    private double convertToDegree(String stringDMS){
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        double D0 = Double.valueOf(stringD[0]);
        double D1 = Double.valueOf(stringD[1]);
        double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        double M0 = Double.valueOf(stringM[0]);
        double M1 = Double.valueOf(stringM[1]);
        double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        double S0 = Double.valueOf(stringS[0]);
        double S1 = Double.valueOf(stringS[1]);
        double FloatS = S0/S1;

        result = new Float(FloatD + (FloatM/60) + (FloatS/3600));
        return result;
    };
    private class UpdateTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... urls) {
            HttpURLConnection mHttpURLConnection = null;
            DataOutputStream mOutputStream = null;
            String strLineEnd = "\r\n";
            String strTwoHyphens = "--";
            String strUpLoadServerUri = SERVER_Upload_URL;
            String strBoundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            int serverResponseCode = 0;
            Log.e("MultiImageChooser","MultiImageChooserActivityMultiImageChooserActivityMultiImageChooserActivity");
            int listSize = al.size();
            for (int i = 0; i<listSize; i++){
                UPLOAD_Fname = al.get(i);
                File sourceFile = new File(UPLOAD_Fname);
                String sourceFileUri = UPLOAD_Fname;
                int ns = listSize-1;
                camera_job6 = "nopush";
                if(i >= ns) camera_job6 = "";
                String dateString = "2000-01-01 12:30:00";
                double Latitude=0;
                double Longitude = 0;
                String gpsValue = "0,0";
                String gpsAddress = "";

                Log.e("전송하는사진: ", UPLOAD_Fname);
                if (!sourceFile.isFile()) {
                    Log.e("업로드", "업로드 파일 없음" + sourceFileUri );
                } else {
                    Log.e("업로드", "업로드 파일 OK" + sourceFileUri );
                    try {
                        final ExifInterface exifInterface = new ExifInterface(UPLOAD_Fname);
                        float[] latLong = new float[2];

                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        BasicFileAttributes attr;
                        Path path = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            path = Paths.get(UPLOAD_Fname);
                            try {
                                attr = Files.readAttributes(path, BasicFileAttributes.class);
                                System.out.println("Creation date: " + attr.creationTime());
                                FileTime dd = attr.creationTime();
                                dateString = dateFormat.format(dd.toMillis());
                                //dateString = dateFormat.format(attr.creationTime()); //lastModDate.toString();
                                Log.e("Date Info A", "Date :" + dateString);
                                //System.out.println("Last access date: " + attr.lastAccessTime());
                                //System.out.println("Last modified date: " + attr.lastModifiedTime());
                            } catch (IOException e) {
                                System.out.println("oops error! " + e.getMessage());
                            }
                        }
                        if( dateString.equals("2000-01-01 12:30:00")){
                            File file = new File(UPLOAD_Fname);
                            Date lastModDate = new Date(file.lastModified());
                            dateString = dateFormat.format(lastModDate); //lastModDate.toString();
                            Log.e("Date Info B", "Date :" + dateString);
                        }
                        Log.e("최종날짜 Info", "Date :" + dateString);

                        if (exifInterface.getLatLong(latLong)) {
                            String attrLATITUDE = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                            String attrLATITUDE_REF = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                            String attrLONGITUDE = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                            String attrLONGITUDE_REF = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                            if((attrLATITUDE !=null)&& (attrLATITUDE_REF !=null)&& (attrLONGITUDE != null)&& (attrLONGITUDE_REF !=null)) {
                                if (attrLATITUDE_REF.equals("N")) {Latitude = utilApp.convertToDegree(attrLATITUDE);} else {Latitude = 0 - utilApp.convertToDegree(attrLATITUDE);}
                                if (attrLONGITUDE_REF.equals("E")) {Longitude = utilApp.convertToDegree(attrLONGITUDE);} else {Longitude = 0 - utilApp.convertToDegree(attrLONGITUDE);}
                            }
                        }

                    } catch (IOException e) {
                        Log.e("GPSERROR","Couldn't read exif info: " + e.getLocalizedMessage());
                    }
                    File oFile = new File(sourceFileUri);

                    if (oFile.exists()) {
                        long L = oFile.length();
                        System.out.println( "축소이전 크기 : " + L + " bytes : " + oFile.getAbsoluteFile());
                    }
                    sourceFileUri = resizeImg(sourceFileUri,0);
                    oFile = new File(sourceFileUri);
                    if (oFile.exists()) {
                        long L = oFile.length();
                        System.out.println("축소 후 크기 : " + L + " bytes : " + oFile.getAbsoluteFile());
                    }
                    ///////////////////
                    /////////////// 업로드소스 여기에
                    Log.e("파일생일", dateString + " / " + sourceFileUri);
                    gpsValue = Latitude + "," + Longitude;
                    gpsAddress = utilApp.getAddress(Latitude,Longitude);
                    String str_encode = "";
                    try{
                        str_encode = URLEncoder.encode(gpsAddress,"UTF-8");
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    Log.e("gpsValue", "gpsValue : " + gpsValue + " / gpsAddress : " + gpsAddress);
                    Log.e("str_encode", "str_encode : " + str_encode);

                    try {
                        FileInputStream fileInputStream = new FileInputStream(sourceFile);
                        URL url = new URL(strUpLoadServerUri);
                        mHttpURLConnection = (HttpURLConnection) url.openConnection();
                        mHttpURLConnection.setDoInput(true); // Allow Inputs
                        mHttpURLConnection.setDoOutput(true); // Allow Outputs
                        mHttpURLConnection.setUseCaches(false); // Don't use a Cached Copy
                        mHttpURLConnection.setRequestMethod("POST");
                        mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                        mHttpURLConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                        mHttpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + strBoundary);
                        mOutputStream = new DataOutputStream(mHttpURLConnection.getOutputStream());
                        mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                        mOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + sourceFileUri + "\"" + strLineEnd);

                        mOutputStream.writeBytes(strLineEnd);
                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];
                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        while (bytesRead > 0) {
                            mOutputStream.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        }
                        mOutputStream.writeBytes(strLineEnd);
                        mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                        ///////////////////////////////////////////////////////////////////////////////////;
                        mOutputStream.writeBytes("Content-Disposition: form-data; name=\"jobtype\"" + strLineEnd);
                        mOutputStream.writeBytes(strLineEnd);
                        mOutputStream.writeBytes(requestUploadJob + strLineEnd);
                        mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                        ///////////////////////////////////////////////////////////////////////////////////;
                        mOutputStream.writeBytes("Content-Disposition: form-data; name=\"type2\"" + strLineEnd);
                        mOutputStream.writeBytes(strLineEnd);
                        mOutputStream.writeBytes(userID + strLineEnd);
                        mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                        ///////////////////////////////////////////////////////////////////////////////////;
                        mOutputStream.writeBytes("Content-Disposition: form-data; name=\"type3\"" + strLineEnd);
                        mOutputStream.writeBytes(strLineEnd);
                        mOutputStream.writeBytes(ordernum + strLineEnd);
                        mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                        ///////////////////////////////////////////////////////////////////////////////////;
                        mOutputStream.writeBytes("Content-Disposition: form-data; name=\"type4\"" + strLineEnd);
                        mOutputStream.writeBytes(strLineEnd);
                        mOutputStream.writeBytes(phototype + strLineEnd);
                        mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                        ///////////////////////////////////////////////////////////////////////////////////;
                        mOutputStream.writeBytes("Content-Disposition: form-data; name=\"type5\"" + strLineEnd);
                        mOutputStream.writeBytes(strLineEnd);
                        mOutputStream.writeBytes(mb_no + strLineEnd);
                        mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                        ///////////////////////////////////////////////////////////////////////////////////;

                        mOutputStream.writeBytes("Content-Disposition: form-data; name=\"fileage\"" + strLineEnd);
                        mOutputStream.writeBytes(strLineEnd);
                        mOutputStream.writeBytes(dateString + strLineEnd);
                        mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                        ///////////////////////////////////////////////////////////////////////////////////;
                        mOutputStream.writeBytes("Content-Disposition: form-data; name=\"gpsValue\"" + strLineEnd); //
                        mOutputStream.writeBytes(strLineEnd);
                        mOutputStream.writeBytes(gpsValue + strLineEnd);
                        mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                        ///////////////////////////////////////////////////////////////////////////////////;

                        mOutputStream.writeBytes("Content-Disposition: form-data; name=\"gpsaddress\"" + strLineEnd);
                        mOutputStream.writeBytes(strLineEnd);
                        mOutputStream.writeBytes(str_encode + strLineEnd);
                        mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                        ///////////////////////////////////////////////////////////////////////////////////;


                        mOutputStream.writeBytes("Content-Disposition: form-data; name=\"type6\"" + strLineEnd);
                        mOutputStream.writeBytes(strLineEnd);
                        mOutputStream.writeBytes(camera_job6 + strLineEnd);
                        mOutputStream.writeBytes(strTwoHyphens + strBoundary + strTwoHyphens + strLineEnd);
                        ///////////////////////////////////////////////////////////////////////////////////;
                        serverResponseCode = mHttpURLConnection.getResponseCode();
                        fileInputStream.close();
                        mOutputStream.flush();
                        mOutputStream.close();
                        Log.e("전송결과", "jobtype:" + requestUploadJob + " serverResponseCode:" + serverResponseCode );

                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                        Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("error:" + e.getMessage(), "Fie upload error");
                    }
                    if (sourceFileUri.length() > 5) {
                        File f = new File(sourceFileUri);
                        Boolean deleted = f.delete();
                        Log.e("임시 파일삭제", "Filename :" + deleted + " / " + sourceFileUri );
                    }

                    //////////////

                }
            }
            uploadDone();
            new Thread(new Runnable() {
                @Override
                public void run() {runOnUiThread(new Runnable(){
                    @Override
                    public void run() {findViewById(R.id.loadingPanel).setVisibility(View.GONE);}
                });}
            }).start();
            return UPLOAD_Fname;
        }
    }
    public String resizeImg(String path,int iOrder){
        Bitmap resultBitmap = null;
        String orgPath = path;
        ContentResolver resolver = MultiImageChooserActivity.this.getContentResolver();
        Uri uri = Uri.fromFile(new File(path)); ;//Uri.parse(path);
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE ; // 1.2MP
            if( requestUploadJob.equals("personimageupload")) {
                IMAGE_MAX_SIZE = 500000;
            }else{
                IMAGE_MAX_SIZE= 1800000;
            }
            in = resolver.openInputStream(uri);
            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            int scale = 1;
            while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
                scale++;
            }

            in = resolver.openInputStream(uri);
            ExifInterface exif = new ExifInterface(path);
            //int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            String Image_rotate = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = Image_rotate != null ? Integer.parseInt(Image_rotate) : ExifInterface.ORIENTATION_NORMAL;
            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                options = new BitmapFactory.Options();
                options.inSampleSize = scale;
                resultBitmap = BitmapFactory.decodeStream(in, null, options);

                // resize to desired dimensions
                int height = resultBitmap.getHeight();
                int width = resultBitmap.getWidth();
                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
                double x = (y / height) * width;
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(resultBitmap, (int) x, (int) y, true);
                resultBitmap.recycle();
                resultBitmap = scaledBitmap;
                if( rotationAngle > 0 ) {
                    resultBitmap = rotateImage(resultBitmap,rotationAngle); ////////////이미지 회전
                    height = resultBitmap.getHeight();
                    width = resultBitmap.getWidth();
                }else{
                }

                int ImgQulity = 92;

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                resultBitmap.compress(Bitmap.CompressFormat.JPEG, ImgQulity, bytes);
                String[] fex = path.split("\\.");
                String exten = fex[fex.length -1];
                String mid_n = String.valueOf(System.currentTimeMillis());
                File file = new File(Environment.getExternalStorageDirectory(), "/JbBuller/");
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        Toast.makeText(getApplicationContext(), "Uploading canceld(Cound not Make temp folder)", Toast.LENGTH_SHORT).show();
                        return "";
                    }
                }
                String new_Fn = Environment.getExternalStorageDirectory() + File.separator + "JbBuller/mcTempFile" + iOrder + "_" + mid_n + "." + exten;
                File f = new File(new_Fn);
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                fo.close();
                String f_ex = "모름";
                if(f.exists()){
                    f_ex = "파일 있음 ";
                }else{
                    f_ex = "파일 없음ㅌㅌ ";
                }
                path = new_Fn;

            } else {
                resultBitmap = BitmapFactory.decodeStream(in);
            }
            in.close();
            return path;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return path;
        }
    }
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        return rotatedImg;
    }
    private void updateLabel() {
        if (freeLabel != null) {
            String text = String.format(getString(R.string.free_version_label),
                    maxImages);
            freeLabel.setText(text);
            if (maxImages == 0) {
                freeLabel.setTextColor(Color.RED);
            } else {
                freeLabel.setTextColor(Color.WHITE);
            }
        }
    }
    public class ImageAdapter extends BaseAdapter {
        private final Matrix m = new Matrix();
        private Canvas canvas;
        private final Bitmap mPlaceHolderBitmap;

        public ImageAdapter(Context c) {
            Bitmap tmpHolderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading_icon);
            mPlaceHolderBitmap = Bitmap.createScaledBitmap(tmpHolderBitmap,colWidth, colWidth, false);
            if (tmpHolderBitmap != mPlaceHolderBitmap) {
                tmpHolderBitmap.recycle();
                tmpHolderBitmap = null;
            }
        }

        public int getCount() {
            if (imagecursor != null) {
                return imagecursor.getCount();
            } else {
                return 0;
            }
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int pos, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = new ImageView(MultiImageChooserActivity.this);
            }
            ImageView imageView = (ImageView) convertView;
            final int position = pos;
            imageView.setBackgroundColor(Color.TRANSPARENT);
            imageView.setImageBitmap(mPlaceHolderBitmap);
            if (!imagecursor.moveToPosition(position)) {
                return imageView;
            }
            if (image_column_index == -1) {
                return imageView;
            }
            final int id = imagecursor.getInt(image_column_index);
            imageView.setImageBitmap(mPlaceHolderBitmap);
            final WeakReference<ImageView> ivRef = new WeakReference<ImageView>(
                    imageView);
            Runnable theRunnable = new Runnable() {

                private void setInvisible() {
                    if (ivRef.get() == null) {
                        return;
                    } else {
                        final ImageView iv = (ImageView) ivRef.get();
                        if (iv == null) {
                            return;
                        } else {
                            MultiImageChooserActivity.this
                                    .runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            iv.setVisibility(View.GONE);
                                            iv.setClickable(false);
                                            iv.setEnabled(false);
                                        }
                                    });
                        }
                    }
                }

                @Override
                public void run() {
                    Bitmap thumb = MediaStore.Images.Thumbnails.getThumbnail(
                            getContentResolver(), id,
                            MediaStore.Images.Thumbnails.MICRO_KIND, null);

                    if (thumb == null) {
                        // The original image no longer exists, hide the image
                        // cell
                        setInvisible();
                        return;
                    } else {
                        final Bitmap mutable = Bitmap.createBitmap(colWidth,colWidth, thumb.getConfig());
                        if (mutable == null) {
                            setInvisible();
                            return;
                        }
                        canvas = new Canvas(mutable);
                        if (canvas == null) {
                            setInvisible();
                            return;
                        }

                        RectF src = new RectF(0, 0, thumb.getWidth(),
                                thumb.getHeight());
                        RectF dst = new RectF(0, 0, canvas.getWidth(),
                                canvas.getHeight());
                        m.reset();
                        m.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
                        canvas.drawBitmap(thumb, m, null);

                        thumb.recycle();
                        thumb = null;

                        MultiImageChooserActivity.this
                                .runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (ivRef.get() == null) {
                                            return;
                                        } else {
                                            final ImageView iv = (ImageView) ivRef.get();
                                            if (iv == null) {
                                                return;
                                            } else {
                                                if (isChecked(position)) {
                                                    iv.setBackgroundColor(Color.RED);
                                                }
                                                iv.setImageBitmap(mutable);
                                            }
                                        }
                                    }
                                });
                    }
                }
            };

            new Thread(theRunnable).start();

            return imageView;
        }
    }
    private String getImageName(int position) {
        actualimagecursor.moveToPosition(position);
        String name = null;

        try {
            name = actualimagecursor.getString(actual_image_column_index);
        } catch (Exception e) {
            return null;
        }
        return name;
    }
    private void setChecked(int position, boolean b) {
        checkStatus.put(position, b);
    }
    public boolean isChecked(int position) {
        boolean ret = checkStatus.get(position);
        return ret;
    }
    public void cancelClicked(View ignored) {
        setResult(RESULT_CANCELED);
        finish();
    }
    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position,long id) {
        String name = getImageName(position);
        if (name == null) {
            return;
        }
        boolean isChecked = !isChecked(position);
        // PhotoMix.Log("DAVID", "Posicion " + position + " isChecked: " +
        // isChecked);
        if (!unlimitedImages && maxImages == 0 && isChecked) {
            // PhotoMix.Log("DAVID", "Aquí no debería entrar...");
            isChecked = false;
        }

        if (isChecked) {
            // Solo se resta un slot si hemos introducido un
            // filename de verdad...
            if (fileNames.add(name)) {
                maxImages--;
                view.setBackgroundColor(Color.RED);
            }
        } else {
            if (fileNames.remove(name)) {
                // Solo incrementa los slots libres si hemos
                // "liberado" uno...
                maxImages++;
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        setChecked(position, isChecked);
        acceptButton.setEnabled(fileNames.size() != 0);
        updateLabel();

    }
    @Override
    public Loader<Cursor> onCreateLoader(int cursorID, Bundle arg1) {
        CursorLoader cl = null;

        ArrayList<String> img = new ArrayList<String>();
        switch (cursorID) {

            case CURSORLOADER_THUMBS:
                img.add(MediaStore.Images.Media._ID);
                break;
            case CURSORLOADER_REAL:
                img.add(MediaStore.Images.Thumbnails.DATA);
                break;
            default:
                break;
        }
        String sortOrder = MediaStore.Images.ImageColumns.DATE_ADDED + " DESC";
        cl = new CursorLoader(MultiImageChooserActivity.this,MediaStore.Images.Media.EXTERNAL_CONTENT_URI,img.toArray(new String[img.size()]), null, null, sortOrder);
        return cl;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) {
            // NULL cursor. This usually means there's no image database yet....
            return;
        }
        switch (loader.getId()) {
            case CURSORLOADER_THUMBS:
                imagecursor = cursor;
                image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
                ia.notifyDataSetChanged();
                break;
            case CURSORLOADER_REAL:
                actualimagecursor = cursor;
                actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                break;
            default:
                break;
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CURSORLOADER_THUMBS) {
            imagecursor = null;
        } else if (loader.getId() == CURSORLOADER_REAL) {
            actualimagecursor = null;
        }
    }
}