package ru.miacomsoft.ipcam.Lib;

import android.content.Context;

import net.arnx.jsonic.JSON;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by myasnikov on 25.12.15.
 */
public class Sys {

    public static void writeFile(Context context, String FileName, HashMap<String, String> msg) {
        try {
            // отрываем поток для записи
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(FileName, context.MODE_PRIVATE)));
            // пишем данные
            bw.write(JSON.encode(msg));
            // закрываем поток
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, String> readFile(Context context, String FileName) {
        StringBuffer sb = new StringBuffer();
        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(FileName)));
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
        HashMap<String, String> decode = JSON.decode(sb.toString());
        return decode;
    }

    /**
     * Отправить список Контент провайдеров
     */
    public static void sendListProvider(Context context, String FiltrText) {

        try {
            System.out.write(("HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html; charset=UTF-8\r\n"
                    + "Connection: close\r\n"
                    + "Server: HTMLserver\r\n\r\n").getBytes());
            System.out.flush();
            /*
            for (PackageInfo pack : context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS)) {
                ProviderInfo[] providers = pack.providers;
                if (providers != null) {
                    for (ProviderInfo provider : providers) {
                        String providerString = provider.authority;
                        if (providerString != null) {
                            if (providerString.contains(FiltrText)) {
                                System.out.write(("<a href='" + providerString + "'>" + providerString +"</a>&nbsp&nbsp&nbsp&nbsp"+provider.processName+"&nbsp&nbsp"+provider.name+" <br>").getBytes());
                                System.out.flush();

                            }
                        }
                    }
                }
            }*/
            System.out.write(("Page not found").getBytes());
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }







    public static void sendRawFile(File pageFile) {
        try {
            FileReader fileInput = new FileReader(pageFile);
            String Code = fileInput.getEncoding();
            fileInput.close();
            String TypeCont = ContentType(pageFile);
            // Первая строка ответа
            System.out.write("HTTP/1.1 200 OK\r\n".getBytes());
            // дата создания в GMT
            DateFormat df = DateFormat.getTimeInstance();
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            // Время последней модификации файла в GMT
            System.out.write(("Last-Modified: " + df.format(new Date(pageFile.lastModified())) + "\r\n").getBytes());
            // Длина файла
            System.out.write(("Content-Length: " + pageFile.length() + "\r\n").getBytes());
            System.out.write(("Content-Type: " + TypeCont + "; ").getBytes());
            System.out.write(("charset=" + Code + "\r\n").getBytes());
            // Остальные заголовки
            System.out.write("Connection: close\r\n".getBytes());
            System.out.write("Server: HTMLserver\r\n\r\n".getBytes());
            // Сам файл:
            FileInputStream fis = new FileInputStream(pageFile.getAbsolutePath());
            int lengRead = 1;
            byte buf[] = new byte[1024];
            while ((lengRead = fis.read(buf)) != -1) {
                System.out.write(buf, 0, lengRead);
            }
            // закрыть файл
            fis.close();
            // завершаем соединение
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        } catch (IOException ex) {
            Logger.getLogger(Sys.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private static String ContentType(File pageFile) {
        String ras = null;
        // путь без файла
        String Dir = pageFile.getPath().replace(pageFile.getName(), "").toLowerCase();
        // имя файла с расширением
        String FileName = pageFile.getName();
        // расширение файла
        String rashirenie = FileName.substring(FileName.lastIndexOf(".") + 1);
        // путь к файлу + имя файла - расширение файла
        String DirFile = pageFile.getPath().replace("." + rashirenie, "");
        // имя файла без расширения
        String File2 = FileName.replace("." + rashirenie, "");
        rashirenie = rashirenie.toLowerCase();// преобразуем в нижний регистр
        if (rashirenie.equals("css")) {
            return "text/css";
        }
        if (rashirenie.equals("js")) {
            return "application/x-javascript";
        }
        if (rashirenie.equals("xml") || rashirenie.equals("dtd")) {
            return "text/xml";
        }
        if ((rashirenie.equals("txt")) || (rashirenie.equals("inf")) || (rashirenie.equals("nfo"))) {
            return "text/plain";
        }
        if ((rashirenie.equals("html")) || (rashirenie.equals("htm")) || (rashirenie.equals("shtml")) || (rashirenie.equals("shtm")) || (rashirenie.equals("stm")) || (rashirenie.equals("sht"))) {
            return "text/html";
        }
        if ((rashirenie.equals("mpeg")) || (rashirenie.equals("mpg")) || (rashirenie.equals("mpe"))) {
            return "video/mpeg";
        }
        if ((rashirenie.equals("ai")) || (rashirenie.equals("ps")) || (rashirenie.equals("eps"))) {
            return "application/postscript";
        }
        if (rashirenie.equals("rtf")) {
            return "application/rtf";
        }
        if ((rashirenie.equals("au")) || (rashirenie.equals("snd"))) {
            return "audio/basic";
        }
        if ((rashirenie.equals("bin")) || (rashirenie.equals("dms")) || (rashirenie.equals("lha")) || (rashirenie.equals("lzh")) || (rashirenie.equals("class")) || (rashirenie.equals("exe"))) {
            return "application/octet-stream";
        }
        if (rashirenie.equals("doc")) {
            return "application/msword";
        }
        if (rashirenie.equals("pdf")) {
            return "application/pdf";
        }
        if (rashirenie.equals("ppt")) {
            return "application/powerpoint";
        }
        if ((rashirenie.equals("smi")) || (rashirenie.equals("smil")) || (rashirenie.equals("sml"))) {
            return "pplication/smil";
        }
        if (rashirenie.equals("zip")) {
            return "application/zip";
        }
        if ((rashirenie.equals("midi")) || (rashirenie.equals("kar"))) {
            return "audio/midi";
        }
        if ((rashirenie.equals("mpga")) || (rashirenie.equals("mp2")) || (rashirenie.equals("mp3"))) {
            return "audio/mpeg";
        }
        if (rashirenie.equals("wav")) {
            return "audio/x-wav";
        }
        if (rashirenie.equals("ief")) {
            return "image/ief";
        }

        if ((rashirenie.equals("jpeg")) || (rashirenie.equals("jpg")) || (rashirenie.equals("jpe"))) {
            return "image/jpeg";
        }
        if (rashirenie.equals("png")) {
            return "image/png";
        }
        if (rashirenie.equals("ico")) {
            return "image/x-icon";
        }
        if ((rashirenie.equals("tiff")) || (rashirenie.equals("tif"))) {
            return "image/tiff";
        }
        if ((rashirenie.equals("wrl")) || (rashirenie.equals("vrml"))) {
            return "model/vrml";
        }
        if (rashirenie.equals("avi")) {
            return "video/x-msvideo";
        }
        if (rashirenie.equals("flv")) {
            return "video/x-flv";
        }
        if (rashirenie.equals("ogg")) {
            return "video/ogg";
        }
        return "application/octet-stream";
    }


}
