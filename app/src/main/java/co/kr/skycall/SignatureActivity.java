package co.kr.skycall;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import static android.graphics.Color.parseColor;

public class SignatureActivity extends Activity {

    public String SignFn = "";
    public String serverFn = "";
    public String orderNum = "";
    public int progressChangedValue = 10;
    public String workDetail = "";
    public String doc_price = "";
    public String ww_addr = "";
    public String ww_day = "";
    public String Payed = "미수";
    private String workInfo_coname = "";
    private String workInfo_cotel = "";
    public String dialog_title = "작업을 종료할까요?";
    public String dialog_cont = "작업종료 이 후 작업확인서 수정을 하실 수 없습니다.";
    private AlertDialog dialog;
    public int pathCnt = 0;
    Button mClear, mGetSign, mCancel;
    File file;
    LinearLayout mContent;
    View view;
    signature mSignature;
    Bitmap bitmap;

    // Creating Separate Directory for saving Generated Images
    String DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/UserSignature/";
    String pic_name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    String StoredPath = DIRECTORY + pic_name + ".png";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        this.setFinishOnTouchOutside(false);
        mContent = (LinearLayout) findViewById(R.id.canvasLayout);
        mSignature = new signature(getApplicationContext(), null);
        //mSignature.setBackgroundColor(Color.WHITE);
        mContent.setBackgroundResource(R.drawable.sign_bg01);//to set background
        // Dynamically generating Layout through java code
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mClear = (Button) findViewById(R.id.clear);
        mGetSign = (Button) findViewById(R.id.getsign);
        mGetSign.setEnabled(false);
        mCancel = (Button) findViewById(R.id.cancel);
        view = mContent;


        mGetSign.setOnClickListener(onButtonClick);
        mClear.setOnClickListener(onButtonClick);
        mCancel.setOnClickListener(onButtonClick);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mCancel.getLayoutParams();
        params.setMargins(0, 0, 5, 0); //left, top, right, bottom
        mCancel.setLayoutParams(params);
        mClear.setLayoutParams(params);
        findViewById(R.id.signProgreeBg).setVisibility(View.GONE);
        Bundle bundle = getIntent().getExtras();
        final EditText TelTar = (EditText) findViewById(R.id.inputconame);
        /*
            //TelTar.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
            //TelTar.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
            TelTar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        Log.e("커서", "감추기");
                       // TelTar.setCursorVisible(false);
                    }else{
                        Log.e("커서", "보이기");
                        //TelTar.setCursorVisible(true);
                    }

                }
            });
        */
        if (bundle != null) {
            orderNum = bundle.getString("ordernum");
            workDetail = bundle.getString("workDetail");
            doc_price = bundle.getString("doc_price");
            String W = Currency.getInstance(Locale.KOREA).getSymbol();
            TextView info1 = (TextView) findViewById(R.id.textView5);
            info1.setText(doc_price );
        }
        Log.e("오더번호:", "ordernum: " + orderNum);
        SeekBar simpleSeekBar = (SeekBar)findViewById(R.id.sb_);
        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(SignatureActivity.this, "Seek bar progress is :" + progressChangedValue,Toast.LENGTH_SHORT).show();
            }
        });
        DisplayMetrics disp = getApplicationContext().getResources().getDisplayMetrics();
        int width = (int) (disp.widthPixels * 0.99); //Display 사이즈의 70%
        int height = (int) (disp.heightPixels * 0.5);  //Display 사이즈의 90%
        Log.v("log_tag", "Width: " + disp.widthPixels + " ," + disp.heightPixels);
        getWindow().getAttributes().width = width;
        //getWindow().getAttributes().height = height;
        // Method to create Directory, if the Directory doesn't exists
        file = new File(DIRECTORY);
        if (!file.exists()) {
            file.mkdir();
        }
        //TelTar.requestFocus();
        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
    Button.OnClickListener onButtonClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (v == mClear) {
                Log.v("log_tag", "Panel Cleared");
                mSignature.clear();
                mGetSign.setEnabled(false);
            } else if (v == mGetSign) {
                //view.setDrawingCacheEnabled(true);
                Log.d("pathCnt Check", "pathCnt :" + pathCnt);
                if( pathCnt < 14 ) {
                    AlertDialog alertDialog = new AlertDialog.Builder(SignatureActivity.this).create();
                    alertDialog.setTitle("알림");
                    alertDialog.setMessage("작업확인서에 이름과 전화 번호를 모두 써주세요\n\n이름이나 전번이 잘못된경우 지우기를 이용하실 수 있습니다.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                }

                RadioButton Rbtn1 = (RadioButton) findViewById(R.id.radioButton1);
                RadioButton Rbtn2 = (RadioButton) findViewById(R.id.radioButton2);

                EditText coname = (EditText) findViewById(R.id.inputconame);
                String Co_name = coname.getText().toString();
                workInfo_coname = Co_name;
                if( Rbtn1.isChecked()) Payed = "미수";
                if( Rbtn2.isChecked()) Payed = "수금";

                AlertDialog.Builder builder = new AlertDialog.Builder(SignatureActivity.this, R.style.MyAlertDialogTheme);
                builder.setTitle(dialog_title );
                builder.setMessage(dialog_cont + "\n\n" + "결제상태 [ " + Payed + "]");
                builder.setPositiveButton("작업종료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("확인 작업종료", "Sending atomic bombs to Jupiter");
                        dialog.dismiss();
                        findViewById(R.id.signProgreeBg).setVisibility(View.VISIBLE);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mSignature.save(view, StoredPath);
                            }
                        }, 500);
                        //mSignature.save(view, StoredPath);
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("취소", "Aborting mission...");
                    }
                });
                dialog = builder.create();
                dialog.show();
                Button bq1 = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button bq2 = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                bq1.setTextColor(parseColor("#0080FF"));
                bq2.setTextColor(parseColor("#0080FF"));
                bq1.setTextSize(18);
                bq2.setTextSize(18);
                bq1.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                bq2.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            } else if(v == mCancel){
                Log.v("log_tag", "Panel Canceled");
                // Calling the BillDetailsActivity
                Intent intent = new Intent(SignatureActivity.this, MainActivity.class);
                intent.putExtra("actionjob", "SignAfterJobCancel");
                startActivity(intent);
            }
        }
    };
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            view.setDrawingCacheEnabled(true);
            mSignature.save(view, StoredPath);
            Toast.makeText(getApplicationContext(), "Successfully Saved", Toast.LENGTH_SHORT).show();
            // Calling the same class
            recreate();
        }
        else
        {
            Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
        }
    }

    public class UpdateTask extends AsyncTask<String, String, String> { /////////////// 싸인 저장 후 웹뷰에 알리기
        protected String doInBackground(String... urls) {
            String sourceFileUri = SignFn;
            Log.e("업로드 준비" , "파일명 : " + SignFn + " / Payed : " + Payed);
            HttpURLConnection mHttpURLConnection = null;
            DataOutputStream mOutputStream = null;
            String strLineEnd = "\r\n";
            String strTwoHyphens = "--";
            String strUpLoadServerUri = serverFn;
            String strBoundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            int serverResponseCode = 0;
            //if( serverResponseCode < 1 ) return null;
            File sourceFile = new File(SignFn);
            Log.e("AAAAAAAAAA","BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBb");
            String LatDeleteFn= "";
            if (!sourceFile.isFile()) {
                Log.e("업로드", "업로드 파일 없음" + sourceFileUri );
                Toast.makeText(getApplicationContext(), "업로드 실패 : 파일 없음.", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("업로드", "업로드 파일 OK" + sourceFileUri );
                File oFile = new File(sourceFileUri);
                String SERVER_Upload_URL = "http://sub.jbbuller.kr/base_work_upload_sign_file.php";
                try {
                    /////////////////////////////////////////////////
                    Bitmap b = BitmapFactory.decodeFile(SignFn);
                    ///////////////////////////////////////////////////////////////////////////////////////
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(SERVER_Upload_URL);
                    mHttpURLConnection = (HttpURLConnection) url.openConnection();
                    mHttpURLConnection.setReadTimeout(1000 * 300);
                    mHttpURLConnection.setReadTimeout(1000 * 300);
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
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"ordernum\"" + strLineEnd);
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes(orderNum + strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;
                    serverResponseCode = mHttpURLConnection.getResponseCode();
                    String getResponseMessage = mHttpURLConnection.getResponseMessage();
                    Log.e("서버응답텍스트", getResponseMessage);

                    if (serverResponseCode == 200) {
                        Log.d("File Uploaded For ", sourceFileUri + "   Successful");
                        Log.e("workInfo_coname", "workInfo_coname : " + workInfo_coname);
                        Intent intent = new Intent(SignatureActivity.this, MainActivity.class);
                        intent.putExtra("actionjob", "SignAfterJob");
                        intent.putExtra("imagePath", StoredPath);
                        intent.putExtra("paymentstatus", Payed);
                        intent.putExtra("Co_name", workInfo_coname);
                        intent.putExtra("Co_tel", "");
                        intent.putExtra("orderNum", orderNum);

                        startActivity(intent);
                        finish();

                    } else {
                        Log.e("File Uploaded For ", sourceFileUri + "   Failed " + serverResponseCode + "| " + mHttpURLConnection.getErrorStream());
                    }
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("error:" + e.getMessage(), "Fie upload error");
                }
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            }
            return "OK";
        }

    }
    public class signature extends View {
        private static final float STROKE_WIDTH = 20f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();
        private float lastTouchX;
        private float lastTouchY;
        private SignatureActivity MO = new SignatureActivity();
        private final RectF dirtyRect = new RectF();
        private int OnceDone= 0;
        private Canvas Gcanvas = null;

        @Override
        protected void onDraw(Canvas canvas) {
            Gcanvas = canvas;
            canvas.drawPath(path, paint);
        }
        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.BEVEL);
            paint.setStrokeWidth(progressChangedValue);
        }
        @SuppressLint("WrongThread")
        public void save(View v, String StoredPath) {
            Log.v("log_tag", "Width: " + v.getWidth());
            Log.v("log_tag", "Height: " + v.getHeight());
            //int no = path.

            mContent.setBackgroundResource(0);
            mSignature.setBackgroundColor(Color.WHITE);
            Gcanvas.drawPath(path, paint);
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            try {
                // Output the file
                FileOutputStream mFileOutStream = new FileOutputStream(StoredPath);
                v.draw(canvas);

                // Convert the output file to Image such as .png
                Bitmap TransBitmap = replaceColor(bitmap);
                TransBitmap = trimBitmap(TransBitmap);
                //bitmap.compress(Bitmap.CompressFormat.PNG, 0, mFileOutStream);
                TransBitmap.compress(Bitmap.CompressFormat.PNG, 0, mFileOutStream);

                mFileOutStream.flush();
                mFileOutStream.close();

                SignFn = StoredPath;
                serverFn = "http://sub.jbbuller.kr/base_work_upload_sign_file.php";
                Activity mActivity = SignatureActivity.this;
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        new SignatureActivity.UpdateTask().execute();
                    }
                });
                /*
                Intent intent = new Intent(SignatureActivity.this, MainActivity.class);
                intent.putExtra("actionjob", "SignAfterJob");
                intent.putExtra("imagePath", StoredPath);
                startActivity(intent);
                finish();

                */

            } catch (Exception e) {
                Log.v("log_tag", e.toString());
            }

        }
        public Bitmap replaceColor(Bitmap src){
            if(src == null) return null;
            int width = src.getWidth();
            int height = src.getHeight();
            int[] pixels = new int[width * height];
            src.getPixels(pixels, 0, width, 0, 0, width, height);
            for(int x = 0;x < pixels.length;++x){
                if(pixels[x] == Color.WHITE){
                    pixels[x] = ~(pixels[x] << 8 & 0xFF000000) & Color.BLACK;
                }
            }
            Bitmap result = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
            return result;
        }
        public Bitmap trimBitmap(Bitmap sourceBitmap){
            int minX = sourceBitmap.getWidth();
            int minY = sourceBitmap.getHeight();
            int maxX = -1;
            int maxY = -1;
            for(int y = 0; y < sourceBitmap.getHeight(); y++){
                for(int x = 0; x < sourceBitmap.getWidth(); x++){
                    int alpha = (sourceBitmap.getPixel(x, y) >> 24) & 255;
                    if(alpha > 0){   // pixel is not 100% transparent
                        if(x < minX) minX = x;
                        if(x > maxX) maxX = x;
                        if(y < minY) minY = y;
                        if(y > maxY) maxY = y;
                    }
                }
            }
            if((maxX < minX) || (maxY < minY)) return null; // Bitmap is entirely transparent // crop bitmap to non-transparent area and return:
            return Bitmap.createBitmap(sourceBitmap, minX, minY, (maxX - minX) + 1, (maxY - minY) + 1);
        }

        public void clear() {
            pathCnt = 0;
            path.reset();
            invalidate();
            mGetSign.setEnabled(false);
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    pathCnt++;
                    Log.e("ACTION_DOWN", "pathCnt : " + pathCnt);
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:
                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {

            Log.v("log_tag", string);

        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }
}

