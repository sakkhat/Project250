package sakkhat.com.p250.p2p;

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

import sakkhat.com.p250.structure.IOItem;

/**
 * Created by Rafiul Islam on 29-Nov-18.
 */

public class O2O {

    private static final int PORT = 8050;
    public static final int SOCKET_ESTABLISHED = 11;
    public static final int FILE_SENT_CONFIRM = 12;
    public static final int FILE_RECEIVED_CONFIRM = 13;
    public static final int FILE_RECEIVE_REQUEST = 14;
    public static final int FILE_RECEIVE_PROGRESS = 15;
    public static final int FILE_SENT_PROGRESS = 16;
    public static final int SOCKET_CLOSED = 18;

    public static class Server extends Thread{
        private static final String TAG = "p2p_server_thread";

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
        private static final String TAG = "p2p_client_thread";

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
        private static final String TAG = "p2p_file_receiver";
        private Socket socket;
        private Handler handler;
        private String path;

        public Receiver(Handler handler, Socket socket, String path){
            this.handler = handler;
            this.socket = socket;
            this.path = path;

            Log.d(TAG, "receiver constructed");

            this.start();
        }

        @Override
        public void run(){
            try {
                DataInputStream dis =  new DataInputStream(socket.getInputStream());
                BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());

                File dir = new File(path);
                if(!dir.exists()){
                    dir.mkdir();
                }

                while (socket != null || !socket.isClosed()){
                    String fileName = dis.readUTF();
                    long fileSize = dis.readLong();

                    handler.obtainMessage(FILE_RECEIVE_REQUEST,
                            new IOItem(fileName, fileSize, true)).sendToTarget();

                    long load = 0;
                    File file = new File(dir+"/"+fileName);
                    FileOutputStream fos = new FileOutputStream(file);

                    Log.e(TAG, "file size: "+Long.toString(fileSize));
                    int readLength;
                    byte[] kb64 = new byte[64*1024];

                    while((readLength = bis.read(kb64, 0, kb64.length)) != 0){
                        load += readLength;
                        fos.write(kb64, 0, readLength);
                        if(load == fileSize){
                            break;
                        }
                    }

                    fos.flush();
                    fos.close();
                    handler.obtainMessage(FILE_RECEIVED_CONFIRM).sendToTarget();
                }
                handler.obtainMessage(SOCKET_CLOSED).sendToTarget();
            } catch (IOException ex){
                Log.e(TAG, ex.toString());
            }
        }
    }

    public static class Sender extends Thread{

        private static final String TAG = "p2p_sender_thread";

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

                while((readLength = fis.read(kb64, 0, kb64.length)) > 0){
                    available -= readLength;
                    bos.write(kb64, 0, readLength);
                    if(available <= 0){
                        bos.flush();
                        fis.close();
                        break;
                    }
                }
                handler.obtainMessage(FILE_SENT_CONFIRM).sendToTarget();

            } catch (IOException ex){
                Log.e(TAG, ex.toString());
            }
        }
    }
}
