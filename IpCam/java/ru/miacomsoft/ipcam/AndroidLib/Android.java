package ru.miacomsoft.ipcam.AndroidLib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import net.arnx.jsonic.JSON;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import ru.miacomsoft.ipcam.MainActivity;


/**
 * Created by MyasnikovIA on 01.06.17.
 * Диалоговые окна
 * http://developer.alexanderklimov.ru/android/alertdialog.php
 */
public class Android {

    public static final String BUFFER_EVENT_VAR = "bufferEventVar";

    private long lastUpdate;
    private  List<Sensor> mList;
    private String ApplicationName;
    public String StartPathFile="";

    private String SerialSim;
    private String AndroidId;
    private String DeviceId;
    private String UUIDId;



    String[] AssetFilesString;
    File AssetFilesDir;
    //    webView.loadUrl("javascript: Accel="+jsonObject.toString()   );
    private MainActivity parentActivity;
    private WebView webView;
    public Android(MainActivity activity, WebView webViewPar)  {
        webView=webViewPar;
        parentActivity = activity;
        lastUpdate = System.currentTimeMillis();
        ApplicationName=parentActivity.getApplicationInfo().loadLabel(parentActivity.getPackageManager()).toString();
        // copyFileOrDir("html");    /// копируем HTML Asset в SDcard
        StartPathFile="file://"+Environment.getExternalStorageDirectory()+File.separator+ApplicationName+File.separator;

         // <!-- получить информацию об устройстве -->
         //  <uses-permission android:name="android.permission.READ_PHONE_STATE" />
         TelephonyManager tm = (TelephonyManager) parentActivity.getSystemService(parentActivity.TELEPHONY_SERVICE);
         SerialSim = "" + tm.getSimSerialNumber();
         AndroidId = "" + android.provider.Settings.Secure.getString(parentActivity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
         DeviceId = "" + tm.getDeviceId();
         UUIDId = new UUID(AndroidId.hashCode(), ((long) tm.getDeviceId().hashCode() << 32) | tm.getSimSerialNumber().hashCode()).toString();
    }



    public String getSerialSim(){
        return UUIDId;
    }


    public String getUUIDId(){
        return UUIDId;
    }


    public String getAndroidId(){
        return AndroidId;
    }

    public String getDeviceId(){
        return DeviceId;
    }

    public void goBack(){
       webView.goBack();
    }
    public void Reload(){
        webView.reload();
    }


    public void url(String UrlStr){
        webView.loadUrl(UrlStr);
    }


    /**
     * Переход в браузер
     * @param UrlStr - строка запроса
     */
    public void getBrouser(String UrlStr){
        if( (UrlStr.toLowerCase().indexOf("http://") == -1) &&(UrlStr.toLowerCase().indexOf("https://") == -1))
        {
            UrlStr+="http://";
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(UrlStr)) ;
        parentActivity.startActivity(browserIntent);
    }

    /**
     * Запись текста в файл
     * @param Str
     * @param FileName
     */
    public void writeFile(String Str,String FileName){
        try {
            // отрываем поток для записи
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter( parentActivity.openFileOutput(FileName,  parentActivity.MODE_PRIVATE)));
            // пишем данные
            bw.write(Str);
            // закрываем поток
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Чтение текстового файла
     * @param FileName
     * @return
     */
    public String readFile(String FileName){
        StringBuffer sb = new StringBuffer();
        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new InputStreamReader(parentActivity.openFileInput(FileName)));
            String str = "";
            // читаем содержимое
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return   sb.toString();
    }


    @JavascriptInterface
    public void showMessage(final String message) {

        final Activity theActivity = parentActivity;
        final WebView theWebView = webView;
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //  if(!theWebView.getUrl().startsWith("http://tutorials.jenkov.com")){
                //      return ;
                // }
                Toast toast = Toast.makeText(
                        theActivity.getApplicationContext(),
                        message,
                        Toast.LENGTH_SHORT);

                toast.show();
            }
        });
    }

    /**
     *  JS функция вывода сообщения во вст\плывающем окне
     * @param msg
     */
    public void alert(String msg){




        //  Toast.makeText(parentActivity, msg, Toast.LENGTH_LONG).show();
        new AlertDialog.Builder(parentActivity)
                .setMessage(msg)
                //.setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // setResult(RESULT_CANCELED);
                                // finish();
                            }
                        }).show();
    }

    public static void Alert(String msg, MainActivity  parentActivity){
        new AlertDialog.Builder(parentActivity)
                .setMessage(msg)
                        //.setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // setResult(RESULT_CANCELED);
                                // finish();
                            }
                        }).show();
    }

        public String getIP(){
        WifiManager wifiMgr1 = (WifiManager) parentActivity .getSystemService(parentActivity .WIFI_SERVICE);
        WifiInfo wifiInfo1 = wifiMgr1.getConnectionInfo();
        int ip = wifiInfo1.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);
        return ipAddress;
    }


    public String GetAssetFilesDir(){
        AssetFilesDir=new File(AssetFilesString[0]);
        AssetFilesDir=AssetFilesDir.getParentFile();
       try {
            File file = new File("test111.txt");
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(GetUrlContent("https://commonsware.com/Android/previews/assets-files-and-data-parsing","_SYSTEM","SYS").getBytes());
            out.close();
        }catch (Throwable t) {
            Toast.makeText(parentActivity, "Exception: " + t.toString(), 2000).show();
        }
       return  parentActivity.getApplicationContext().getFilesDir().getAbsolutePath();
        // return  AssetFilesDir.getAbsolutePath();
    }

    /**
     *
     * @param urlStr
     * @param user
     * @param pass
     * @return
     */
    public String GetUrlContent(String urlStr, String user, String pass) {
        try {
            DefaultHttpClient Client = new DefaultHttpClient();
            Client.getCredentialsProvider().setCredentials(
            AuthScope.ANY,
             new UsernamePasswordCredentials(user,pass)
        );
        HttpGet httpGet = new HttpGet(urlStr);
        HttpResponse response = Client.execute(httpGet);
        // System.out.println("response = " + response);
        BufferedReader breader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder responseString = new StringBuilder();
        String line = "";
        while ((line = breader.readLine()) != null) {
            responseString.append(line);
        }
        breader.close();
        return  responseString.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Загрузить файл с сайта с авторизацией
     * @param urlStr
     * @param user
     * @param pass
     * @param outFilePath
     */
    public  String LoadUrlContent(String urlStr, String user, String pass, String outFilePath) {
        try {
            // URL url = new URL ("http://ip:port/download_url");
            // outFilePath="/test.txt"; //AssetFilesDir
            if (outFilePath.length()==0){
                String[] isbnParts = urlStr.split("/");
                String FileName=isbnParts[isbnParts.length-1];
                if (FileName.length()==0){
                    return "Error FileName";
                }
                outFilePath=FileName;
            }
            outFilePath=Environment.getExternalStorageDirectory()+"/"+ApplicationName+"/html/"+outFilePath;
            URL url = new URL(urlStr);
            String authStr = user + ":" + pass;
            String authEncoded = Base64.encodeToString(authStr.getBytes(), Base64.DEFAULT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + authEncoded);
            File file = new File(outFilePath);
            InputStream in = (InputStream) connection.getInputStream();
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            for (int b; (b = in.read()) != -1;) {
                out.write(b);
            }
            out.close();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "Ok";
    }



    //Получение всего обьема внутренней памяти
    public static long getMemSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    public  String getDataDir() {
        try {
            return parentActivity.getPackageManager().getPackageInfo(parentActivity.getPackageName(), 0).applicationInfo.dataDir;
        } catch (Exception e) {
            return null;
        }
    }

    //Получить Свободной памяти на SD карте
    public static long getFreeSDSize() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long bSize = sf.getBlockSize();
            long availBlocks = sf.getAvailableBlocks();
            return bSize * availBlocks;
        } else {
            return -1;
        }
    }

    public String getIp() {
        WifiManager wifiMgr = (WifiManager) parentActivity.getSystemService(parentActivity.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return   Formatter.formatIpAddress(ip);
    }

    /**
     * URL-encodes everything between "/"-characters.
     * Encodes spaces as '%20' instead of '+'.
     */
    private String encodeUri( String uri )
    {
        String newUri = "";
        StringTokenizer st = new StringTokenizer( uri, "/ ", true );
        while ( st.hasMoreTokens())
        {
            String tok = st.nextToken();
            if ( tok.equals( "/" ))
                newUri += "/";
            else if ( tok.equals( " " ))
                newUri += "%20";
            else
            {
                newUri += URLEncoder.encode(tok);
                // For Java 1.4 you'll want to use this instead:
                // try { newUri += URLEncoder.encode( tok, "UTF-8" ); } catch ( java.io.UnsupportedEncodingException uee ) {}
            }
        }
        return newUri;
    }

    public String getDirList(String StartPath){
         Map<String,Map<String,String>> FileList=new HashMap<String,Map<String,String>>();
         Map<String,String> FileOne;
        // File sdCardPath = Environment.getExternalStorageDirectory().getParentFile();
        File sdCardPath;
        if(StartPath.length()==0){
            sdCardPath =new File( Environment.getExternalStorageDirectory()+File.separator+ApplicationName+File.separator+"html");
        }else{
            sdCardPath = new File(StartPath);
        }
        if (!sdCardPath.exists()){
            sdCardPath = Environment.getExternalStorageDirectory().getParentFile();
        }
        FileOne=new HashMap<String,String>();
        FileOne.put("Path",sdCardPath.getParentFile().getAbsolutePath());
        FileOne.put("Name",sdCardPath.getParentFile().getName());
        FileOne.put("Dirname", sdCardPath.getParentFile().getAbsolutePath());
        FileOne.put("ViewName","...");
        FileOne.put("Type","dir");
        FileList.put(FileOne.get("ViewName"),FileOne);

        FileOne=new HashMap<String,String>();
        FileOne.put("Path",Environment.getDataDirectory().getAbsolutePath());
        FileOne.put("Name", Environment.getDataDirectory().getName());
        FileOne.put("Dirname", Environment.getDataDirectory().getAbsolutePath());
        FileOne.put("ViewName","./DATA/.");
        FileOne.put("Type","dir");
        FileList.put(FileOne.get("ViewName"),FileOne);

        FileOne=new HashMap<String,String>();
        FileOne.put("Path", Environment.getExternalStorageDirectory().getAbsolutePath());
        FileOne.put("Name", Environment.getExternalStorageDirectory().getName());
        FileOne.put("Dirname", Environment.getExternalStorageDirectory().getAbsolutePath());
        FileOne.put("ViewName","./SDCARD/.");
        FileOne.put("Type","dir");
        FileList.put(FileOne.get("ViewName"),FileOne);


        String dir = "";
        File[] paths = sdCardPath.listFiles();
        for (File path : paths) {
            Date lastMod = new Date(path.lastModified());
            String size =String.valueOf( path.getTotalSpace());
            if (path.isDirectory()) {
                FileOne=new HashMap<String,String>();
                FileOne.put("Path",path.getAbsolutePath());
                FileOne.put("Name",path.getName());
                FileOne.put("Dirname",path.getAbsolutePath());
                FileOne.put("ViewName",path.getName());
                FileOne.put("Type","dir");
                FileList.put(FileOne.get("ViewName"),FileOne);
            } else {
                FileOne=new HashMap<String,String>();
                FileOne.put("Path",path.getAbsolutePath());
                FileOne.put("Name",path.getName());
                FileOne.put("Dirname",path.getParentFile().getAbsolutePath());
                FileOne.put("ViewName",path.getName());
                FileOne.put("Type","file");
                FileList.put(FileOne.get("ViewName"),FileOne);
            }
        }
        return JSON.encode(FileList, false);
    }


    ////-------------------------------------------------------------------------------------
    ////-------------------------------------------------------------------------------------
    ////-------------------------------------------------------------------------------------
    ////-------------------------------------------------------------------------------------
    private void copyFileOrDir(String path) {
        AssetManager assetManager = parentActivity.getAssets();
        String fullPath =  Environment.getExternalStorageDirectory()+"/"+ApplicationName+"/" + path;
        // if (new File(fullPath).exists()==true){return;}
        String assets[] = null;
        try {
            Log.i("tag", "copyFileOrDir() " + path);
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                // String fullPath =Environment.getExternalStorageDirectory()+"/CacheWebBrowser/files/"+ path;
                Log.i("tag", "path="+fullPath);
                File dir = new File(fullPath);
                if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                    if (!dir.mkdirs())
                        Log.i("tag", "could not create dir "+fullPath);
                for (int i = 0; i < assets.length; ++i) {
                    String p;
                    if (path.equals(""))
                        p = "";
                    else
                        p = path + "/";
                    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                        copyFileOrDir( p + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }
    private void copyFile(String filename) {
        AssetManager assetManager = parentActivity.getAssets();
        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            Log.i("tag", "copyFile() "+filename);
            in = assetManager.open(filename);
            if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file
                newFileName = Environment.getExternalStorageDirectory()+"/"+ApplicationName+"/" + filename.substring(0, filename.length()-4);
            else
                newFileName = Environment.getExternalStorageDirectory()+"/"+ApplicationName+"/"+ filename;
            // if (new File(newFileName).exists()==true){return;}
            out = new FileOutputStream(newFileName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", "Exception in copyFile() of "+newFileName);
            Log.e("tag", "Exception in copyFile() "+e.toString());
        }

    }
    ////-------------------------------------------------------------------------------------
    ////-------------------------------------------------------------------------------------
    ////-------------------------------------------------------------------------------------
    ////-------------------------------------------------------------------------------------



}
