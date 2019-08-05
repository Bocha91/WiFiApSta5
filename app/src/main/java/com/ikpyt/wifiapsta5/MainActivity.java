package com.ikpyt.wifiapsta5;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import static android.os.SystemClock.elapsedRealtime;
//import java.util.List;
//import android.net.wifi.ScanResult;
//import android.net.wifi.WifiManager;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/*
TYPT_BOOT   = 1;
TYPT_BUTTON = 2;
TYPT_TIME   = 3;
TYPT_SCAN   = 4;
*/
public class MainActivity extends AppCompatActivity {
    private final String TAG = "MyLogs";
    boolean knop = false;
    long old_timest = 0;
    Button button;
    TextView mInfoTextView;

    private Intent mMyServiceIntent;
    private MyBroadcastReceiver mMyBroadcastReceiver;
    private UpdateBroadcastReceiver mUpdateBroadcastReceiver;

    public static final boolean IS_PRE_M_ANDROID = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1000;
    //Context context;
    //WifiManager wifiManager;
    //IntentFilter intFilt;
    //BroadcastReceiver receiver;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        knop = false;
        button = (Button) findViewById(R.id.buttonScan);
        mInfoTextView = (TextView) findViewById(R.id.textView);
        mInfoTextView.setText("");

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (knop == false) {
                    button.setText(R.string.stop_scan);
                    knop = true;
                    // Запускаем свой IntentService
                    mMyServiceIntent = new Intent(MainActivity.this, MyIntentService.class);
                    //                                      time и task это имена передаваемых параметров, их значения в поле value
                    startService(mMyServiceIntent.putExtra("type", 2).putExtra("time", 3).putExtra("task", "Погладить кота"));

                } else {
                    button.setText(R.string.start_scan);
                    knop = false;
                    if (mMyServiceIntent != null) {
                        stopService(mMyServiceIntent);
                        mMyServiceIntent = null;
                    }
                }
                Log.i(TAG, "Сервис запущен: " + knop);
            }
        });

        mMyBroadcastReceiver = new MyBroadcastReceiver();
        mUpdateBroadcastReceiver = new UpdateBroadcastReceiver();

        // регистрируем BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(
                MyIntentService.ACTION_MYINTENTSERVICE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mMyBroadcastReceiver, intentFilter);

        // Регистрируем второй приёмник Update
        IntentFilter updateIntentFilter = new IntentFilter(
                MyIntentService.ACTION_UPDATE);
        updateIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mUpdateBroadcastReceiver, updateIntentFilter);

        // старт будильника
        scheduleAlarm();


    }

    @Override protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        // The activity is about to become visible.
    }

    @Override protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        //registerReceiver( wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) );

        if (!this.isFineOrCoarseLocationPermissionGranted()) {
            requestCoarseLocationPermission();
        } else if (isFineOrCoarseLocationPermissionGranted() || IS_PRE_M_ANDROID) {
            startWifiAccessPointsSubscription();
        }

        //startWifiSignalLevelSubscription();
        //startSupplicantSubscription();
        //startWifiInfoSubscription();
        //startWifiStateSubscription();
/*
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                boolean stat = wifiManager.startScan();
                Log.d(TAG, "Start scan..."+stat);
            }

        });
*/
    }

    @Override protected void onPause() {
        Log.d(TAG, "onPause");
        //context.unregisterReceiver(receiver);
        super.onPause();
    }

    @Override protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }

    @Override protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        unregisterReceiver(mMyBroadcastReceiver);
        unregisterReceiver(mUpdateBroadcastReceiver);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra(MyIntentService.EXTRA_KEY_OUT);
            Long timest = intent.getLongExtra(MyIntentService.EXTRA_KEY_TIME, 0);
            mInfoTextView.setText(mInfoTextView.getText() + result + "\ntime=" + (timest - old_timest) / 1000 + "\n");
            if (mMyServiceIntent != null) {
                stopService(mMyServiceIntent);
                mMyServiceIntent = null;
            }
            old_timest = timest;
            button.setText(R.string.start_scan);
            knop = false;
        }
    }

    public class UpdateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int update = intent.getIntExtra(MyIntentService.EXTRA_KEY_UPDATE, 0);
            mInfoTextView.setText(mInfoTextView.getText() + "\nUpdate=" + update);
        }
    }

    //----------------------------------------------------------------------------------------------
    // настраиваем повторяющийся будильник на каждые 15 секунд который будеи запускать наш сервис MyIntentService
    public void scheduleAlarm() {
        // Создаем намерение, которое будет выполнять AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), MyTimeReceiver.class);
        // Создать PendingIntent, который будет срабатывать при срабатывании будильника
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyTimeReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // С этого момента настраиваем периодическую сигнализацию каждые 15 секунд
        long firstMillis = System.currentTimeMillis(); // будильник установлен сразу
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // Первый параметр - это тип: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Интервал может быть INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        //alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_HALF_HOUR, pIntent);
        //alarm.setExact(AlarmManager.RTC_WAKEUP, firstMillis+15000, pIntent); // будит один раз через 15 секунд
        //alarm.setRepeating (AlarmManager.RTC_WAKEUP, firstMillis,15000, pIntent); // разбудит сразу и потом каждые 15 секунд, реально интервалы от балды
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsedRealtime(), 30000, pIntent);
    }

    // После установки будильника, если мы хотим отменить будильник, мы можем сделать это с помощью:
    public void cancelAlarm(View v) {
        Intent intent = new Intent(getApplicationContext(), MyTimeReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyTimeReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    //----------------------------------------------------------------------------------------------
    private void startWifiAccessPointsSubscription() {

        boolean fineLocationPermissionNotGranted = ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED;
        boolean coarseLocationPermissionNotGranted = ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED;

        if (fineLocationPermissionNotGranted && coarseLocationPermissionNotGranted) {
            return;
        }

        if (!AccessRequester.isLocationEnabled(this)) {
            AccessRequester.requestLocationAccess(this);
            return;
        }
/*
        wifiSubscription = ReactiveWifi.observeWifiAccessPoints(getApplicationContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::displayAccessPoints);
*/
/*
        context = getApplicationContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        intFilt = new IntentFilter();
        intFilt.addAction(WifiManager.RSSI_CHANGED_ACTION); // you can keep this filter if you want to get fresh results when singnal stregth of the APs was changed
        intFilt.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wifiManager.startScan(); // start scan again to get fresh results ASAP
                //wifiManager.getScanResults();
                List<ScanResult> wifiScanList = wifiManager.getScanResults();
                Log.d(TAG, "onReceive" + wifiScanList.toString() + " size=" + wifiScanList.size());
                //txtWifiInfo.setText("");
                for (int i = 0; i < wifiScanList.size(); i++) {
                    String info = ((wifiScanList.get(i)).toString());
                    mInfoTextView.append(info + "\n\n");
                    Log.d(TAG, info + "\n\n");

                }
            }
        };
        context.registerReceiver(receiver, intFilt);
*/
    }


    private void requestCoarseLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        }
    }

    private boolean isFineOrCoarseLocationPermissionGranted() {
        boolean isAndroidMOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        boolean isFineLocationPermissionGranted = isGranted(ACCESS_FINE_LOCATION);
        boolean isCoarseLocationPermissionGranted = isGranted(ACCESS_COARSE_LOCATION);

        return isAndroidMOrHigher && (isFineLocationPermissionGranted
                || isCoarseLocationPermissionGranted);

    }
    private boolean isGranted(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED;
    }

}