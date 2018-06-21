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
import android.util.Log;
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

        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (dcim == null) {
            return;
        }
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

        final File[] pics = dcim.listFiles();
        if (pics == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int barState = 0;
                    List<File> pituresList = new ArrayList<File>();

                        for (File pic : pics) {
                            if (pic.isDirectory()) {
                                searchPicInFolder(pic, pituresList);
                            } else if(checkExtension(pic.toString())) { //check if file
                                pituresList.add(pic);
                            }
                        }

                    files = pituresList;
                    for (File file : files) {
                        TcpClient tcpClient = new TcpClient(file);
                        tcpClient.startCommunication();
                        barState = barState + 100 / files.size();
                        builder.setProgress(100, barState, false);
                        NM.notify(NI, builder.build());

                    }
                    //finish
                    builder.setProgress(0, 0, false);
                    builder.setContentTitle("Finished transfer!");
                    builder.setContentText("Finished transfer!");
                    NM.notify(NI, builder.build());
                } catch (Exception e) {
                    Log.e("transfer", "Error: ", e);
                }
            }
        }).start();
    }

    /**
     *  searchPicInFolder() searches for pictures, recursively if needed,
     *  in a given folder.
     * @param folder folder to search
     * @param list list to add the pictures we found
     */
    public void searchPicInFolder(File folder, List<File> list) {
        File[] pics = folder.listFiles();
        for (File pic : pics) {
            if (pic.isDirectory()) {
                searchPicInFolder(pic, list);
            } else if(checkExtension(pic.toString())) {
                list.add(pic);
            }
        }
    }

    /**
     * checkExtension() checks the extension of a file
     * to see if its a pic.
     * @param string - string to check
     * @return true if it a pic
     */
    private boolean checkExtension(String string) {
        return  (string.contains(".jpg") || string.contains(".png") || string.contains(".gif") || string.contains(".bmp"));
    }
}
