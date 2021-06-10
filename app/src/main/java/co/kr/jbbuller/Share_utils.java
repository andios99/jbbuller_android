package co.kr.jbbuller;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Share_utils {
    private Context context;
    private static String TAG = "phptest_MainActivity";
    private static final String TAG_JSON="webnautes";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_ADDRESS ="address";
    private static String fileName = null;
    private MediaRecorder recorder = null;
    ListView mlistView;
    ProgressDialog progressDialog;


    public Share_utils(Context context){
        this.context=context;
    }
    public void voice_record_reset(){
        RecStatus = 0;
        if( recorder != null) {
            try{
                recorder.reset();
                recorder.release();
            }catch (Exception e){
                Log.e("voice_record_reset", "voice_record_reset  에러");
            }

        }
    }
    private int RecStatus = 0;
    public String voice_record_Start(String mbno){
        File file = new File(Environment.getExternalStorageDirectory(), "/JbBuller/");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(context.getApplicationContext(), "Uploading canceld(Cound not Make temp folder)", Toast.LENGTH_SHORT).show();
                return "";
            }
        }
        if( recorder != null) {
            try{
                recorder.reset();
                Log.e("음성녹음", "Share_utils  reset OK");
            }catch (Exception e){
                Log.e("음성녹음", "Share_utils  reset 에러" + e.toString());
            }

            try{
                if( RecStatus == 1 ) {
                    recorder.stop();
                    RecStatus = 2;
                }
                recorder.release();
                Log.e("음성녹음", "Share_utils  stop  release  OK");
            }catch (Exception e){
                Log.e("음성녹음", "Share_utils  널 아니지만 중지 릴리즈 안하고 바로 NULL로가자");
                ((MainActivity)context).javascript_RecordErrorShow();
                return "";
            }

            recorder = null;
        }


        fileName = Environment.getExternalStorageDirectory() + File.separator + "JbBuller/jbbuller_voice_record.mp3";
        //fileName = context.getExternalCacheDir().getAbsolutePath() + "/jbbuller_voice_record.3gp";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(fileName);

        try {
            recorder.prepare();
            recorder.start();
            RecStatus = 1;
            Log.e("음성녹음", " Share_utils  prepare() start() OK");
        } catch (IOException e) {
            Log.e("음성녹음", "Share_utils  prepare() failed");
        }



        return fileName;
    }
    private MediaMetadataRetriever retriever;
    private MediaPlayer mediaPlayer;
    public String voice_record_stop(){
        RecStatus = 2;
        if( recorder == null) return "";
        try{
            if( RecStatus == 1 ) recorder.stop();
            recorder.stop();
            recorder.release();
            recorder = null;
            RecStatus = 0;
        }catch (Exception e){
            Log.e("파일 정지해제", "Share_utils  에러남");

            try{
                recorder.reset();
                Log.e("파일RESET", "reset OK");
            }catch (Exception err){
                Log.e("파일 Reset에러", "Exception");
            }
            ((MainActivity)context).javascript_RecordErrorShow();
            //return "";
        }

        File oFile = new File(fileName);
        if (oFile.exists()) {
            long L = oFile.length();
            System.out.println("Share_utils 녹음파일크기 : " + L + " bytes : " + oFile.getAbsoluteFile());
        }else{
            fileName = "";
        }
        return fileName;
    }
    public void chFile(){
        String fn = "/storage/emulated/0/JbBuller/jbbuller_voice_record.3gp";
        File ff = new File(fn);
        Log.e("Share_utils 파일 있는지 테스트", "FN:" + ff.getAbsolutePath());


    }
    public void sendError_to_Server(final String Error_file, final String Error_cause, final String Error_string, final String Error_pos) {
        new Thread() {
            public void run() {
                sendError_to_ServerDo(Error_file, Error_cause, Error_string,Error_pos);
            }
        }.start();
    }
    public void sendError_to_ServerDo(String Error_file, String Error_cause, String Error_string,  String Error_pos){
        String postReceiverUrl = "http://jbbuller.kr/__app_error_report.php";
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(postReceiverUrl);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("Error_file", Error_file));
        nameValuePairs.add(new BasicNameValuePair("Error_cause", Error_cause));
        nameValuePairs.add(new BasicNameValuePair("Error_string", Error_string));
        nameValuePairs.add(new BasicNameValuePair("Error_pos", Error_pos));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity resEntity = response.getEntity();
        if (resEntity != null) {
            try {
                String responseStr = EntityUtils.toString(resEntity).trim();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static long get_CacheSize(Context context){
        File dir = context.getCacheDir();
        if (dir == null) return 0;
        long size = 0;
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                size += getFolderSize(dir);
            }
        } else {
            size=dir.length();
        }
        return size;
    }
    public static long getFolderSize(File dir) {
        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for(int i = 0; i < fileList.length; i++) {
                // Recursive call if it's a directory
                if(fileList[i].isDirectory()) {
                    result += getFolderSize(fileList[i]);
                } else {
                    // Sum the file size in bytes
                    result += fileList[i].length();
                }
            }
            return result; // return the file size
        }
        return 0;
    }
    public static void clearApplicationCache(Context context, File file) {
        File dir = null;
        if (file == null) {
            dir = context.getCacheDir();
        } else {
            dir = file;
        }
        Log.e("캐시사기제", "폴더:" + dir);
        if (dir == null) return;
        File[] children = dir.listFiles();
        try {
            for (int i = 0; i < children.length; i++) {
                if (children[i].isDirectory()) {
                    clearApplicationCache(context, children[i]);
                    Log.e("캐시사기제", "Clear:" + children[i]);
                } else {
                    children[i].delete();
                    Log.e("캐시사기제", "Cach Folder Delete:" + children[i]);
                }
            }

        } catch (Exception e){
            Log.e("캐시사기제", "폴더에러:" + e);
        }
    }
    public String resizeImg(String path,int iOrder){
        Bitmap resultBitmap = null;
        String orgPath = path;
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.fromFile(new File(path)); ;//Uri.parse(path);
        InputStream in = null;
        int mmx = 1800000;
        if( iOrder > 100000 ) mmx = iOrder;
        Log.e("resizeImg", "경로" + path + "/ 크기 :" + mmx);
        try {
            int IMAGE_MAX_SIZE = mmx; //1800000; // 1.2MP
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
            Log.e("resizeImg스케일", "scale : " + scale );
            in = resolver.openInputStream(uri);
            ExifInterface exif = new ExifInterface(path);
            //int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            String Image_rotate = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = Image_rotate != null ? Integer.parseInt(Image_rotate) : ExifInterface.ORIENTATION_NORMAL;
            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
            Log.e("resizeImg회전", "rotationAngle : " + rotationAngle );
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
                        Toast.makeText(context.getApplicationContext(), "Uploading canceld(Cound not Make temp folder)", Toast.LENGTH_SHORT).show();
                        return "";
                    }
                }
                Random generator = new Random();
                int num1= generator.nextInt(89) + 10;
                String new_Fn = Environment.getExternalStorageDirectory() + File.separator + "JbBuller/mcTempFile_" + num1 + "_" + mid_n + "." + exten;
                File f = new File(new_Fn);
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                fo.close();
                Log.e("최종 파일", "new_Fn : " + new_Fn );
                String f_ex = "모름";
                if(f.exists()){
                    f_ex = "파일 있음 ";
                }else{
                    f_ex = "파일 없음ㅌㅌ ";
                }
                Log.e("최종 파일 Last", "Last FIlename 상태 : " + f_ex );
                path = new_Fn;

            } else {

            }
            in.close();
            return path;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return path;
        }
    }
    public void deleteUpLoadedFn(String sourceFileUri, int callFrom){
        if (sourceFileUri.length() > 5) {
            File f = new File(sourceFileUri);
            Boolean deleted = f.delete();
            Log.e("임시 파일삭제", "Filename :" + deleted + " / " + sourceFileUri + " / callFrom : " + callFrom);
        }
    }
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        return rotatedImg;
    }
    public double convertToDegree(String stringDMS){
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
    public boolean appDown(){
        try {

            String PATH = Environment.getExternalStorageDirectory() + "/download/";
            File file = new File(PATH);
            file.mkdirs();
            // Create a file on the external storage under download
            File outputFile = new File(file, "Jbbuller.apk");
            FileOutputStream fos = new FileOutputStream(outputFile);
            String apkurl = "http://jbbuller.kr/apk/jb1010.apk";
            HttpGet m_httpGet = null;
            HttpResponse m_httpResponse = null;

            // Create a http client with the parameters
            HttpClient m_httpClient = new DefaultHttpClient();
            String result = null;
            try {
                // Create a get object
                m_httpGet = new HttpGet(apkurl);
                // Execute the html request
                m_httpResponse = m_httpClient.execute(m_httpGet);
                HttpEntity entity = m_httpResponse.getEntity();
                // See if we get a response
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    byte[] buffer = new byte[1024];
                    // Write out the file
                    int len1 = 0;
                    while ((len1 = instream.read(buffer)) != -1) {
                        fos.write(buffer, 0, len1);
                    }
                    fos.close();
                    instream.close();// till here, it works fine - .apk is download to my sdcard in download file

                }

            } catch (ConnectTimeoutException cte) {
                // Toast.makeText(MainApplication.m_context, "Connection Timeout", Toast.LENGTH_SHORT).show();
                return false;
            } catch (Exception e) {
                return false;
            } finally {
                m_httpClient.getConnectionManager().closeExpiredConnections();
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "Jbbuller.apk")),"application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(intent);
            return true;
            // System.exit(0);

        } catch (IOException e) {
            Log.e("Error", "Failed to update new apk");
            return false;
        } catch (Exception e1) {
            Log.e("Error",  "Failed to update new apk");
            return false;
        }
    }
    public String getAddress(double lat, double lng) {
        String nowAddress = "";
        Geocoder geocoder = new Geocoder(this.context, Locale.getDefault());
        //lat = 37.3614255;
        //lng = 126.8807602;
        try {
            ConnectivityManager cm = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm.getActiveNetworkInfo() == null) return "";

            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            String currentAddress = "";
            if( addresses.isEmpty() ) {

            }else{
                Address obj = addresses.get(0);
                currentAddress = obj.getAddressLine(0);
            }
            currentAddress = currentAddress.replace("대한민국 ","");
            currentAddress = currentAddress.replace("대한민국","");
            //Log.e("IGA", "lat:" + lat + " / lng : " + lng + "/Crt = " + currentAddress);
            return currentAddress;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            return "";
        }
    }

    public static void get_backButton_ExceptList() {
        String url = "http://jbbuller.kr/_app_backbutton_except_file_list.php";
        GetData task = new GetData();
        task.execute(url);
    }
    public static List<String> return_Data ;
    private static class GetData extends AsyncTask<String, Void, String> {
        private String mJsonString;
        private ArrayList<HashMap<String, String>> mArrayList;
        private String errorString;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            List<String> ret_datas = new ArrayList<String>();
            if (result == null){
            }else {
                mJsonString = result;
                try {
                    JSONObject jsonObject = new JSONObject(mJsonString);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");

                    Log.e("mArrayList", jsonArray.toString());
                    Log.e("길이", "Length:" + jsonArray.length());
                    for(int i=0;i<jsonArray.length();i++){
                        String eaOne = jsonArray.getString(i);
                        ret_datas.add(eaOne);
                        Log.e("eaOne", eaOne);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "showResult : ", e);
                }
            }
            return_Data = ret_datas;
        }
        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();
                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);
                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }else{
                    inputStream = httpURLConnection.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }
                bufferedReader.close();
                return sb.toString().trim();
            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();
                return null;
            }
        }
    }
}
