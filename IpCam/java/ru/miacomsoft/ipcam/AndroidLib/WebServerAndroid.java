package ru.miacomsoft.ipcam.AndroidLib;

import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Base64;
import android.webkit.WebView;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import ru.miacomsoft.ipcam.Lib.Sys;
import ru.miacomsoft.ipcam.MainActivity;
import ru.miacomsoft.ipcam.services.ServiceExample;

/**
 * Created by MyasnikovIA on 13.02.19.
 */
public class WebServerAndroid {

    private HashMap<String, String> Setup = new HashMap<String, String>(10, (float) 0.5);
    public static String ApplicationName;
    private String StartPathFile;
    public static String AppPacedjName;



    private MainActivity parentActivity;
    private WebView webView;
    public WebServerAndroid (MainActivity activity, WebView webViewPar)  {
        webView=webViewPar;
        parentActivity = activity;
        ApplicationName=parentActivity.getApplicationInfo().loadLabel(parentActivity.getPackageManager()).toString();
        StartPathFile= Environment.getExternalStorageDirectory()+ File.separator+ApplicationName+File.separator+"html";
        if (!new File(StartPathFile).exists()){
            new File(StartPathFile).mkdirs();
        }

        Setup = Sys.readFile(parentActivity, "conf.ini");
        if (Setup.size() == 0) {
            String Author = new String(Base64.encode(("user" + ":" + "user").getBytes(), Base64.DEFAULT));
            Setup.put("UserPort", "8080");
            Setup.put("Author", Author);
            Setup.put("Interval", "3000");
            Setup.put("DefaultHost", "index.html");
            Setup.put("CharSet", "cp1251");
            Setup.put("IsAutorization", "0");
            Setup.put("StartPath", StartPathFile);
        }
    }


    public void Start(){
        Setup.put("run", "1");
        Sys.writeFile( parentActivity, "conf.ini", Setup);
        parentActivity.startService(new Intent( parentActivity.getBaseContext(), ServiceExample.class));
    }

    public void Stop(){
        Setup.put("run", "0");
        Sys.writeFile( parentActivity, "conf.ini", Setup);
        parentActivity.stopService(new Intent(parentActivity.getBaseContext(), ServiceExample.class));
    }





    public String getStartPathFile(){
        return StartPathFile;
    }

    public String getGetSDCard(){
        String data=Environment.getExternalStorageDirectory()+File.separator;
        File dataFile=new File(data);
        if (!dataFile.exists()){
            dataFile.mkdirs();
        }
        return  dataFile.getAbsolutePath();
    }
    public String getGetDataDir(){
        String data=Environment.getDataDirectory()+File.separator+"data"+File.separator+WebServerAndroid.AppPacedjName+File.separator+"html";
        File dataFile=new File(data);
        if (!dataFile.exists()){
            dataFile.mkdirs();
        }
        return  dataFile.getAbsolutePath();
        // return     Environment.getDataDirectory()+File.separator+"data"+File.separator+Settings.Secure.ANDROID_ID;
    }

    public String getGetRootDir(){
        return     Environment.getRootDirectory()+File.separator;
    }





    public String getStatus(){
        return Setup.get("run");
    }
    public String getHost(){
        return Setup.get("DefaultHost");
    }
    public void setHost(String HostName){
        Setup.put("DefaultHost", HostName);
        Sys.writeFile( parentActivity, "conf.ini", Setup);
    }
    public String getPort(){
        return Setup.get("UserPort");
    }
    public void setPort(String HostPort){
        Setup.put("UserPort", HostPort);
        Sys.writeFile( parentActivity, "conf.ini", Setup);
    }

    public String getChar(){
        return Setup.get("CharSet");
    }
    public void setChar(String CharSet){
        Setup.put("CharSet", CharSet);
        Sys.writeFile( parentActivity, "conf.ini", Setup);
    }

    public String getAuthor(){
        String Author = Setup.get("Author");
        Author=new String(Base64.decode((Author).getBytes(), Base64.DEFAULT));
        return Author.split(":")[0];
    }
    public String getPass(){
        String Author = Setup.get("Author");
        Author=new String(Base64.decode((Author).getBytes(), Base64.DEFAULT));
        return Author.split(":")[1];
    }

    public void setAuthor(String UserName,String UserPass){
        String Author = new String(Base64.encode((UserName + ":" + UserPass).getBytes(), Base64.DEFAULT));
        Setup.put("Author", Author);
        Sys.writeFile(parentActivity, "conf.ini", Setup);
    }






    public String getWebAuthor(){
        return Setup.get("IsAutorization");
    }
    public void setWebAuthor(String isAuthor){
        Setup.put("IsAutorization", isAuthor);
        Sys.writeFile( parentActivity, "conf.ini", Setup);
    }

    public String getInterval(){
        return Setup.get("Interval");
    }

    public void setInterval(String Interval){
        Setup.put("Interval", Interval);
        Sys.writeFile(parentActivity, "conf.ini", Setup);
    }

    public void StartBrowser(){
        WifiManager wifiMgr1 = (WifiManager) parentActivity.getSystemService(parentActivity.WIFI_SERVICE);
        WifiInfo wifiInfo1 = wifiMgr1.getConnectionInfo();
        int ip = wifiInfo1.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + ipAddress + ":" +  Setup.get("UserPort")));
        parentActivity.startActivity(browserIntent);
    }

    public String getStartPath(){
        return Setup.get("StartPath");
    }

    public void setStartPath(String StartPathFile){
      Setup.put("StartPath", StartPathFile);
      Sys.writeFile(parentActivity, "conf.ini", Setup);
    }



    public String getDirList(String StartPath,String FileListConteyner){
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
        StringBuffer sb= new StringBuffer();
        sb.append( StartPath+"<br/>");
        sb.append("<div class='list-group' id='"+FileListConteyner+"'>");

        sb.append("<a type=\"button\" " +
                " class=\"list-group-item list-group-item-action\" " +
                " data-path='"+Environment.getDataDirectory().getAbsolutePath()+"'" +
                " data-name='"+Environment.getDataDirectory().getName()+"'" +
                " data-type='dir'" +
                " data-dir='"+Environment.getDataDirectory().getAbsolutePath()+
                //" onclick=\" Android.showMessage('"+Environment.getDataDirectory().getAbsolutePath()+"');   \""+
                 " onclick=\" SelectFileName='"+Environment.getDataDirectory().getAbsolutePath()+"';  \""+
                // " ondblclick=' \""+Environment.getDataDirectory().getAbsolutePath()+"\" );  '"+
                "'>" +
                " ./DATA/." +
                " </a>");

        sb.append("<a type=\"button\" " +
                " class=\"list-group-item list-group-item-action\" " +
                " data-path='"+Environment.getExternalStorageDirectory().getAbsolutePath()+"'" +
                " data-name='"+Environment.getExternalStorageDirectory().getName()+"'" +
                " data-type='dir'" +
                " data-dir='"+Environment.getExternalStorageDirectory().getAbsolutePath()+"'>" +
                " ./SDCARD/." +
                " </a>");

        sb.append("<a type=\"button\" " +
                " class=\"list-group-item list-group-item-action\" " +
                " data-path='"+sdCardPath.getParentFile().getAbsolutePath()+"'" +
                " data-name='"+sdCardPath.getParentFile().getName()+"'" +
                " data-type='dir'" +
                " data-dir='"+sdCardPath.getParentFile().getAbsolutePath()+"'>" +
                "..." +
                " </a>");

        File[] paths = sdCardPath.listFiles();
        for (File path : paths) {
            Date lastMod = new Date(path.lastModified());
            String size =String.valueOf( path.getTotalSpace());
            if (path.isDirectory()) {
                sb.append("<a type=\"button\" " +
                        " class=\"list-group-item list-group-item-action\" " +
                        " data-path='"+path.getAbsolutePath()+"'" +
                        " data-name='"+path.getName()+"'" +
                        " data-type='dir'" +
                        " data-dir='"+path.getAbsolutePath()+"'>" +
                        "" +path.getName()+
                        " </a>");

            } else {
                sb.append("<a type=\"button\" " +
                        " class=\"list-group-item list-group-item-action\" " +
                        " data-path='" + path.getAbsolutePath() + "'" +
                        " data-name='" + path.getName() + "'" +
                        " data-type='file'" +
                        " data-dir='" + path.getParentFile().getAbsolutePath() + "'>" +
                        "" + path.getName() +
                        " </a>");
            }
        }
        sb.append("</div>");

        return sb.toString();
    }




}
