package com.farashahr.esp

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.CborException
import co.nstant.`in`.cbor.model.*
import co.nstant.`in`.cbor.model.Array
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import org.eclipse.californium.core.coap.MediaTypeRegistry

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executor


class Repository(
    val ioExecutor: Executor,
    val query_delivery_url: String
) {

    companion object {
    }
    private val dataSetLive = MutableLiveData<List<DeviceAddress>>()
    private val handler = Handler(Looper.getMainLooper())

    fun getliveData() : MutableLiveData<List<DeviceAddress>>
    {
        return dataSetLive;
    }
    fun post(
        post_delivery_url: String,
        clientResourceName: String,
        clientId: String,
        targetResourceName: String,
        targetId: String,
        cmd: Int
    ) {
        CoAPConnect.post(ioExecutor, post_delivery_url, clientResourceName, clientId, targetResourceName, targetId, UnsignedInteger(cmd.toLong()));
    }
    fun getDeviceList()
    {
        CoAPConnect.getDeviceList(ioExecutor, query_delivery_url, handler, dataSetLive)
    }

}