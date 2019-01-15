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
import java.net.Socket;

import sakkhat.com.p250.structure.IOItem;


/**
 * Created by Rafiul Islam on 15-Jan-19.
 */

public class O2OPC {

    public static final int SOCKET_CONNECTED = 11;
    public static final int SOCKET_ERROR = 12;
    public static final int FILE_RECEIVED = 13;
    public static final int FILE_SENT = 14;
    public static final int COMMANDED = 15;
    public static final int FILE_RECEIVE_REQUEST = 16;

    private static final short RECEIVE_TYPE_FILE = 1;
    private static final short RECEIVE_TYPE_COMMAND = 2;

    // mobile commands
    public static final short RING_MODE = 21;
    public static final short SILENT_MODE = 22;
    public static final short VIBRATE_MODE = 23;

    // pc commands
    public static final short PC_SHUTDOWN = 51;

    private static final String PATH = Environment.getExternalStorageDirectory()+"/p2p";

    public static class RequestToPC extends Thread{
        private static final String TAG = "o2o_pc_request";

        private String address;
        private int port;
        private Handler handler;

        public RequestToPC(String address, int port, Handler handler){
            this.address = address;
            this.port = port;
            this.handler = handler;
            this.setName(TAG);

            this.start();
        }

        @Override
        public void run(){
            try{
                Socket socket = new Socket(address, port);
                Log.d(TAG, "socket connected: "+socket.getRemoteSocketAddress().toString());
                handler.obtainMessage(SOCKET_CONNECTED, socket).sendToTarget();
            } catch (IOException ex){
                Log.e(TAG, ex.toString());
                handler.obtainMessage(SOCKET_ERROR).sendToTarget();
            }
        }
    }

    public static class Receiver extends Thread {
        private static final String TAG = "pc_share_receiver";

        private Socket socket;
        private Handler handler;

        public Receiver(Socket socket, Handler handler){
            this.socket = socket;
            this.handler = handler;
            this.setName(TAG);

            this.start();
        }

        @Override
        public void run(){
            try{
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());

                short type, command;
                String fileName; long len, load; int readLen;
                while(socket != null || !socket.isClosed()){
                    type = dis.readShort();

                    if(type == RECEIVE_TYPE_FILE){
                        load = 0; readLen = 0;
                        fileName =  dis.readUTF();
                        len = dis.readLong();

                        IOItem ioItem = new IOItem(fileName, len, true);
                        handler.obtainMessage(FILE_RECEIVE_REQUEST, ioItem).sendToTarget();

                        byte[] rawData = new byte[64*1024];
                        File dir = new File(PATH);
                        if(!dir.exists()){
                            dir.mkdir();
                        }
                        File file = new File(dir+"/"+fileName);
                        FileOutputStream fos = new FileOutputStream(file);
                        while((readLen = bis.read(rawData, 0, rawData.length))!= -1){
                            load += readLen;
                            fos.write(rawData, 0, readLen);
                            if(load ==len){
                                handler.obtainMessage(FILE_RECEIVED).sendToTarget();
                                break;
                            }
                        }
                        fos.flush();
                        fos.close();
                    }
                    else if(type == RECEIVE_TYPE_COMMAND){
                        command = dis.readShort();
                        handler.obtainMessage(command).sendToTarget();
                        Log.d(TAG, "command");
                    }
                }
            } catch (IOException ex){
                Log.e(TAG, ex.toString());
                handler.obtainMessage(SOCKET_ERROR).sendToTarget();
            }

        }
    }

    public static class Sender extends Thread {

        private static final String TAG = "pc_share_sender";
        private Socket socket;
        private Handler handler;
        private File file = null;
        private short command = -1;

        public Sender(Socket socket, Handler handler, File file){
            this.socket = socket;
            this.handler = handler;
            this.file = file;
            this.setName(TAG);

            this.start();
        }

        public Sender(Socket socket, Handler handler, short command){
            this.socket = socket;
            this.handler = handler;
            this.command = command;
            this.setName(TAG);

            this.start();
        }

        @Override
        public void run(){
            if(file != null){
                // send file
                try{
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

                    // send the protocol
                    dos.writeShort(RECEIVE_TYPE_FILE);
                    dos.writeUTF(file.getName());
                    dos.writeLong(file.length());

                    FileInputStream fis = new FileInputStream(file);
                    byte[] kb64 = new byte[1020*64];
                    int readLength; long available = file.length();

                    while((readLength = fis.read(kb64, 0, kb64.length)) > 0){
                        available -= readLength;
                        bos.write(kb64, 0, readLength);
                        if(available <= 0){
                            bos.flush();
                            fis.close();
                            break;
                        }
                    }
                    handler.obtainMessage(FILE_SENT).sendToTarget();

                } catch (IOException ex){
                    Log.e(TAG, ex.toString());
                    handler.obtainMessage(SOCKET_ERROR).sendToTarget();
                }
            }
            else if(command != -1){
                try{
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    // send the prototype
                    dos.writeShort(RECEIVE_TYPE_COMMAND);
                    // send the file
                    dos.writeShort(command);

                    handler.obtainMessage(COMMANDED).sendToTarget();
                } catch (IOException ex){

                }
            }
        }
    }
}


