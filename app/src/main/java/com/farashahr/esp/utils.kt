package com.farashahr.esp

import android.content.Context

fun getOutboxUrl(context: Context, targetResource: String, targetId: String): String {
    return String.format(
        context.getString(R.string.app_coap_url_format),
        context.getString(R.string.app_server_ip),
        targetResource,
        targetId
    )
}
fun getQueryUrl(context: Context, devType: String) : String{
    return String.format(
        context.getString(R.string.app_query_url_format),
        context.getString(R.string.app_server_ip),
        devType
    )
}

fun getObserveMessagesUrl(context: Context, userName: String): String {
    return String.format(
        context.getString(R.string.app_coap_url_format),
        context.getString(R.string.app_server_ip),
        context.getString(R.string.app_user_resource),
        userName
    )
}
fun getObserveReportUrl(context: Context): String {
    return String.format(
        context.getString(R.string.app_coap_url_format),
        context.getString(R.string.app_server_ip),
        context.getString(R.string.app_report_resource),
        context.getString(R.string.app_report_id)
    )
}