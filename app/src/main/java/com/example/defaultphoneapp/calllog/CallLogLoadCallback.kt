package com.example.defaultphoneapp.calllog

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader

/**
 * Created by wenjie on 2024/08/28.
 */
class CallLogLoadCallback(
    private val context: Context, private val callback: (MutableList<CallLogItem>) -> Unit = {}
) : LoaderManager.LoaderCallbacks<Cursor> {

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CallLogCursorLoader.newInstance(context, args)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        loader.reset()
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        //calllog
//        CallLog.Calls._ID
//        CallLog.Calls.NUMBER,
//        CallLog.Calls.DATE,
//        CallLog.Calls.DURATION,
//        CallLog.Calls.TYPE,
//        CallLog.Calls.NEW,
        val callLogItems = mutableListOf<CallLogItem>()
        data?.let { cursor ->
            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls._ID))
                    val number =
                        cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val dateLong = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    val duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                    val type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                    val new = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.NEW))
                    val cacheName = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))
                    Log.e("asd", "通话记录： $id $cacheName $number $duration $type $new")
                    callLogItems += CallLogItem(id, cacheName,number,dateLong, duration, type, new)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            callback(callLogItems)
        }
    }
}