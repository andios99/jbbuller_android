package co.kr.jbbuller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadTo_server {
    private Context context;
    private String UPLOAD_Fname = "";
    public Share_utils utilApp;
    public String Upload_JobType = "";
    public int UploadMaxSize = 1800000;
    public UploadTo_server(Context context){
        this.context=context;
    }
    public String Upload_One_Start(String filename, String jobtype, int maxSize){
        Log.e("UploadTo_server", " 파일업로드 시작 :" + filename + " / jobtype : " + jobtype);
        Upload_JobType = jobtype;
        UPLOAD_Fname = filename;
        if( maxSize > 100000) UploadMaxSize = maxSize;
        String[] myTaskParams = { UPLOAD_Fname, Upload_JobType };
        new UpdateTask ().execute(myTaskParams);
        return filename;
    }

    public void sendDatas(String JobAct,String Filename ,String gpsValue, String dateString, String gpsAddress){//my.sendDatas(Upload_JobType, file_saveGet);
        Log.e("sendDatas" , "Job :" + JobAct + " / Filename :" + Filename);
        ((MainActivity)context).UPload_ResultGet_sendToWebview(Filename,JobAct,gpsValue,dateString, gpsAddress);
        Main_LoadingShow(0);
       // MainActivity.UPload_ResultGet_sendToWebview(Filename,JobAct);
    }
    public void Main_LoadingShow(int ShowHide){
        ((MainActivity)context).LoadingBar_ShowHide(ShowHide);
    }
    private class UpdateTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... pParams) {
            String Fn = pParams[0];
            String jobtype = pParams[1];
            String sourceFileUri = UPLOAD_Fname;
            utilApp = new Share_utils(context);
            Log.e("업로드 준비" , "파일명 : " + UPLOAD_Fname);
            HttpURLConnection mHttpURLConnection = null;
            DataOutputStream mOutputStream = null;
            String strLineEnd = "\r\n";
            String strTwoHyphens = "--";
            String strBoundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            int serverResponseCode = 0;
            File sourceFile = new File(UPLOAD_Fname);
            Log.e("UploadTo_server","UploadTo_server starging");
            String LatDeleteFn= "";
            String dateString = "2000-01-01 12:30:00";
            double Latitude=0;
            double Longitude = 0;
            String gpsValue = "0,0";
            String gpsAddress = "";

            if (!sourceFile.isFile()) {
                Log.e("UploadTo_server", "업로드 파일 없음" + sourceFileUri );
                Toast.makeText(context.getApplicationContext(), "업로드 실패 : 파일 없음.", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("UploadTo_server", "업로드 파일 OK" + sourceFileUri );
                Main_LoadingShow(1);
                try {
                    final ExifInterface exifInterface = new ExifInterface(UPLOAD_Fname);
                    float[] latLong = new float[2];
                    File file = new File(UPLOAD_Fname);
                    Date lastModDate = new Date(file.lastModified());
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dateString = dateFormat.format(lastModDate); //lastModDate.toString();
                    Log.e("Date Info", "Date :" + dateString);
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
                sourceFileUri = utilApp.resizeImg(sourceFileUri,UploadMaxSize);
                Log.e("축소된파일명", "sourceFileUri : " + sourceFileUri);
                oFile = new File(sourceFileUri);
                if (oFile.exists()) {
                    long L = oFile.length();
                    System.out.println("축소 후 크기 : " + L + " bytes : " + oFile.getAbsoluteFile());
                }
                String SERVER_Upload_URL = "http://jbbuller.kr/upload_image_allkind.php";

                Log.e("파일생일", dateString);
                gpsValue = Latitude + "," + Longitude;
                gpsAddress = utilApp.getAddress(Latitude,Longitude);
                String str_encode = "";
                try{
                    str_encode = URLEncoder.encode(gpsAddress,"UTF-8");
                } catch(Exception e) {
                    e.printStackTrace();
                }
                Log.e("촬영후 GPS", "GPS : " + gpsValue);
                Log.e("gpsValue", "gpsValue : " + gpsValue + " / gpsAddress : " + gpsAddress);
                Log.e("str_encode", "str_encode : " + str_encode);
                try {
                    /////////////////////////////////////////////////
                    Bitmap b = BitmapFactory.decodeFile(UPLOAD_Fname);
                    ///////////////////////////////////////////////////////////////////////////////////////
                    FileInputStream fileInputStream = new FileInputStream(sourceFileUri);
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
                    Log.e("SendingFile", "File Name  : " + sourceFileUri);
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
                    Log.e("UPSTART","jobtype :" + jobtype + "   /file:" + sourceFileUri );


                    /*
                        $jobtype=$_POST["jobtype"];
                        $fileage=$_POST["fileage"];
                        $gpsValue=$_POST["gpsValue"];
                        $gpsaddress=$_POST["gpsaddress"];  // filename_last
                      */
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;
                    mOutputStream.writeBytes("Content-Disposition: form-data; name=\"jobtype\"" + strLineEnd);
                    mOutputStream.writeBytes(strLineEnd);
                    mOutputStream.writeBytes(jobtype + strLineEnd);
                    mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                    ///////////////////////////////////////////////////////////////////////////////////;
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

                    serverResponseCode = mHttpURLConnection.getResponseCode();

                    BufferedReader bufferedReader = null;
                    bufferedReader = new BufferedReader(new InputStreamReader(mHttpURLConnection.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    JSONObject json_data = new JSONObject(sb.toString().trim());
                    String file_saveGet = json_data.getString("filename_last");
                    String file_result = json_data.getString("result");
                    Log.e("file_saveGet", "result: " + file_result + " / filename_last: " + file_saveGet);
                    UploadTo_server my = new UploadTo_server(context);
                    Log.e("촬영후 GPS", "GPS : " + gpsValue);
                    Log.e("gpsValue", "gpsValue : " + gpsValue + " / gpsAddress : " + gpsAddress);
                    Log.e("str_encode", "str_encode : " + str_encode);

                    my.sendDatas(Upload_JobType, file_saveGet,gpsValue,dateString, gpsAddress);
                    //Main_LoadingShow(0);
                    if (serverResponseCode == 200) {
                        fileInputStream.close();
                        mOutputStream.flush();
                        mOutputStream.close();
                        //utilApp.deleteUpLoadedFn(sourceFileUri,1);
                    } else {
                        //utilApp.deleteUpLoadedFn(sourceFileUri,2);
                        fileInputStream.close();
                        mOutputStream.flush();
                        mOutputStream.close();
                    }
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    //utilApp.deleteUpLoadedFn(sourceFileUri,3);
                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                } catch (Exception e) {
                    e.printStackTrace();
                    //utilApp.deleteUpLoadedFn(sourceFileUri,4);
                    Log.e("error:" + e.getMessage(), "Fie upload error");
                }
                //Main_LoadingShow(0);
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            }
            return "OK";
        }
    }
}
