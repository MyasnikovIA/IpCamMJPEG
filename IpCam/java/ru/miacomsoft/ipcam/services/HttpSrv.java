package ru.miacomsoft.ipcam.services;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import net.arnx.jsonic.JSON;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.miacomsoft.ipcam.Lib.Sys;
import ru.miacomsoft.ipcam.MainActivity;


/**
 * @author myasnikov
 */
public class HttpSrv {
    private static Context context;

    public HttpSrv(Context context) {
        this.context = context;
    }

    private static String UserName;
    private static String UserPass;

    private static int numComp = 0;
    private static String IPmac = "";
    private static String DefaultHost = "";

    public static int port = 9090;
    public static boolean IsAutorization = true;
    static public boolean process = false;
    static public File sdcard;
    static public String CharSet ="utf-8";

    //            String authString = "user" + ":" + "123";
    //            code = new String(Base64.encode(authString.getBytes()));

    public void Start(HashMap<String, String> Setup) {
        this.UserName  = Setup.get("UserName");
        this.UserPass = Setup.get("UserPass");
        this.DefaultHost = Setup.get("DefaultHost");
        this.port = Integer.valueOf(Setup.get("UserPort"));
        if(Setup.get("CharSet")!=null){
            this.CharSet=     Setup.get("CharSet");
        }
        if (Setup.get("IsAutorization").equals("0")) {
            IsAutorization = false;
        } else {
            IsAutorization = true;
        }


        Setup.get("StartPath");

        process = true;
        //sdcard = Environment.getExternalStorageDirectory();
        sdcard = new File(Setup.get("StartPath"));
        // получаем IP адрес и MAC адрес сервера
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            IPmac = ip.getHostAddress() + "|" + new String(network.getHardwareAddress());
        } catch (Exception ex) {
            IPmac = "NoIP|Nomac";
        }

        // Toast.makeText(context, "Start Web Server", Toast.LENGTH_LONG).show();
        Thread myThready;
        myThready = new Thread(new Runnable() {
            public void run() {
                try {
                    ServerSocket ss = new ServerSocket(HttpSrv.port);
                    while (process == true) {
                        numComp++;
                        Socket socket = ss.accept();
                        new Thread(new SocketProcessor(socket)).start();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Throwable ex) {
                    Logger.getLogger(HttpSrv.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        myThready.start();    //Запуск потока

    }

    /**
     * Остановить сервер
     */
    public void Stop() {
        process = false;
        // Toast.makeText(context, "Stop File Web Server", Toast.LENGTH_LONG).show();
    }

    private static class SocketProcessor implements Runnable {


        private static Object StandardLog;
        private Socket socket;
        private InputStream is;
        private OutputStream os;
        private String contentZapros = "";

        private SocketProcessor(Socket socket) throws Throwable {
            this.socket = socket;

            this.is = socket.getInputStream();
            this.os = socket.getOutputStream();
            Headers.clear();
            String Adress = socket.getRemoteSocketAddress().toString();
            Headers.put("RemoteIPAdress", Adress);
            Adress = Adress.split(":")[0];
            Adress = Adress.substring(1, Adress.length());
        }

        public void run() {
            try {
                readInputHeaders();
                writeResponse();
            } catch (Throwable t) {
            } finally {
                try {
                    socket.close();
                } catch (Throwable t) {
                    /*do nothing*/
                }
            }
        }

        private HashMap<String, Object> Headers = new HashMap<String, Object>(10, (float) 0.5);
        private HashMap<String, String> inParam = new HashMap<String, String>(10, (float) 0.5);
        private byte[] PostByte = new byte[0];
        private String Koderovka = "";
        //  private String getCommand = "";
        private String getCmd = "";

        /**
         * Чтение входных данных от клиента
         *
         * @throws java.io.IOException
         */
        private void readInputHeaders() throws IOException {
            // FileWriter outLog = new FileWriter(rootPath + "\\log.txt", true); //the true will append the new data
            //  outLog.write("add a line\n");//appends the string to the file
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            contentZapros = "";

            StringBuffer sbInData = new StringBuffer();
            int numLin = 0;
            InputStreamReader isr = new InputStreamReader(is);
            int charInt;
            char[] charArray = new char[1024];
            // Читаем заголовок
            StringBuffer sb = new StringBuffer();
            StringBuffer sbTmp = new StringBuffer();
            while ((charInt = isr.read()) > 0) {
                if (socket.isConnected() == false) {
                    return;
                }
                //   System.out.write((char) charInt);
                //  outLog.write((char) charInt);
                sbTmp.append((char) charInt);
                if (sbTmp.toString().indexOf("\n") != -1) {
                    if (sbTmp.toString().length() == 2) {
                        break; // чтение заголовка окончено
                    }
                    sbTmp.setLength(0);
                }
                sb.append((char) charInt);
            }

            int indLine = 0;
            for (String TitleLine : sb.toString().split("\r\n")) {
                indLine++;
                if (indLine == 1) {
                    TitleLine = TitleLine.replaceAll("GET /", "");
                    TitleLine = TitleLine.replaceAll("POST /", "");
                    TitleLine = TitleLine.replaceAll(" HTTP/1.1", "");
                    TitleLine = TitleLine.replaceAll(" HTTP/1.0", "");
                    contentZapros = java.net.URLDecoder.decode(TitleLine, "UTF-8");
                    // Json.put("ContentZapros", contentZapros);
                    // System.out.println("=-=" + contentZapros);
                    if (contentZapros.indexOf("?") != -1) {

                        String tmp = contentZapros.substring(0, contentZapros.indexOf("/?") + 2);
                        String param = contentZapros.replace(tmp, "");
                        getCmd = param;
                        Headers.put("ParamAll", param);
                        contentZapros = tmp.substring(0, tmp.length());
                        int indParam = 0;
                        for (String par : param.split("&")) {
                            String[] val = par.split("=");
                            if (val.length == 2) {
                                Headers.put(val[0], val[1]);
                                val[0] = val[0].replace(" ", "_");
                                Headers.put(val[0], val[1]);
                            } else {
                                indParam++;
                                Headers.put("Param" + String.valueOf(indParam), val[0]);
                            }
                        }
                        contentZapros = tmp.substring(0, tmp.length() - 2);//.toLowerCase()
                    }

                    if (contentZapros.length() == 0) {
                        contentZapros = DefaultHost;
                    }
                    Headers.put("Zapros", contentZapros);
                    Headers.put("RootPath", sdcard.getAbsolutePath());
                    File pathPege = new File(sdcard.getAbsolutePath() + "/" + contentZapros);
                    Headers.put("AbsalutZapros", pathPege.getAbsolutePath());
                } else {
                    if (TitleLine == null || TitleLine.trim().length() == 0) {
                        break;
                    }
                    if (TitleLine.split(":").length > 0) {
                        String val = TitleLine.split(":")[0];
                        val = val.replace(" ", "_");
                        Headers.put(val, TitleLine.replace(TitleLine.split(":")[0] + ":", ""));
                    }
                    if (TitleLine.indexOf("Authorization:") == 0) {
                        //  Authorization: Basic dXNlcjoxMjM=
                        String coderead = TitleLine.replaceAll("Authorization: Basic ", "");
                        Headers.put("Author", coderead);
                    }
                }
            }

            //
            // кодировка входных данных
            if (Headers.containsKey("Content-Type") == true) {
                // Content-Type: text/html; charset=windows-1251
                if (Headers.get("Content-Type").toString().split("charset=").length == 2) {
                    Koderovka = Headers.get("Content-Type").toString().split("charset=")[1];
                    Headers.put("Charset", Koderovka);
                    //  Json.put("Charset", Koderovka);
                }
            }
            //  outLog.write("\n--POST--\n");
            if (Headers.containsKey("Content-Length") == true) {
                // Читаем тело пост запроса
                String lengStr = Headers.get("Content-Length").toString();
                // outLog.write("\n--1 " + lengStr + "--\n");
                lengStr = lengStr.replace(" ", "");
                int lengAll = Integer.parseInt(lengStr);
                // outLog.write("\n--2 " + lengAll + "--\n");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // CharBuffer sbPost = CharBuffer.allocate(lengAll);
                int charInt1 = -1;
                while ((charInt1 = isr.read()) > 0) {
                    // outLog.write((char) charInt1);
                    // sbPost.append((char) charInt);
                    baos.write(charInt1);
                    lengAll--;
                    if (lengAll == 0) {
                        break;
                    }
                    if (socket.isConnected() == false) {
                        return;
                    }
                }
                //    String s = baos.toString("Cp1251");
                //    String s = baos.toString();
                PostByte = baos.toByteArray();
                Headers.put("PostBodyText", JSON.encode(baos));
                Headers.put("PostBodyByte", baos.toByteArray());

                //  Json.put("PostBody", java.net.URLDecoder.decode(new String(POST), "UTF-8"));
                //    Content-Type: text/html; charset=windows-1251
                //   PrintWriter pwPost = new PrintWriter(new FileWriter("C:\\!-!\\srvPOST.xml"));
                //   pwPost.write(new String(POST));
                // pwPost.write(s);
                //   pwPost.close();
            }
            // Парсим Cookie если он есть
            if (Headers.containsKey("Cookie") == true) {
                String Cookie = Headers.get("Cookie").toString();
                Cookie = Cookie.substring(1, Cookie.length());// убираем лишний пробел сначала строки
                for (String elem : Cookie.split("; ")) {
                    String[] val = elem.split("=");
                    Headers.put(val[0], val[1]);
                    val[0] = val[0].replace(" ", "_");
                    Headers.put(val[0], val[1]);
                    Headers.put(val[0], val[1]);
                    inParam.put(val[0], val[1]);
                }
            }
            //  PrintWriter pw = new PrintWriter(new FileWriter("C:\\Intel\\srvLogInData.xml"));
            //  pw.write(sb.toString());
            //  pw.close();
            sb.setLength(0);
            //   outLog.close();
        }


        /**
         * метод отправки ответа клиенту
         */
        private void writeResponse() {
            if (socket.isConnected() == false) {
                return;
            }
            PrintStream out = new PrintStream(os);
            System.setOut(out);
            System.setErr(out);


            // Авторизация, если она включена
            if (IsAutorization == true) {
                if (author() == false) {
                    return;
                }
            }
            try {
               sendStream(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
             /*
            //https://www.damonkohler.com/2010/10/mjpeg-streaming-protocol.html
            try {
                System.out.write((
                        "HTTP/1.0 200 OK\r\n" +
                                "Server: YourServerName\r\n" +
                                "Connection: close\r\n" +
                                "Max-Age: 0\r\n" +
                                "Expires: 0\r\n" +
                                "Cache-Control: no-cache, private\r\n" +
                                "Pragma: no-cache\r\n" +
                                "Content-Type: multipart/x-mixed-replace; " +
                                "boundary=--BoundaryString\r\n\r\n").getBytes());
                while (true) {
                     // dataJpegPreviev = jpegProvider.getJpeg();
                    System.out.write((
                                    "--BoundaryString\r\n" +
                                    "Content-type: image/jpg\r\n" +
                                    "Content-Length: " +
                                    MainActivity.dataJpegPreviev.length +
                                    "\r\n\r\n").getBytes());
                    System.out.write(MainActivity.dataJpegPreviev);
                    System.out.write("\r\n\r\n".getBytes());
                    System.out.flush();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            */
            if (1==1){
              return;
            }


            String PathStr = Headers.get("AbsalutZapros").toString();
            //  Headers.put("AbsalutZapros", PathStr);
            File pathPege = new File(PathStr);
            if (pathPege.exists() && !pathPege.isDirectory()) {
                Sys.sendRawFile(pathPege);
            } else {
                CreateCompId();
                try {

                    // Headers.put("Zapros", "WebFileManagerApp.htm");
                    if (sendContentProvidr(Headers.get("Zapros").toString()) == false) {
                        Sys.sendListProvider(context, ".htm");
                        //Sys.sendListProvider(context, "");
                        //  sendInfo();

                    }
                } catch (Exception ex) {
                    System.out.println("Error" + ex.toString());
                }

                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            }
        }


        /**
         * Создание иденификатора подключаемого компьютера  и сохранение его в куках клиенской машины
         */
        public void CreateCompId() {
            //
            // создаем идентификатор компьютера , сохраняем его в Кукисах и перезагружаем страницу
            if (Headers.containsKey("WORCSTATIONID") == false) {
                try {
                    Date currentDate = new Date();
                    Long time = currentDate.getTime();
                    String IDcomp = getMD5(numComp + IPmac + time);
                    String initWORCSTation = ""
                            + "<script>"
                            + "    function setCookie(cname, cvalue, exdays) { var d = new Date(); d.setTime(d.getTime() + (exdays)); var expires = 'expires='+d.toUTCString();   		document.cookie = cname + '=' + cvalue + '; ' + expires;} \n"
                            + "    setCookie('WORCSTATIONID', '" + IDcomp + "', 157680000); "
                            + "    window.location.href=window.location.toString();"
                            + "</script>"; //31536000
                    os.write(("HTTP/1.1 200 OK\r\n").getBytes());
                    os.write(("Content-Type: text/html; ").getBytes());
                    os.write(("Content-Length: " + initWORCSTation.length() + "\r\n").getBytes());
                    os.write(("charset=utf-8\r\n").getBytes());
                    os.write("Connection: close\r\n".getBytes());
                    os.write("Server: HTMLserver\r\n\r\n".getBytes());
                    os.write(initWORCSTation.getBytes());
                    return;
                } catch (Exception ex) {
                    System.err.println("Error create ID comp:" + ex.toString());
                    return;
                }
            }
        }


        /**
         * Найти провайдера по имени, и если он есть, тогда запустить его
         *
         * @param ProvierName
         * @return
         */
        private boolean sendContentProvidr(String ProvierName) {
            boolean isContentOk = false;
            for (PackageInfo pack : context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS)) {
                ProviderInfo[] providers = pack.providers;
                if (providers != null) {
                    for (ProviderInfo provider : providers) {
                        String providerString = provider.authority;
                        if (providerString != null) {
                            String providerLow = providerString.toLowerCase();
                            String zapr = ProvierName.toLowerCase();
                            if (providerLow.equals(zapr)) {
                                ContentResolver cr = context.getContentResolver();
                                Uri CONTACT_URI = Uri.parse("content://" + providerString);

                                Bundle Head = new Bundle();
                                for (String key : Headers.keySet()) {
                                    Head.putString(key, Headers.get(key).toString());
                                }
                                if (Headers.containsKey("PostBodyByte")) {
                                    Head.putByteArray("PostBodyByte", PostByte);
                                }
                                Head.putString("CharSet",CharSet);

                                Bundle callRes = cr.call(CONTACT_URI, providerString, JSON.encode(Headers), Head);
                                if (callRes != null) {
                                    try {
                                       // byte [] res = callRes.getByteArray(providerString);
                                       // if (res != null) {
                                       //     System.out.write(res);
                                       //     System.out.flush();
                                       //     return true;
                                       // }
                                        byte []  res = callRes.getByteArray("return");
                                        if (res != null) {
                                            System.out.write(res);
                                            System.out.flush();
                                            return true;
                                        }
                                        System.out.write(JSON.encode(callRes).getBytes());
                                        System.out.flush();
                                    } catch (IOException e) {
                                        System.err.print("Error:"+e.toString());
                                    }
                                }
                                isContentOk = true;
                                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                            }
                        }
                    }
                }
            }
            return isContentOk;

        }

        private final static String BOUNDARY_STRING = "boundarystring";
        private void sendStream(OutputStream out) throws IOException {

            String header = "HTTP/1.0 200 OK\r\n" +
                    "Connection: close\r\n" +
                    "Server: Android Webcam\r\n" +
                    "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
                    "Pragma: no-cache\r\n" +
                    "Content-Type: multipart/x-mixed-replace;boundary=" + BOUNDARY_STRING + "\r\n" +
                    "\r\n";
            out.write(header.getBytes());
            out.flush();
            out.write(("--" + BOUNDARY_STRING + "\r\n").getBytes());
            while (true) {
                byte[] data = null;

                try {
                    synchronized (MainActivity.output_stream) {
                        MainActivity.output_stream.wait();
                    }
                    data = MainActivity.mJpegData;;
                } catch (InterruptedException e) {
                    // Log.v(TAG, "Fail to get new JPEG image");
                    break;
                }

                String subHeader = "Content-Type: image/jpeg\r\n" +
                        "Content-Length: " + data.length + "\r\n" +
                        "\r\n";
                out.write(subHeader.getBytes());
                out.write(data);
                out.write(("\r\n--" + BOUNDARY_STRING + "\r\n").getBytes());
                out.flush();
            }
        }


        /**
         * Закодировать строку кодировкой MD5
         *
         * @param input
         * @return
         */
        private String getMD5(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] messageDigest = md.digest(input.getBytes());
                BigInteger number = new BigInteger(1, messageDigest);
                String hashtext = number.toString(16);
                // Now we need to zero pad it if you actually want the full 32 chars.
                while (hashtext.length() < 32) {
                    hashtext = "0" + hashtext;
                }
                return hashtext;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        private String getJsonString(Hashtable<String, String> map) {
            String jsonString = "{";
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                jsonString = jsonString + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\",";
            }
            int i = jsonString.lastIndexOf(",");
            jsonString = jsonString.substring(0, i);
            jsonString += "}";
            return jsonString;
        }

        private void sendInfo() {

            try {

                File[] paths = sdcard.listFiles();
                for (File path : paths) {// перебираем список файлов в каталоге
                    if (!path.isDirectory()) {// если фацл
                        // System.out.println("   <a onmousedown=\"OnButtonDownFile(event, this, 'contextMenuId')\"   style=\"color: rgb(0, 100, 0);  cursor: pointer;  \" href=\"http://" + Host + "/" + DirShref + "\"   id=\"" + path.getAbsolutePath() + "\">&nbsp&nbsp&nbsp&nbsp" + path.getName() + "___</a><br>");
                    } else {
                        String DirShref = path.getAbsolutePath();
                        System.out.println("   <a onmousedown=\"OnButtonDownFile(event, this, 'contextMenuId')\"   style=\"color: rgb(0, 100, 0);  cursor: pointer;  \" href=\"" + path.getAbsolutePath().replace(sdcard.getAbsolutePath(), "") + "\"   id=\"" + path.getAbsolutePath() + "\">&nbsp&nbsp&nbsp&nbsp" + path.getName() + "___</a><br>");
                    }
                }

                System.out.write(("<br>").getBytes());
                System.out.write(("<br>").getBytes());
                String ProvierName = Headers.get("Zapros").toString();
                        /*
                        StringBuffer ProvierNameInvert = new StringBuffer();
                        if (ProvierName.indexOf(".") != -1) {
                            String[] element = ProvierName.split(".");
                            for (int ind = element.length; ind > 0; ind--) {
                                ProvierNameInvert.append(element[ind] + ".");
                            }
                            ProvierName = ProvierNameInvert.toString().substring(0, ProvierNameInvert.toString().length() - 1);
                        }
                        */
                System.out.write(("ProvierName - " + ProvierName + "<br>").getBytes());
                System.out.write(("<br>").getBytes());
                Properties p = System.getProperties();
                System.out.write((Headers.toString() + "<br>").getBytes());
                System.out.write(("<br>").getBytes());
                System.out.write(("<br>").getBytes());
                System.out.write(("<br>").getBytes());
                System.out.write(("<br>").getBytes());
                System.out.write(("Java Runtime Environment version: " + p.getProperty("java.version") + "<br>").getBytes());
                System.out.write(("Java Runtime Environment vendor: " + p.getProperty("java.vendor") + "<br>").getBytes());
                System.out.write(("Java vendor URL: " + p.getProperty("java.vendor.url") + "<br>").getBytes());
                System.out.write(("Java installation directory: " + p.getProperty("java.home") + "<br>").getBytes());
                System.out.write(("Java Virtual Machine specification version: " + p.getProperty("java.vm.specification.version") + "<br>").getBytes());
                System.out.write(("Java Virtual Machine specification vendor: " + p.getProperty("java.vm.specification.vendor") + "<br>").getBytes());
                System.out.write(("Java Virtual Machine specification name: " + p.getProperty("java.vm.specification.name") + "<br>").getBytes());
                System.out.write(("Java Virtual Machine implementation version: " + p.getProperty("java.vm.version") + "<br>").getBytes());
                System.out.write(("Java Virtual Machine implementation vendor: " + p.getProperty("java.vm.vendor") + "<br>").getBytes());
                System.out.write(("Java Virtual Machine implementation name: " + p.getProperty("java.vm.name") + "<br>").getBytes());
                System.out.write(("Java Runtime Environment specification version: " + p.getProperty("java.specification.version") + "<br>").getBytes());
                System.out.write(("Java Runtime Environment specification vendor: " + p.getProperty("java.specification.vendor") + "<br>").getBytes());
                System.out.write(("Java Runtime Environment specification name: " + p.getProperty("java.specification.name") + "<br>").getBytes());
                System.out.write(("Java class format version number: " + p.getProperty("java.class.version") + "<br>").getBytes());
                System.out.write(("Java class path: " + p.getProperty("java.class.path") + "<br>").getBytes());
                System.out.write(("List of paths to search when loading libraries: " + p.getProperty("java.library.path") + "<br>").getBytes());
                System.out.write(("Default temp file path: " + p.getProperty("java.io.tmpdir") + "<br>").getBytes());
                System.out.write(("Name of JIT compiler to use: " + p.getProperty("java.compiler") + "<br>").getBytes());
                System.out.write(("Path of extension directory or directories: " + p.getProperty("java.ext.dirs") + "<br>").getBytes());
                System.out.write(("Operating system name: " + p.getProperty("os.name") + "<br>").getBytes());
                System.out.write(("Operating system architecture: " + p.getProperty("os.arch") + "<br>").getBytes());
                System.out.write(("Operating system version: " + p.getProperty("os.version") + "<br>").getBytes());
                System.out.write(("File separator (\"/\" on UNIX): " + p.getProperty("file.separator") + "<br>").getBytes());
                System.out.write(("Path separator (\":\" on UNIX): " + p.getProperty("path.separator") + "<br>").getBytes());
                System.out.write(("Line separator (\"\\n\" on UNIX): " + p.getProperty("line.separator") + "<br>").getBytes());
                System.out.write(("User's account name: " + p.getProperty("user.name") + "<br>").getBytes());
                System.out.write(("User's home directory: " + p.getProperty("user.home") + "<br>").getBytes());
                System.out.write(("User's current working directory: " + p.getProperty("user.dir") + "<br>").getBytes());
            } catch (Exception ex) {
                System.err.println("Error send info:" + ex.toString());
            }
        }


        private boolean author() {
            try {
                if (!Headers.containsKey("Author")) {
                    System.out.write(("HTTP/1.1 401 OK\r\n"
                            + "Content-type: text/html; charset=utf8\r\n"
                            + "WWW-Authenticate: Basic realm=\"Cache\""
                            + "\r\n\r\n"
                            + "<html>\r\n"
                            + "  <head>\r\n"
                            + "      <meta charset='UTF-8'>\r\n"
                            + "  </head>\r\n"
                            + "   <body><h2></h2>\r\n"
                            + "        <p> Error autor</p>\r\n"
                            + "   </body>\r\n"
                            + "</html>\r\n").getBytes());
                    return false;
                }

                String Author = new String(Base64.decode(Headers.get("Author").toString(), Base64.DEFAULT));
                if (Author.split(":").length != 2) {
                    System.out.write(("HTTP/1.1 401 OK\r\n"
                            + "Content-type: text/html; charset=utf8\r\n"
                            + "WWW-Authenticate: Basic realm=\"Cache\""
                            + "\r\n\r\n"
                            + "<html>\r\n"
                            + "  <head>\r\n"
                            + "      <meta charset='UTF-8'>\r\n"
                            + "  </head>\r\n"
                            + "   <body><h2></h2>\r\n"
                            + "        <p> Error autor</p>\r\n"
                            + "   </body>\r\n"
                            + "</html>\r\n").getBytes());
                    return false;
                }
                if ((!Author.split(":")[0].equals(UserName)) || (!Author.split(":")[1].equals(UserPass))) {
                    System.out.write(("HTTP/1.1 401 OK\r\n"
                            + "Content-type: text/html; charset=utf8\r\n"
                            + "WWW-Authenticate: Basic realm=\"Cache\""
                            + "\r\n\r\n"
                            + "<html>\r\n"
                            + "  <head>\r\n"
                            + "      <meta charset='UTF-8'>\r\n"
                            + "  </head>\r\n"
                            + "   <body><h2></h2>\r\n"
                            + "        <p> Error autor</p>\r\n"
                            + "   </body>\r\n"
                            + "</html>\r\n").getBytes());
                    return false;
                }

            } catch (Exception ex) {
                System.err.println("Error author:" + ex.toString());
            }
            return true;
        }
    }


    /*
    public void handleConnection(Socket socket, byte[] data) throws Exception {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write((
                "HTTP/1.0 200 OK\r\n" +
                        "Server: YourServerName\r\n" +
                        "Connection: close\r\n" +
                        "Max-Age: 0\r\n" +
                        "Expires: 0\r\n" +
                        "Cache-Control: no-cache, private\r\n" +
                        "Pragma: no-cache\r\n" +
                        "Content-Type: multipart/x-mixed-replace; " +
                        "boundary=--BoundaryString\r\n\r\n").getBytes());
        while (true) {
            data = jpegProvider.getJpeg();
            outputStream.write((
                    "--BoundaryString\r\n" +
                            "Content-type: image/jpg\r\n". +
                            "Content-Length: " +
                            data.length +
                            "\r\n\r\n").getBytes());
            outputStream.write(data);
            outputStream.write("\r\n\r\n".getBytes());
            outputStream.flush();
        }
    }
    */
}