package com.example.defaultphoneapp.calllog

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import androidx.loader.content.CursorLoader

/**
 * Created by wenjie on 2024/08/28.
 */
class CallLogCursorLoader(
    context: Context,
    uri: Uri,
    projection: Array<String>? = null,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    private val args: Bundle? = null
) : CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder) {

    companion object {
        private val PROJECTION = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE,
            CallLog.Calls.NEW,
            CallLog.Calls.CACHED_NAME,
//            CallLog.Calls.CACHED_NUMBER_TYPE,
//            CallLog.Calls.CACHED_NUMBER_LABEL,
        )
        private val URI = CallLog.Calls.CONTENT_URI
        private val SELECTION = null
        private val SELECTION_ARGS = null
        private  val SORT_ORDER = CallLog.Calls.DEFAULT_SORT_ORDER
        fun newInstance(context: Context, args: Bundle? = null): CursorLoader {
            return CallLogCursorLoader(
                context, URI, PROJECTION, SELECTION, SELECTION_ARGS, SORT_ORDER, args
            )
        }
    }
}