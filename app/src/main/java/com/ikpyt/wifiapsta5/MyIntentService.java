package com.ikpyt.wifiapsta5;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import java.util.List;
import android.widget.Toast;

import static android.os.SystemClock.elapsedRealtime;
/*
RedmiTer
xtyhfde467



*/
/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class MyIntentService extends IntentService {
    private final String TAG = "MyLogsS";
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    //public static final String ACTION_FOO = "com.ikpyt.wifiapsta5.action.FOO";
    //public static final String ACTION_BAZ = "com.ikpyt.wifiapsta5.action.BAZ";
    public static final String ACTION_MYINTENTSERVICE = "com.ikpyt.wifiapsta5.RESPONSE";
    public static final String EXTRA_KEY_OUT = "EXTRA_OUT";
    public static final String EXTRA_KEY_TIME = "EXTRA_TIME";
    String extraOut = "Кота накормили, погладили и поиграли с ним\n"; // строка для результата
    // TODO: Rename parameters
    //public static final String EXTRA_PARAM1 = "com.ikpyt.wifiapsta5.extra.PARAM1";
    //public static final String EXTRA_PARAM2 = "com.ikpyt.wifiapsta5.extra.PARAM2";
    public static final String ACTION_UPDATE = "com.ikpyt.wifiapsta5.UPDATE";
    public static final String EXTRA_KEY_UPDATE = "EXTRA_UPDATE";
    // уведомления
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    // для кнопок
    private boolean mIsSuccess;
    private boolean mIsStopped;
    // для WiFi
    WifiManager wifiManager;
    boolean success = false;

    List<ScanResult> wifiScanList;

    public MyIntentService()
    {
        super("MyIntentService");
        Log.d(TAG, "MyIntentService");

        mIsSuccess = false;
        mIsStopped = false;
    }
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiScanList = wifiManager.getScanResults();
        Log.d(TAG, "wifiScanList.size()="+wifiScanList.size());
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        String notice;
        mIsStopped = true;

        if (mIsSuccess) { notice = "onDestroy with success";}
        else            { notice = "onDestroy WITHOUT success!"; }

        Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_LONG).show();
        super.onDestroy();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        String task = intent.getStringExtra("task");
        int type = intent.getIntExtra("type",0);
        //long firstMillis = System.currentTimeMillis();
        Log.d(TAG, "onHandleIntent("+task+")");
        //boolean stat = wifiManager.startScan();
        //Log.d(TAG, "Start scan..."+stat);

        switch(type)
        {
            case 1: // BOOT
                // снять блокировку пробуждения, чтобы после завершения сервиса система могла заснуть
                WakefulBroadcastReceiver.completeWakefulIntent(intent);
                break;
            case 2: //BUTTON
                success = wifiManager.startScan();
                break;
            case 3: // TIME
                success = wifiManager.startScan();
                break;
            case 4: // SCAN
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
                StringBuilder info = new StringBuilder();
                info.append("\n");
                for (int i = 0; i < wifiScanList.size(); i++) {
                    ScanResult ap = wifiScanList.get(i);
                    //String info = (ap.toString());
                    info.append("\n").append(ap.SSID).append(" ").append(ap.BSSID).append(" ").append(+ap.level);
                }
                Log.d(TAG, info + "\n\n");

                break;
            default:
                break;

        }

        //success = wifiManager.startScan();
        /*if (!success) {

            // scan failure handling
            scanFailure();
        }*/

        for (int i = 0; i <= 5; i++) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(mIsStopped){
                break;
            }
/*
            // посылаем промежуточные данные
            Intent updateIntent = new Intent();
            updateIntent.setAction(ACTION_UPDATE);
            updateIntent.addCategory(Intent.CATEGORY_DEFAULT);
            updateIntent.putExtra(EXTRA_KEY_UPDATE, i);
            sendBroadcast(updateIntent);
*/
            int note = ((i==0)||(i==9))? Notification.DEFAULT_SOUND /*| Notification.DEFAULT_VIBRATE*/ : Notification.BADGE_ICON_NONE ;
//            mIsSuccess = true;
            // формируем уведомление
            String notificationText = String.valueOf( (100 * i / 10))+ " %";
            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Progress "+wifiScanList.size()+" :"+task)
                    .setContentText(notificationText)
                    .setTicker("Notification!")
                    .setWhen(System.currentTimeMillis())
                    //.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE )
                    .setDefaults(note)
                    .setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
        mIsSuccess = true;

        // возвращаем результат
        Intent responseIntent = new Intent();
        responseIntent.setAction(ACTION_MYINTENTSERVICE);
        responseIntent.addCategory(Intent.CATEGORY_DEFAULT);
        responseIntent.putExtra(EXTRA_KEY_OUT, extraOut);
        responseIntent.putExtra(EXTRA_KEY_TIME, elapsedRealtime());
        sendBroadcast(responseIntent);
    }
    private void scanSuccess() {
        wifiScanList = wifiManager.getScanResults();
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        wifiScanList = wifiManager.getScanResults();
    }
}
