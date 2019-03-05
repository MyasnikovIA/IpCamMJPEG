package ru.miacomsoft.ipcam;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.SystemClock;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import ru.miacomsoft.ipcam.AndroidLib.Android;
import ru.miacomsoft.ipcam.AndroidLib.WebServerAndroid;
import ru.miacomsoft.ipcam.Lib.LockOrientation;

public class MainActivity extends Activity {

    /// https://startandroid.ru/ru/uroki/vse-uroki-spiskom/266-urok-133-kamera-delaem-snimok-i-pishem-video.html
    /// https://startandroid.ru/ru/uroki/vse-uroki-spiskom/264-urok-132-kamera-vyvod-izobrazhenija-na-ekran-obrabotka-povorota.html
    SurfaceView sv;
    SurfaceHolder holder;
    HolderCallback holderCallback;
    Camera camera;
    String StartPathFile;
    String ApplicationName;

    public static byte [] dataJpegPreviev;//getting the byte array

    public static ByteArrayOutputStream output_stream;
    public static byte[] mJpegData;

    public static MainActivity parentActivity;

    final int CAMERA_ID = 0;
    final boolean FULL_SCREEN = false;

    private WebView webView ;
    private Android andoid;
    private WebServerAndroid webServerAndroid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        parentActivity=this;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        output_stream = new ByteArrayOutputStream();
        mJpegData = null;

        webView= (WebView) findViewById(R.id.webView);
        webView.getSettings().setUserAgentString("Desktop");
        new LockOrientation(this).lock();

        webView.getSettings().setJavaScriptEnabled(true);
        andoid=new Android(this,webView);
        webView.addJavascriptInterface(andoid, "Android");

        webServerAndroid = new WebServerAndroid(this,webView);
        webView.addJavascriptInterface(webServerAndroid, "WebServer");

        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(true);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setAllowFileAccess(true);
        webView.loadUrl("file:///android_asset/html/index.html");
        WebServerAndroid.AppPacedjName=getApplicationContext().getPackageName();

        ApplicationName=parentActivity.getApplicationInfo().loadLabel(parentActivity.getPackageManager()).toString();
        StartPathFile= Environment.getExternalStorageDirectory()+ File.separator+ApplicationName+File.separator+"html/img.jpeg";


        sv = (SurfaceView) findViewById(R.id.surfaceView);
        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holderCallback = new HolderCallback();
        holder.addCallback(holderCallback);
    }

    Socket toServer1;

    public DataOutputStream outStream1=null;

    public void connect(View v){
       try{
         toServer1=new Socket(" 192.168.1.100",8000);// connect to server socket of Ip address 192.168.140.101 at port 8000
         outStream1=new DataOutputStream(toServer1.getOutputStream());
       }catch(Exception e){e.printStackTrace();}
     }
    public void SendSrv(View v){
        try {
            String str="gggggggggggggggg";
            outStream.writeInt(str.getBytes().length);
            outStream.write(str.getBytes());
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open(CAMERA_ID);

        param=camera.getParameters();// acquire the parameters for the camera
        size=param.getPreviewSize();// get the size of each frame captured by the camera
        camera.setParameters(param);// setting the parameters to the camera but this line is not required
        /*
        try{
            toServer=new Socket(" 192.168.1.100",8000);// connect to server socket of Ip address 192.168.140.101 at port 8000
            outStream=new DataOutputStream(toServer.getOutputStream());}//open an outputstream to the socket for sending the image data
        catch(Exception e){
            e.printStackTrace();
        }
        */
        camera.setPreviewCallback( MyPreview);
        // setPreviewSize(FULL_SCREEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    class HolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            camera.stopPreview();
            setCameraDisplayOrientation(CAMERA_ID);
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

    }

    void setPreviewSize(boolean fullScreen) {

        // получаем размеры экрана
        Display display = getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();

        // определяем размеры превью камеры
        Camera.Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        // RectF экрана, соотвествует размерам экрана
        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

        // RectF первью
        if (widthIsMax) {
            // превью в горизонтальной ориентации
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            // превью в вертикальной ориентации
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        // подготовка матрицы преобразования
        if (!fullScreen) {
            // если превью будет "втиснут" в экран (второй вариант из урока)
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            // если экран будет "втиснут" в превью (третий вариант из урока)
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        // преобразование
        matrix.mapRect(rectPreview);

        // установка размеров surface из получившегося преобразования
        sv.getLayoutParams().height = (int) (rectPreview.bottom);
        sv.getLayoutParams().width = (int) (rectPreview.right);
    }

    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // получаем инфо по камере cameraId
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // задняя камера
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // передняя камера
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) &&webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Camera.Parameters param;
   private Camera.Size size;
   public DataOutputStream outStream=null;
    long compressionMillis=0;
    long timestampBeforecompression=100;
    Camera.PreviewCallback MyPreview =        new Camera.PreviewCallback() {
         // http://qaru.site/questions/5958992/camera-picture-streaming-as-mjpeg-with-nanohttpd-in-android
        public void onPreviewFrame(byte[] data, Camera camera) {
           try{
               YuvImage yuv_image = new YuvImage(data,param.getPreviewFormat() , size.width, size.height, null); // all bytes are in YUV format therefore to use the YUV helper functions we are putting in a YUV object

              /*
               Matrix matrix = new Matrix();
               matrix.postRotate(90);
               Bitmap bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

               Matrix matrix = new Matrix();
               matrix.postRotate(90);
               byte[] bytes = output_stream.toByteArray();
               Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
               Bitmap.createBitmap(bitmap, 0 , 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
              */

                Rect rect = new Rect(0, 0, size.width, size.height);
                output_stream.reset();
                yuv_image.compressToJpeg(rect, 80, output_stream);// image has now been converted to the jpg format and bytes have been written to the output_stream object
                mJpegData = output_stream.toByteArray();

                synchronized (output_stream) { output_stream.notifyAll();}
            }catch(Exception e){e.printStackTrace();}
            compressionMillis = SystemClock.uptimeMillis() - timestampBeforecompression;
            if (125 - compressionMillis > 0) { SystemClock.sleep(125 - compressionMillis);}
        }
    };

}
