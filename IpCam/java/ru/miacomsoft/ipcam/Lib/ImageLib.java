package ru.miacomsoft.ipcam.Lib;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;
import android.widget.ZoomButtonsController;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by MyasnikovIA on 05.02.19.
 */
public class ImageLib {

   public static String encodeImage(Bitmap bm)
    {
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        return  "data:image/jpeg;base64,"+Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
         // return "data:image/png;base64,"+Base64.encodeToString(baos.toByteArray(), Base64.URL_SAFE | Base64.NO_WRAP);
         // return "data:image/png;base64,"+Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    public static String encodeImage(String path)
    {
        File imagefile = new File(path);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(imagefile);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        return  "data:image/jpeg;base64,"+Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    public static double hashBitmap(Bitmap bmp) {
        long hash = 31; // or a higher prime at your choice
        for (int x = 0; x < bmp.getWidth(); x++) {
            for (int y = 0; y < bmp.getHeight(); y++) {
                hash *= (bmp.getPixel(x, y) + 31);
            }
        }
        return hash;
    }

    public static Bitmap getBitmapFromWebView(WebView webView) {
        try {
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

            //Measure WebView's content
            webView.measure(widthMeasureSpec, heightMeasureSpec);
            webView.layout(0, 0, webView.getMeasuredWidth(), webView.getMeasuredHeight());

            //Build drawing cache and store its size
            webView.buildDrawingCache();

            int measuredWidth = webView.getMeasuredWidth();
            int measuredHeight = webView.getMeasuredHeight();

            //Creates the bitmap and draw WebView's content on in
            Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);

            Paint paint = new Paint();

            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bitmap, 0, bitmap.getHeight(), paint);

            webView.draw(canvas);
            webView.destroyDrawingCache();

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getBasicAuthValue(String usr, String pwd) {
        String credentials = usr + ":" + pwd;
        int flags = Base64.URL_SAFE | Base64.NO_WRAP;
        byte[] bytes = credentials.getBytes();
        return "Basic " + Base64.encodeToString(bytes, flags);
    }

    /**
            * Disable zoom buttons for WebView.
    */
    public static void disableWebviewZoomControls(final WebView webview) {
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);

        // Use the API 11+ calls to disable the controls
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            new Runnable() {
                @SuppressLint("NewApi")
                public void run() {
                    webview.getSettings().setDisplayZoomControls(false);
                }
            }.run();
        }
        else {
            try {
                ZoomButtonsController zoom_control;
                zoom_control = ((ZoomButtonsController) webview.getClass().getMethod("getZoomButtonsController").invoke(webview, (Object[]) null));
                zoom_control.getContainer().setVisibility(View.GONE);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
