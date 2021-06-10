package co.kr.jbbuller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.graphics.Color.parseColor;

public class Full_signatureActivity extends Activity {

    public String SignFn = "";
    public String serverFn = "";
    public String orderNum = "";
    public int progressChangedValue = 10;
    public String workDetail = "";
    public String doc_price = "";
    public String ww_addr = "";
    public String ww_day = "";
    public String Payed = "미수";
    public TextView loading_st = null;
    private String workInfo_cotel = "";

    private AlertDialog dialog;
    public int pathCnt = 0;
    Button mClear, mGetSign, mCancel,carChangeBtn ,PriceChnageBtn;
    File file;
    LinearLayout mContent;
    LinearLayout LayoutC;
    TextView textMemo;
    View view;
    signature mSignature = null;
    Bitmap bitmap;

    // Creating Separate Directory for saving Generated Images
    String DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/UserSignature/";
    String pic_name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    String StoredPath = DIRECTORY + pic_name + ".png";
    public String car_plate_list = "";
    public String w_type = "";
    public String work_car_num = "";
    public String car_plateset = "";
    public String paydonemsg = "";
    public String work_addr= "";
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_activity_signature);
        this.setFinishOnTouchOutside(false);
        LayoutC = (LinearLayout) findViewById(R.id.LayoutC);
        textMemo = (TextView) findViewById(R.id.textMemo);
        mClear = (Button) findViewById(R.id.clear);
        mGetSign = (Button) findViewById(R.id.getsign);
        //mGetSign.setEnabled(false);
        mCancel = (Button) findViewById(R.id.cancel);
        carChangeBtn = (Button) findViewById(R.id.carChangeBtn);
        PriceChnageBtn = (Button) findViewById(R.id.PriceChnageBtn);
        mGetSign.setOnClickListener(onButtonClick);
        mClear.setOnClickListener(onButtonClick);
        mCancel.setOnClickListener(onButtonClick);
        textMemo.setOnClickListener(onButtonClick);
        carChangeBtn.setOnClickListener(onButtonClick);
        PriceChnageBtn.setOnClickListener(onButtonClick);
        loading_st = (TextView) findViewById(R.id.loading_detailstr);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mCancel.getLayoutParams();
        params.setMargins(0, 0, 5, 0); //left, top, right, bottom
        mCancel.setLayoutParams(params);
        mClear.setLayoutParams(params);
        findViewById(R.id.signProgreeBg).setVisibility(View.GONE);
        Bundle bundle = getIntent().getExtras();
        final EditText TelTar = (EditText) findViewById(R.id.inputconame);
        /*
            i.putExtra("ordernum", ordernum);
            i.putExtra("w_time", w_time);
            i.putExtra("doc_price", t_price);

            i.putExtra("car_plateset", car_plateset);
            i.putExtra("car_plate_list", car_plate_list);
            i.putExtra("w_type", w_type);
            i.putExtra("added_time", added_time);
            i.putExtra("paydonemsg", paydonemsg);
        */
        String W = Currency.getInstance(Locale.KOREA).getSymbol();
        EditText info0 = (EditText) findViewById(R.id.infoaddr);
        EditText info1 = (EditText) findViewById(R.id.infovalue1);
        EditText info2 = (EditText) findViewById(R.id.infovalue2);
        EditText info3 = (EditText) findViewById(R.id.infovalue3);
        EditText info4 = (EditText) findViewById(R.id.infovalue4);
        EditText inputmemo = (EditText) findViewById(R.id.inputmemo);

        LinearLayout LayoutT = (LinearLayout) findViewById(R.id.LayoutT);
        if (bundle != null) {
            orderNum = bundle.getString("ordernum");
            workDetail = bundle.getString("w_time");
            doc_price = bundle.getString("doc_price");
            car_plateset = bundle.getString("car_plateset");
            car_plate_list = bundle.getString("car_plate_list");
            w_type = bundle.getString("w_type");
            paydonemsg = bundle.getString("paydonemsg");
            work_car_num = bundle.getString("car_num");
            work_addr = bundle.getString("work_addr");
            info0.setText(work_addr);
            info1.setText(workDetail);
            info2.setText(car_plateset );
            info3.setText(w_type );
            info4.setText(doc_price );
            inputmemo.setText(paydonemsg);
            info0.setEnabled(false);
            info1.setEnabled(false);
            info2.setEnabled(false);
            info3.setEnabled(false);
            info4.setEnabled(false);
        }else {
            AlertDialog alertDialog = new AlertDialog.Builder(Full_signatureActivity.this).create();
            alertDialog.setTitle("알림");
            alertDialog.setMessage("오더정보를 받지 못했습니다");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            alertDialog.show();
            return;
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
                Toast.makeText(Full_signatureActivity.this, "Seek bar progress is :" + progressChangedValue,Toast.LENGTH_SHORT).show();
            }
        });
        file = new File(DIRECTORY);
        if (!file.exists()) file.mkdir();

    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {ButtonHeightCheck();}
    public int CVs(float px, Context context) {return (int) px;}
    private int onceDone = 0;
    public void ButtonHeightCheck(){
        if( onceDone > 0 ) return;

        LinearLayout LayoutAddr = (LinearLayout) findViewById(R.id.LayoutAddr);
        LinearLayout LayoutA = (LinearLayout) findViewById(R.id.LayoutA);
        LinearLayout LayoutB = (LinearLayout) findViewById(R.id.LayoutB);
        LinearLayout LayoutC = (LinearLayout) findViewById(R.id.LayoutC);
        LinearLayout LayoutD = (LinearLayout) findViewById(R.id.LayoutD);
        LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.buttonLayout);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        LinearLayout canvasLayout = (LinearLayout) findViewById(R.id.canvasLayout);
        Button getsign = (Button) findViewById(R.id.getsign);
        Context Cx = getApplicationContext();
        mContent = (LinearLayout) findViewById(R.id.canvasLayout);
        int AllH = CVs(linearLayout.getHeight(),Cx);
        int XH = 0;
        XH += CVs(LayoutAddr.getHeight(),Cx);
        XH += CVs(LayoutA.getHeight(),Cx);
        XH += CVs(LayoutB.getHeight(),Cx);
        if (LayoutC.getVisibility() == View.VISIBLE) XH += CVs(LayoutC.getHeight(),Cx);
        XH += CVs(LayoutD.getHeight(),Cx);
        //XH += CVs(buttonLayout.getHeight(),Cx);
        XH += CVs(canvasLayout.getHeight(),Cx);
        int BtnH = CVs(getsign.getHeight(),Cx );
        int CanvasHeight = CVs(canvasLayout.getHeight(),Cx);
        int Gspace = CVs(10,Cx);
        Log.e("높이체크", "다합친높이:" + XH + " 화면높이:" + AllH + " 버튼높이:" + BtnH + " 캔버스높이:" + CanvasHeight);
        if( AllH -10 > XH){
            int Gab = CanvasHeight + (AllH - XH -Gspace);
            Gab += 10;
            Log.e("높이조절", "GAB:" + Gab + " / BtnH:" + BtnH);
            Gab = (int) (Gab - (BtnH*2.5));

            Log.e("높이조절", "GAB:" + Gab);
            //ViewGroup.LayoutParams params = mContent.getLayoutParams(); // Changes the height and width to the specified *pixels*
            //params.height = Gab;
            ViewGroup.LayoutParams params = mContent.getLayoutParams();
            params.height = Gab;
            mContent.requestLayout();
            //mContent.setMinimumHeight(Gab);
            //mContent.getLayoutParams().height=Gab;
            int AfterH = CanvasHeight = CVs(canvasLayout.getHeight(),Cx);
            Log.e("높이조절", "After:" + AfterH);
        }
        if( mSignature != null ) mContent.removeView(mSignature);
        mSignature = new signature(getApplicationContext(), null);
        //mSignature.setBackgroundColor(Color.WHITE);
        view = mContent;
        mContent.setBackgroundResource(R.drawable.sign_bg01);//to set background
        // Dynamically generating Layout through java code
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        onceDone =1;
    }
    private AlertDialog.Builder builder;
    private  List<String> Plate_Strs;
    private  List<String> Plate_rsnos;

    public void select_otheCar(){
        String [] datas = car_plate_list.split("\\|");
        Plate_Strs = new ArrayList<String>();
        Plate_rsnos = new ArrayList<String>();
        for (int i = 0; i < datas.length; i++) {
            String eaLIne = datas[i];
            if( eaLIne.length() > 2 ){
                String [] eaData = eaLIne.split("/");
                if (eaData.length > 1) {
                    Plate_Strs.add(eaData[1]);
                    Plate_rsnos.add(eaData[0]);
                }
            }

        }
        final CharSequence[] options = Plate_Strs.toArray(new CharSequence[Plate_Strs.size()]);
        //final CharSequence[] options = {"카메라촬영", "갤러리에서선택", "취소"};
        builder = new AlertDialog.Builder(Full_signatureActivity.this);
        builder.setTitle("장비 변경하기");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                String One = (String) options[item];
                int xn = Plate_Strs.indexOf(One);
                if( xn >= 0 ){
                    work_car_num = Plate_rsnos.get(xn);
                    car_plateset=One;
                    EditText info2 = (EditText) findViewById(R.id.infovalue2);
                    info2.setText(One);
                    Log.e("차량변경", "NUM:" + work_car_num + " / 차량번호:" + One);
                }
                dialog.dismiss();
            }
        });

        builder.show();
    }
    Button.OnClickListener onButtonClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Log.e("onClick", "onClick :" + v);
            if (v == mClear) {
                Log.v("log_tag", "Panel Cleared");
                mSignature.clear();
                //mGetSign.setEnabled(false);
            } else if (v == textMemo) {
                //LayoutC.setVisibility(View.GONE);
                //onceDone = 0;
                //ButtonHeightCheck();
            }else if( v == PriceChnageBtn){
                Intent intent = new Intent(Full_signatureActivity.this, MainActivity.class);
                //intent.putExtra("actionjob", "SignAfterJobDonenew");
                if( pathCnt > 2 ) {
                    String ask_title = "금액변경";
                    String ask_cont = "금액변경페이지로 이동할까요?\n현재 싸인은 사라집니다.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(Full_signatureActivity.this, R.style.messageyesno);
                    builder.setTitle(ask_title );
                    builder.setMessage(ask_cont);
                    builder.setPositiveButton("금액수정", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            intent.putExtra("actionjob", "SignAfterJobPriceChange");
                            intent.putExtra("orderNum", orderNum);
                            startActivity(intent);
                            finish();
                        }
                    });
                    builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.e("취소", "Aborting mission...");
                        }
                    });
                    dialog = builder.create();
                    dialog.show();
                    return;
                }else{
                    intent.putExtra("actionjob", "SignAfterJobPriceChange");
                    intent.putExtra("orderNum", orderNum);
                    startActivity(intent);
                    finish();
                }
            }else if(v == carChangeBtn){
                select_otheCar();
            } else if (v == mGetSign) {
                //view.setDrawingCacheEnabled(true);
                Log.e("pathCnt Check", "pathCnt :" + pathCnt);
                if( pathCnt < 5 ) {
                    String ask_title = "작업완료";
                    String ask_cont = "싸인 없이 작업을 완료 할까요?";
                    AlertDialog.Builder builder = new AlertDialog.Builder(Full_signatureActivity.this, R.style.messageyesno);
                    builder.setTitle(ask_title );
                    builder.setMessage(ask_cont);
                    builder.setPositiveButton("작업완료", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            findViewById(R.id.signProgreeBg).setVisibility(View.VISIBLE);
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mSignature.save(view, StoredPath);
                                }
                            }, 500);

                            Log.e("싸인없이 작업완료", "서버에 작업종료 보내기");
                            jobDone_server_excute();
                            dialog.dismiss();

                        }
                    });
                    builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.e("취소", "Aborting mission...");
                        }
                    });
                    dialog = builder.create();
                    dialog.show();
                    return;

                }
                EditText inputmemo = (EditText) findViewById(R.id.inputmemo);
                paydonemsg = inputmemo.getText().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(Full_signatureActivity.this, R.style.messageyesno);
                String ask_title = "작업완료";
                String ask_cont = "현재 싸인으로 작업완료 할까요?";
                builder.setTitle(ask_title );
                builder.setMessage(ask_cont);
                builder.setPositiveButton("작업완료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("확인 작업종료", "Sending atomic bombs to Jupiter");
                        dialog.dismiss();

                        loading_st.setText(".. 싸인 파일 만드는 중 ..");

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
                builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
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
                Intent intent = new Intent(Full_signatureActivity.this, MainActivity.class);

                intent.putExtra("returnordernum", orderNum);
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
            //loading_st.setText("... 작업 완료 적용 중 ...");

            if (!sourceFile.isFile()) {
                Log.e("업로드", "업로드 파일 없음" + sourceFileUri );
                Toast.makeText(getApplicationContext(), "업로드 실패 : 파일 없음.", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("업로드", "업로드 파일 OK" + sourceFileUri );
                //File oFile = new File(sourceFileUri);
                String SERVER_Upload_URL = "http://sub.jbbuller.kr/base_work_upload_sign_file.php";
                try {
                    /////////////////////////////////////////////////
                    //Bitmap b = BitmapFactory.decodeFile(SignFn);
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
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"upload_imgtype\"" + strLineEnd);
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes("imageandtext" + strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    //Log.e("저장", "차번:" + work_car_num + " 차량번호:" + car_plateset + " 메시지:" + paydonemsg );
                    ///////////////////////////////////////////////////////////////////////////////////;
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"work_car_num\"" + strLineEnd);
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes(URLEncoder.encode(work_car_num) + strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"car_plateset\"" + strLineEnd);
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes(URLEncoder.encode(car_plateset) + strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"paydonemsg\"" + strLineEnd);
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes(URLEncoder.encode(paydonemsg) + strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;

                    serverResponseCode = mHttpURLConnection.getResponseCode();
                    String getResponseMessage = mHttpURLConnection.getResponseMessage();
                    Log.e("서버응답텍스트", getResponseMessage + " serverResponseCode:" + serverResponseCode);

                    if (serverResponseCode == 200) {
                        Log.d("File Uploaded For ", sourceFileUri + "   Successful");

                        jobDone_server_excute();


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

    public void jobDone_server_excute() {
        String url = "http://jbbuller.kr/__app_job_done_save_order.php?ordernum=" + orderNum;
        Log.e("서버에완료알리기", "URL:" + url);
        class GetDataJSON_Cnt extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject json_data = new JSONObject(result);
                    String resultstr = json_data.getString("result");
                    Log.e("서버전송결과", "RESULT:" + resultstr);

                    Intent intent = new Intent(Full_signatureActivity.this, MainActivity.class);
                    intent.putExtra("actionjob", "SignAfterJobDonenew");
                    intent.putExtra("imagePath", StoredPath);
                    intent.putExtra("work_car_num", work_car_num);
                    intent.putExtra("paydonemessage", paydonemsg);
                    intent.putExtra("car_plateset", car_plateset);
                    intent.putExtra("orderNum", orderNum);
                    startActivity(intent);
                    finish();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        GetDataJSON_Cnt g = new GetDataJSON_Cnt();
        g.execute(url);
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
            //Log.e("Gcanvas" , "onDraw Canvas " + Gcanvas);
            //LayoutC.setVisibility(View.GONE);
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
            Log.e("Gcanvas" , "Canvas" + Gcanvas);
            Log.e("path" , "path" + path);
            Log.e("paint" , "paint" + paint);

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
                Log.e("저장", "차번:" + work_car_num + " 차량번호:" + car_plateset + " 메시지:" + paydonemsg );
                File sourceFile = new File(SignFn);
                if (!sourceFile.isFile()) {
                    Log.e("업로드", "업로드 파일 없음" + SignFn );
                } else {
                    Log.e("업로드", "업로드 파일 있음 업로드 시작하기" + SignFn );
                }
                loading_st.setText(".. 작업 완료 적용 중 ..");
                Activity mActivity = Full_signatureActivity.this;

                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Log.e("업로드호출함", "Call updateTask");
                        new Full_signatureActivity.UpdateTask().execute();
                    }
                });
                /*


                Intent intent = new Intent(Full_signatureActivity.this, MainActivity.class);
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
            //mGetSign.setEnabled(false);
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

