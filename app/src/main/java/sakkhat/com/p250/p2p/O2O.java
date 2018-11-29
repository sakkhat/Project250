package sakkhat.com.p250.p2p;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Time;

/**
 * Created by Rafiul Islam on 29-Nov-18.
 */

public class O2O {

    private static final int PORT = 15008;
    public static final int SOCKET_ESTABLISHED = 20168310;
    public static final int FILE_NAME = 20168311;
    public static final int FILE_SIZE = 20168312;
    public static final int FILE_SENT_CONFIRM = 20168313;
    public static final int FILE_RECEIVED_CONFIRM = 20168314;

    public static final String PATH = Environment.getExternalStoragePublicDirectory(Environment
            .DIRECTORY_DOCUMENTS).getPath();

    public static class Server extends Thread{
        private static final String TAG = "o2o_server_thread";
        private Handler handler;
        public Server(Handler handler){
            this.handler = handler;

            this.start();
        }
        @Override
        public void run(){
            try{
                ServerSocket ss = new ServerSocket(PORT);
                Socket socket = ss.accept();
                handler.obtainMessage(SOCKET_ESTABLISHED, socket).sendToTarget();
            }catch (IOException ex){
                Log.e(TAG, ex.toString());
            }
        }
    }

    public static class Client extends Thread{
        private static final String TAG = "o2o_client_thread";

        private Handler handler;
        private InetAddress host;
        public Client(Handler handler, InetAddress host){
            this.handler = handler;
            this.host = host;

            this.start();
        }

        @Override
        public void run(){
            try {
                Socket socket = new Socket(host, PORT);
                handler.obtainMessage(SOCKET_ESTABLISHED, socket).sendToTarget();;
            } catch (IOException e){
                Log.e(TAG, e.toString());
            }
        }
    }

    public static class Receiver extends Thread{
        private static final String TAG = "file_receiver";
        private Socket socket;
        private Handler handler;

        public Receiver(Handler handler, Socket socket){
            this.handler = handler;
            this.socket = socket;

            Log.d(TAG, "constructed");

            this.start();
        }

        @Override
        public void run(){
            try {
                BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                DataInputStream dis =  new DataInputStream(socket.getInputStream());

                while (socket != null || !socket.isClosed()){
                    String fileName = dis.readUTF();
                    long fileSize = dis.readLong();

                    File file = new File(PATH+"/"+fileName);
                    long loaded = 0;

                    FileOutputStream fos = new FileOutputStream(file);

                    int readLength;
                    byte[] kb64 = new byte[64*1024];
                    while((readLength = bis.read(kb64)) > 0){
                        fos.write(kb64, 0, readLength);
                        loaded += readLength;

                        if(readLength == 0 || loaded == fileSize){
                            fos.flush();
                            fos.close();
                            handler.obtainMessage(FILE_RECEIVED_CONFIRM).sendToTarget();
                            break;
                        }
                    }
                }
            } catch (IOException ex){
                Log.e(TAG, ex.toString());
            }
        }
    }

    public static class Sender extends Thread{

        private static final String TAG = "o2o_sender_thread";

        private Handler handler;
        private Socket socket;
        private File file;

        public Sender(Handler handler, Socket socket, File file){
            this.handler = handler;
            this.socket = socket;
            this.file = file;

            this.start();
        }

        @Override
        public void run(){
            try{
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

                dos.writeUTF(file.getName());
                dos.writeLong(file.length());

                FileInputStream fis = new FileInputStream(file);
                long available = file.length();
                byte[] kb64 = new byte[1020*64];
                int readLength;

                while((readLength = fis.read(kb64)) != -1 || available >0){
                    available -= readLength;
                    bos.write(kb64);
                }

                fis.close();
                handler.obtainMessage(FILE_SENT_CONFIRM).sendToTarget();

            } catch (IOException ex){
                Log.e(TAG, ex.toString());
            }
        }
    }
}
