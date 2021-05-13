package com.ikpyt.wifiapsta5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyScanReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 54321;
    public static final String ACTION = "com.ikpyt.wifiapsta5.alarm";

    // периодически срабатывает по тревоге (запускает службу для запуска задачи)
    @Override
    public void onReceive (Context context, Intent intent){
        Intent i = new Intent(context, MyIntentService.class);
        i.putExtra("type", MyIntentService.SCAN)
         .putExtra("time", 5)
         .putExtra("task", "Сканить кота");
        context.startService(i);
    }
}
