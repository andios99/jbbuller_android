package co.kr.jbbuller;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class multi_upload_util {
    private Context context;
    private String fileUpload_main = "";
    private String UPLOAD_Fname = "";
    public String uploadMediaType = "";
    private String LastDeleteFn;
    public String URL_Header = "http://jbbuller.kr/";
    private String jobAct = "";
    private String room_num =  "";
    private String mb_no =  "";
    private String netresult_string = "";
    private String fileUpload_icon = "";
    public ArrayList<String> Upload_LIst;
    public Share_utils utilApp;
    public int UploadMaxSize = 1000000;

    public multi_upload_util(Context context){
        this.context=context;
    }

    public static int getBitmapOfWidth( String fileName ){
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileName, options);
            return options.outWidth;
        } catch(Exception e) {
            return 0;
        }
    }
    public static int getBitmapOfHeight( String fileName ){
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileName, options);

            return options.outHeight;
        } catch(Exception e) {
            return 0;
        }
    }
    public void UploadMultiStart(ArrayList<String> fileList, String mbno, String roomnum){
        utilApp = new Share_utils(context);
        Upload_LIst = fileList;
        mb_no=mbno;
        room_num=roomnum;
        uploadMediaType = "image";
        Log.e("BeforeMultiUp", "mbno:" + mb_no + "/ roomnum : " + room_num + " / fileList :" + Upload_LIst.toString() );
        new multi_upload_util.UploadImages().execute();
    }

    public String getMimeType(String filePath) {
        String type = null;
        String extension = null;
        int i = filePath.lastIndexOf('.');
        if (i > 0)
            extension = filePath.substring(i+1);
        if (extension != null)
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return type;
    }

    private int multi_upCnt = 0;
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private class UploadImages extends AsyncTask<String, String, String> {
        private String Errmgg = "";
        @Override
        protected void onPreExecute() {

        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //dialog.dismiss();
            ((MainActivity)context).progress_dismiss();
            if( !Errmgg.equals("")) {
                alertt(Errmgg);
            }
        }
        protected void onProgressUpdate(String... progress) {
            if (progress[0].equals("progress")) {
               // dialog.setProgress(Integer.parseInt(progress[1]));
                //dialog.setMessage(progress[2]);
                ((MainActivity)context).progress_int_change(Integer.parseInt(progress[1]));
                ((MainActivity)context).progress_string_change(progress[2]);
            }
            else if (progress[0].equals("max")) {
               ((MainActivity)context).progress_int_change(Integer.parseInt(progress[1]));
               // dialog.setMax(Integer.parseInt(progress[1]));
            }
        }
        @Override
        protected String doInBackground(String... params) {
            int listSize = Upload_LIst.size();
            int file_resized = 0;
            Upload_PopNotice();
            ((MainActivity)context).progress_setMax(listSize);
            //dialog.setMax(listSize);
            multi_upCnt = 0;
            for (int i = 0; i<listSize; i++){
                UPLOAD_Fname = Upload_LIst.get(i);
                file_resized = 0;
                String sourceFileUri = UPLOAD_Fname;
                File sourceFile = new File(UPLOAD_Fname);
                int UpFile_sizeBefore = (int) sourceFile.length();
                int img_width = 0;
                int N_sizeWW = 10;
                int N_sizeHH = 10;
                int img_height =0;
                int SoundDuration = 0;
                String imgone = UPLOAD_Fname;
                sourceFile = new File(UPLOAD_Fname);
                uploadMediaType = getMimeType(UPLOAD_Fname);
                Log.e("FileType", "FN:" +  UPLOAD_Fname + " Type:" + uploadMediaType);

                if(uploadMediaType.contains("image")) {
                    UPLOAD_Fname = utilApp.resizeImg(UPLOAD_Fname,UploadMaxSize);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) img_width = getBitmapOfWidth(sourceFileUri);
                    img_height = getBitmapOfHeight(sourceFileUri);
                    imgone = resizeImg(sourceFileUri,i);
                    if( imgone == "") return null;
                    sourceFile = new File(imgone);

                    if(imgone != UPLOAD_Fname && ! uploadMediaType.equals("video") ){
                        UPLOAD_Fname = imgone;
                        file_resized = 1;
                        sourceFileUri = UPLOAD_Fname;
                        N_sizeWW = getBitmapOfWidth(UPLOAD_Fname);
                        N_sizeHH = getBitmapOfHeight(UPLOAD_Fname);
                        img_width = N_sizeWW;
                        img_height = N_sizeHH;
                    }
                }else if(uploadMediaType.contains("audio")){
                    Uri Filepath = Uri.parse(sourceFile.toString());
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(sourceFile.toString());
                    MediaPlayer JbPlayer = MediaPlayer.create(context,Filepath);
                    SoundDuration = JbPlayer.getDuration();
                }
                HttpURLConnection mHttpURLConnection = null;
                DataOutputStream mOutputStream = null;
                String strLineEnd = "\r\n";
                String strTwoHyphens = "--";
                String strBoundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                int serverResponseCode = 0;
                String serverResponseMessage = "";
                int UpFile_size = (int) sourceFile.length();
                Log.e("파일 크기", "이전 :" + UpFile_sizeBefore + " 이후 : " + UpFile_size);

                int fSizeMB = (int)UpFile_size / 1048576;
                String response = "";
                if (!sourceFile.isFile() || fSizeMB > 500 ) {
                    if(fSizeMB > 500 ){
                        Errmgg = "Maximum size of file is 500MB";
                        break;
                    }
                } else if( UpFile_size > 10 ) {
                    if( img_width > 8000 || img_height > 8000){
                        Errmgg = "The maximum photo size if 8,000 Pixel";//사진의 크기는 최대 8,000 픽셀까지 입니다.|사진용량을 줄이거나 분할 후 업로그해 주세요";
                        break;
                    }else{
                        try {
                            //dialog.setProgress(0);
                            ((MainActivity)context).progress_int_change(0);
                            publishProgress("progress",Integer.toString((int)i+1),Integer.toString((int)i+1) + "/" + listSize);
                            String strUpLoadServerUri = "http://jbbuller.kr/_sky_m_01_chat_upload_api.php";
                            if( uploadMediaType.equals("video")) strUpLoadServerUri = "http://jbbuller.kr/_sky_m_01_chat_video_upload_api.php";
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
                            mOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + URLEncoder.encode(sourceFileUri, "utf-8") + "\"" + strLineEnd);
                            //mOutputStream.write(Building.getBytes("EUC_KR"));
                            mOutputStream.writeBytes(strLineEnd);
                            // create a buffer of maximum size
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            buffer = new byte[bufferSize];
                            // read file and write it into form...
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                            int sentBytes=0;
                            while (bytesRead > 0) {
                                mOutputStream.write(buffer, 0, bufferSize);
                                bytesAvailable = fileInputStream.available();
                                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                                sentBytes += bufferSize;

                                if( sentBytes > 0  && bytesAvailable > 0 ){
                                    float a = (float)sentBytes / (float)UpFile_size;
                                    int PP =  (int)(a * 100);
                                    //dialog.setProgress(PP);
                                    ((MainActivity)context).progress_int_change(PP);

                                }else{

                                }
                            }
                            mOutputStream.writeBytes(strLineEnd);
                            mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                            ///////////////////////////////////////////////////////////////////////////////////;
                            mOutputStream.writeBytes("Content-Disposition: form-data; name=\"jobAct\"" + strLineEnd);
                            mOutputStream.writeBytes(strLineEnd);
                            mOutputStream.writeBytes(jobAct + strLineEnd);
                            mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                            ///////////////////////////////////////////////////////////////////////////////////;
                            mOutputStream.writeBytes("Content-Disposition: form-data; name=\"room_num\"" + strLineEnd);
                            mOutputStream.writeBytes(strLineEnd);
                            mOutputStream.writeBytes(room_num + strLineEnd);
                            mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                            ///////////////////////////////////////////////////////////////////////////////////;
                            mOutputStream.writeBytes("Content-Disposition: form-data; name=\"mb_no\"" + strLineEnd);
                            mOutputStream.writeBytes(strLineEnd);
                            mOutputStream.writeBytes(mb_no + strLineEnd);
                            mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                            ///////////////////////////////////////////////////////////////////////////////////;
                            mOutputStream.writeBytes("Content-Disposition: form-data; name=\"sduration\"" + strLineEnd);
                            mOutputStream.writeBytes(strLineEnd);
                            mOutputStream.writeBytes(SoundDuration + strLineEnd);
                            mOutputStream.writeBytes(strTwoHyphens + strBoundary + strLineEnd);
                            ///////////////////////////////////////////////////////////////////////////////////;

                            serverResponseCode = mHttpURLConnection.getResponseCode();
                            serverResponseMessage = mHttpURLConnection.getResponseMessage();

                            InputStream is = mHttpURLConnection.getInputStream();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] byteBuffer = new byte[1024];
                            byte[] byteData = null;
                            int nLength = 0;
                            while((nLength = is.read(byteBuffer)) > 0) {
                                baos.write(byteBuffer, 0, nLength);
                            }
                            byteData = baos.toByteArray();
                            //String response = new String(byteData, "euc-kr");
                            netresult_string = new String(byteData);
                            fileInputStream.close();
                            mOutputStream.flush();
                            mOutputStream.close();

                        } catch (MalformedURLException ex) {
                            ex.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (serverResponseCode == 200) {
                    Log.e("서버받은코드", "MGXKON10 mg=" + netresult_string);

                    if( file_resized == 1) {
                        File file = new File(UPLOAD_Fname);
                        boolean Ok_DEL = file.delete();
                        String f_ex = "삭제후 파일 유무 모름";
                        if (file.exists()) {
                            f_ex = "삭제후 파일 있음 ";
                        } else {
                            f_ex = "삭제후 파일 없음ㅌㅌ ";
                        }

                    }else {

                    }

                    try {
                        JSONObject obj = (JSONObject) new JSONTokener(netresult_string).nextValue();
                        String imagmain  = obj .getString("imagmain"); //
                        String imgicon  = obj .getString("imgicon");
                        String audioduration = obj .getString("audioduration");
                        String extention = obj .getString("extention");
                        //Log.e("JSONObject", obj.toString());
                        //$arr = array("result"=>"OK", "jobact"=>$jobact,"mbno"=>$userno,
                        // "imagmain"=>$ret_main,"imgicon"=>"_", "videotype"=>$fileType,"videoname"=>$wwFileName,"videosize"=>$fileSize);

                        //$arr = array("result"=>"OK", "jobact"=>$jobact,"mbno"=>$userno,
                        // "imagmain"=>$main_name,"imgicon"=>"_", "videotype"=>$fileType,"videoname"=>$ffname,"videosize"=>$fileSize,"Vname"=>$ffname);
                        if( uploadMediaType.equals("video")){//video_Up_from_App(ea_file,ea_size,fn1,fn2)
                            String videotype  = obj .getString("videotype");
                            String videoname  = obj .getString("videoname");
                            String videosize  = obj .getString("videosize");
                            //Log.e("VName" , "Vname :" + videoname );
                            final String ret_jvascript = "javascript:video_Up_from_App('" + videoname + "',"+ videosize+",'" + videotype + "','" + imagmain + "')";
                            ((MainActivity)context).javascript_UploadJobDo(ret_jvascript);
                        }else{
                            upload_result_SendToWebview(imagmain,imgicon ,multi_upCnt , listSize , audioduration,extention);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String[] separated = netresult_string.split("|");
                    fileUpload_main = separated[2];
                    fileUpload_icon = separated[3];
                } else {
                    final int ercode = serverResponseCode;
                    final int ercodex = serverResponseCode;
                    final String ercodestr = serverResponseMessage;
                    final String errmg = "Upload filed. [code :" + ercode + " / " + ercodex + "] message ["+ ercodestr +"]";
                    alertt(errmg);
                }
            }
            //dialog.dismiss();
            ((MainActivity)context).progress_dismiss();
            return "OK";
        }
    }
    public void upload_result_SendToWebview(String imagmain,String imgicon, int nowno, int endno , String duration, String extention){
        //upload_result_SendToWebview(imagmain,imgicon ,multi_upCnt , listSize);

        if( jobAct.equals("myWebFolderAddimage") || jobAct.equals("hotel_folder")){
            if( multi_upCnt >= endno-1){
                final String ret_jvascript = "javascript:rcgback_image_upload('" + jobAct + "','" + mb_no + "','" + imagmain + "','" + imgicon + "'," + multi_upCnt + "," + endno +")";
                ((MainActivity)context).javascript_UploadJobDo(ret_jvascript);
            }else{
                multi_upCnt++;
            }
        }else{
            final String ret_jvascript = "javascript:rcgback_image_upload('" + jobAct + "','" + mb_no + "','" +
                    imagmain + "','" + imgicon + "'," + multi_upCnt + "," + endno +", '" + duration + "','" + extention + "')";
            Log.e("rcgback_image_upload", ret_jvascript);
            ((MainActivity)context).javascript_UploadJobDo(ret_jvascript);
        }
    }

    public String resizeImg(String path,int iOrder){
        Bitmap resultBitmap = null;
        String orgPath = path;
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.fromFile(new File(path)); ;//Uri.parse(path);
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1800000; // 1.2MP
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
                File file = new File(Environment.getExternalStorageDirectory(), "/McCafe/");
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        alertt("Uploading canceld(Cound not Make temp folder)");
                        return "";
                    }
                }
                String new_Fn = Environment.getExternalStorageDirectory() + File.separator + "McCafe/mcTempFile" + iOrder + "_" + mid_n + "." + exten;
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
            Log.e("XXXX", e.getMessage(), e);
            return path;
        }
    }
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        return rotatedImg;
    }
    public static String getDataColumn(Context context, Uri uri,String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection,selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    public void Upload_PopNotice(){

    }
    public void alertt(String text) {
        Log.e("alertt", " text MAIN : " + text);
        Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

}
