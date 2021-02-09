package co.kr.skycall;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.viewpager.widget.ViewPager;
import java.io.File;
import static java.lang.Integer.parseInt;
public class Image_Zoomer extends Activity {
    ImageButton saveBtn;
    private ImageView viewObj ;
    private Context context;
    private Image_Zoomer_acitivty adapter;
    private ViewPager viewPager;
    private String[] mGalImages = null; //new String[] {} ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image_zoom_popup);
        context = this.getApplicationContext();
        Bundle b = getIntent().getExtras();
        String FF = b.getString("fnlist");
        String StnumS = b.getString("startnum");
        if( StnumS==null) StnumS = "0";
        if( StnumS.length() < 1 ) StnumS = "0";
        int stnum = parseInt(StnumS);
        viewObj = (ImageView) findViewById(R.id.imageView1);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        TextView tv = (TextView) findViewById(R.id.paging);
        saveBtn = (ImageButton) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(onButtonClick);
        mGalImages = FF.split(",");
        adapter = new Image_Zoomer_acitivty(Image_Zoomer.this, FF ,stnum ,tv);

        Log.e("이미지 뷰 받고 로딩", "시작점:" + stnum);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(stnum);
    }
    public String getFileName(String url) {
        String filenameWithoutExtension = "";
        String Extens = ".jpg";
        if(url.toLowerCase().endsWith(".png")) Extens = ".png";
        filenameWithoutExtension = String.valueOf(System.currentTimeMillis() + Extens);
        return filenameWithoutExtension;
    }
    Button.OnClickListener onButtonClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Log.e("onClick", "onClick :" + v);
            if (v == saveBtn) {
                int currentItem = viewPager.getCurrentItem();
                String adr = mGalImages[currentItem];
                Log.e("onClick", "현재파일 찾기 :" + v);
                DownloadManager mdDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(adr));
                File destinationFile = new File(Environment.getExternalStorageDirectory(), getFileName(adr));
                request.setDescription("받기중 ...");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationUri(Uri.fromFile(destinationFile));
                mdDownloadManager.enqueue(request);
                showmessage("다운로드 시작");
                //new DownloadFilesTask().execute();
            }
        }
    };
    public void showmessage(String text) {
        Log.e("alertt", " text MAIN : " + text);
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}
