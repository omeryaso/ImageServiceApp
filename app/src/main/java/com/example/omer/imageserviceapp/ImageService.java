package com.example.omer.imageserviceapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageService extends Service {

    //members
    IntentFilter intentFilter= new IntentFilter();
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    //get the different network states
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        startTransfer(context); // Starting the Transfer
                    }
                }
            }
        }
    };
    List<File> files;

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        Toast.makeText(this, "Service starting...", Toast.LENGTH_LONG).show();
        // Registers the receiver so that your service will listen for
        // broadcasts
        this.registerReceiver(this.broadcastReceiver, intentFilter);
        return START_STICKY;
    }

    /**
     * onBind - No need to implement so returns NULL.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * onCreate() will be called when service creation
     * and decides what will happen on create.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        this.intentFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        this.intentFilter.addAction("android.net.wifi.STATE_CHANGE");
    }

    /**
     * onDestroy() will be called when service distraction has started
     * and will define the proper actions.
     */
    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service ending...", Toast.LENGTH_LONG).show();
        this.unregisterReceiver(this.broadcastReceiver);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startTransfer(Context context) {
        //set notification progress bar
        final int NI = 1;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
        final NotificationManager NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default", "Progress bar", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Progress bar for image transfer");
        NM.createNotificationChannel(channel);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("Passing images....");
        builder.setContentText("Passing in progress...");
        //start the transfer
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int barState = 0;
                    updatePicsFilesList();
                    for (File file : files) {
                        //crete new tcp client to talk with server
                        TcpClient tcpClient = new TcpClient(file);
                        //talk to image service and send him the photo
                        tcpClient.startCommunication();
                        //update the progress bar
                        barState = barState + 100 / files.size();
                        builder.setProgress(100, barState, false);
                        NM.notify(NI, builder.build());

                    }
                    //finish
                    builder.setProgress(0, 0, false);
                    builder.setContentTitle("Finished transfer!");
                    builder.setContentText("Finished transfer!");
                    NM.notify(NI, builder.build());
                } catch (Exception ex) {

                }
            }
        }).start();
    }


    public void getOneFile(File dir, List<File> picsFilesList) {
        File[] dirFiles = dir.listFiles();
        int len = dirFiles.length;
        for (int i=0; i <len; i++) {
            if (dirFiles[i].isDirectory()) {
                getOneFile(dirFiles[i], picsFilesList);
            } else if(cheackExtention(dirFiles[i].toString())) {
                picsFilesList.add(dirFiles[i]);
            }
        }
    }


    public void updatePicsFilesList() {
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        //get the dirs
        File[] fileOrDir = dcim.listFiles();
        List<File> picsFilesList = new ArrayList<File>();
        int len =fileOrDir.length;
        if (fileOrDir != null) {
            for (int i=0; i <len; i++) {
                //check if dir
                if (fileOrDir[i].isDirectory()) {
                    getOneFile(fileOrDir[i], picsFilesList);
                } else if(cheackExtention(fileOrDir[i].toString())) { //check if file
                    picsFilesList.add(fileOrDir[i]);
                }
            }
        }
        //update the member
        files = picsFilesList;
    }

    private boolean cheackExtention(String string) {
        return  (string.contains(".jpg") || string.contains(".png") || string.contains(".gif") || string.contains(".bmp"));
    }
}
