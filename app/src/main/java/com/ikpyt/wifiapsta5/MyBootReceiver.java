package com.ikpyt.wifiapsta5;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;


// WakefulBroadcastReceiver гарантирует, что устройство не переходит в спящий режим во время запуска службы
public class MyBootReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Запускаем указанную службу при получении этого сообщения
        Intent i = new Intent(context, MyIntentService.class);
        i.putExtra("type", 1).putExtra("time", 0).putExtra("task", "Загрузим кота");
        startWakefulService(context, i);
    }
}