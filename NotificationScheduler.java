package com.partnersdiary.app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationScheduler {
    public static final String CHANNEL_ID = "partners_diary_alerts";
    private final Context context;
    private final AlarmManager alarmManager;

    public NotificationScheduler(Context c) {
        context = c.getApplicationContext();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void createChannelsAndDailyAlarms() {
        createChannel();
        scheduleDaily(9, 0, 900001, "Morning follow-up reminder");
        scheduleDaily(13, 0, 900002, "Afternoon follow-up reminder");
        scheduleDaily(18, 0, 900003, "Evening follow-up reminder");
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Partners Diary Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Appointment and follow-up reminders");
            ch.enableVibration(true);
            ch.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(ch);
        }
    }

    private void scheduleDaily(int hour, int minute, int requestCode, String label) {
        Intent i = new Intent(context, AlarmReceiver.class);
        i.putExtra("type", "daily");
        i.putExtra("title", "Partners Diary");
        i.putExtra("body", label + " — check today's follow-ups.");
        PendingIntent pi = PendingIntent.getBroadcast(
                context, requestCode, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if (c.getTimeInMillis() <= System.currentTimeMillis()) c.add(Calendar.DAY_OF_YEAR, 1);

        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);
        }
    }

    public void scheduleAppointment(String id, String name, String date, String time) {
        if (id == null || id.isEmpty()) return;
        int requestCode = stableId(id);
        Intent i = new Intent(context, AlarmReceiver.class);
        i.putExtra("type", "appointment");
        i.putExtra("title", "Partners Diary — Appointment");
        i.putExtra("body", (name == null || name.isEmpty() ? "Appointment" : name) + " appointment time has been reached.");
        PendingIntent pi = PendingIntent.getBroadcast(
                context, requestCode, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (date == null || date.isEmpty() || time == null || time.isEmpty()) {
            alarmManager.cancel(pi);
            context.getSharedPreferences("appointments", Context.MODE_PRIVATE).edit().remove(id).apply();
            return;
        }

        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).parse(date + "T" + time);
            if (d == null || d.getTime() <= System.currentTimeMillis()) {
                alarmManager.cancel(pi);
                return;
            }
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, d.getTime(), pi);
            context.getSharedPreferences("appointments", Context.MODE_PRIVATE)
                    .edit().putString(id, name + "|" + date + "|" + time).apply();
        } catch (ParseException ignored) {}
    }

    public void rescheduleStoredAppointments() {
        for (String id : context.getSharedPreferences("appointments", Context.MODE_PRIVATE).getAll().keySet()) {
            String v = context.getSharedPreferences("appointments", Context.MODE_PRIVATE).getString(id, "");
            if (v == null) continue;
            String[] p = v.split("\\|", -1);
            if (p.length >= 3) scheduleAppointment(id, p[0], p[1], p[2]);
        }
    }

    private int stableId(String s) {
        int h = 7;
        for (int i=0;i<s.length();i++) h = 31*h + s.charAt(i);
        return Math.abs(h % 1000000) + 100;
    }
}
