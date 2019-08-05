package com.ikpyt.wifiapsta5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyTimeReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.ikpyt.wifiapsta5.alarm";

    // периодически срабатывает по тревоге (запускает службу для запуска задачи)
    @Override
    public void onReceive (Context context, Intent intent){
        Intent i = new Intent(context, MyIntentService.class);
        //i.putExtra("foo", "bar");
        i.putExtra("type", 3).putExtra("time", 7).putExtra("task", "Разбудить кота");
        context.startService(i);
    }
}
