package com.danlls.daniel.todule_android.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.danlls.daniel.todule_android.R;
import com.danlls.daniel.todule_android.provider.ToduleDBContract;
import com.danlls.daniel.todule_android.utilities.DateTimeUtils;
import com.danlls.daniel.todule_android.utilities.NotificationHelper;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by danieL on 10/10/2017.
 */

public class NotificationReceiver extends BroadcastReceiver {

    String CHANNEL_ID = "todule_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {

                if (action.equals("android.intent.action.BOOT_COMPLETED") || action.equals("android.intent.action.REBOOT")) {
                    Log.d("BroadCast Received", "ON BOOT COMPLETE");
                    String select = ToduleDBContract.TodoNotification.COLUMN_NAME_REMINDER_CANCELED + " = ?";
                    String[] selectionArgs = {String.valueOf(ToduleDBContract.TodoNotification.REMINDER_NOT_CANCELED)};
                    Cursor cr = context.getContentResolver().query(ToduleDBContract.TodoNotification.CONTENT_URI, null, select, selectionArgs, null);
                    try {
                        while (cr.moveToNext()) {
                            long toduleId = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoNotification.COLUMN_NAME_TODULE_ID));
                            int reminderTime = cr.getInt(cr.getColumnIndexOrThrow(ToduleDBContract.TodoNotification.COLUMN_NAME_REMINDER_TIME));
                            Uri toduleUri = ContentUris.withAppendedId(ToduleDBContract.TodoNotification.CONTENT_ID_URI_BASE, toduleId);
                            NotificationHelper.setReminder(context, toduleUri, reminderTime);
                        }
                    } finally {
                        cr.close();
                    }
                } else if (action.equals("com.danlls.daniel.todule_android.REMINDER_NOTIFICATION")) {
                    long entryId = intent.getLongExtra("todule_id", -1L);

                    // Intent to redirect user to app when notification is clicked
                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    notificationIntent.putExtra("todule_id", entryId);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    PendingIntent resultPendingIntent =
                            PendingIntent.getActivity(
                                    context,
                                    0,
                                    notificationIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );

                    // Intent to delete reminder after notification is canceled
                    Intent notificationDeleteIntent = new Intent(context, getClass());
                    notificationDeleteIntent.setAction("com.danlls.daniel.todule_android.DELETE_REMINDER");
                    notificationDeleteIntent.putExtra("todule_id", entryId);

                    PendingIntent notifDeletePendingIntent =
                            PendingIntent.getBroadcast(
                                    context,
                                    0,
                                    notificationDeleteIntent,
                                    PendingIntent.FLAG_CANCEL_CURRENT
                            );

                    // Intent to mark todule as done in notification
                    Intent markToduleDoneIntent = new Intent(context, getClass());
                    markToduleDoneIntent.setAction("com.danlls.daniel.todule_android.TODULE_DONE");
                    markToduleDoneIntent.putExtra("todule_id", entryId);

                    PendingIntent markToduleDonePendingIntent =
                            PendingIntent.getBroadcast(
                                    context,
                                    0,
                                    markToduleDoneIntent,
                                    PendingIntent.FLAG_CANCEL_CURRENT
                            );

                    // Construct notification
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_stat_todule)
                                    .setContentTitle("Reminder: " + intent.getStringExtra("todule_title"))
                                    .setContentText(DateTimeUtils.dateTimeDiff(System.currentTimeMillis(),intent.getLongExtra("todule_due_date", -1L)))
                                    .setContentIntent(resultPendingIntent)
                                    .setDeleteIntent(notifDeletePendingIntent)
                                    .setAutoCancel(true);

                    mBuilder.setDefaults(Notification.DEFAULT_ALL);
                    mBuilder.addAction(R.drawable.ic_done_white_18dp, "Mark as done", markToduleDonePendingIntent);

                    // Gets an instance of the NotificationManager service
                    NotificationManager mNotifyMgr =
                            (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                    // Builds the notification and issues it.
                    mNotifyMgr.notify((int) entryId, mBuilder.build());

                } else if(action.equals("com.danlls.daniel.todule_android.DELETE_REMINDER")) {
                    long toduleId = intent.getLongExtra("todule_id", -1L);
                    NotificationHelper.cancelReminder(context, toduleId);
                } else if(action.equals("com.danlls.daniel.todule_android.TODULE_DONE")){
                    long toduleId = intent.getLongExtra("todule_id", -1L);
                    NotificationManager mNotifyMgr =
                            (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                    mNotifyMgr.cancel((int) toduleId);
                    Uri toduleUri = ContentUris.withAppendedId(ToduleDBContract.TodoEntry.CONTENT_ID_URI_BASE, toduleId);
                    ContentValues cv = new ContentValues();
                    cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_TASK_DONE, ToduleDBContract.TodoEntry.TASK_COMPLETED);
                    cv.put(ToduleDBContract.TodoEntry.COLUMN_NAME_COMPLETED_DATE, System.currentTimeMillis());
                    context.getContentResolver().update(toduleUri, cv , null, null);
                }
            }

        }
    }
}
