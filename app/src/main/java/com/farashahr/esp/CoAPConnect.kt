package com.farashahr.esp

import android.os.Handler
import android.util.Log
import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.CborEncoder
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
import androidx.lifecycle.MutableLiveData
import co.nstant.`in`.cbor.CborException

class CoAPConnect {
    abstract class ResponseHandler(
        val listLive: androidx.lifecycle.MutableLiveData<List<DeviceAddress>>,
    ) {
    }

    val handlers = mutableSetOf<ResponseHandler>()
    fun register_for_reports(list: MutableLiveData<List<DeviceAddress>>) {
        handlers.add(object : ResponseHandler(list) {})
    }

    // observing reports
    fun Observe(
        ioExecutor: Executor,
        uri: String
    ) {
        ioExecutor.execute {
            val client = CoapClient(uri)
            try {
                client
                    .observe(object : CoapHandler {
                        override fun onLoad(response: CoapResponse?) {
                            if (response == null || !response.isSuccess)
                                return@onLoad
                            try {
                                if (response.payloadSize == 0)
                                    return@onLoad

                                val bais = ByteArrayInputStream(
                                    response.payload,
                                    0,
                                    response.payloadSize
                                )
                                processReport(bais)

                                /*if (!dataItems.isEmpty())
                                    for (h in handlers)
                                        h.onLoad(dataItems)*/

                            } catch (ex: java.lang.Exception) {

                            }
                        }

                        override fun onError() {

                        }

                    })
            } catch (ex: Exception) {
                Log.e("coap", ex.message, ex)
            }
        }
    }

    private fun processReport(bais: ByteArrayInputStream) {
        val dataItems: List<DataItem> = CborDecoder(bais).decode()
        for (di in dataItems) {
            if (di.majorType == MajorType.ARRAY) {
                for (d in (di as Array).dataItems) {
                    val cols = d as Array
                    // fromPart
                    val toRes = String(
                        ((cols.dataItems[0] as Array).dataItems[0] as ByteString).bytes,
                        StandardCharsets.UTF_8
                    )
                    val toId = String(
                        ((cols.dataItems[0] as Array).dataItems[1] as ByteString).bytes,
                        StandardCharsets.UTF_8
                    )
                    val value =
                        (cols.dataItems[2] as UnsignedInteger).value.toInt()
                    val c = DeviceCommand(value)
                    for (h in handlers) {
                        var changed = false
                        h.listLive.value?.firstOrNull() {
                            it.resourceName == toRes && it.deviceName == toId && it.Sensor == c.port
                        }?.apply {
                            state.data = value
                            changed = true
                            dirty = false
                            //                                    state = value
                        }
                        if (changed)
                            h.listLive.postValue(h.listLive.value)

                    }
                }
            }
        }
    }

    companion object {
        fun post(
            ioExecutor: Executor,
            post_delivery_url: String,
            clientResourceName: String,
            clientId: String,
            targetResourceName: String,
            targetId: String,
            data: DataItem?
        ) {
            ioExecutor.execute {
                try {
                    val baos = ByteArrayOutputStream()
                    CborEncoder(baos).encode(
                        CborBuilder()
                            .addArray()
                            .addArray() // add array
                            .addArray().add(clientResourceName.toByteArray())
                            .add(clientId.toByteArray()).end()
                            .addArray().add(targetResourceName.toByteArray())
                            .add(targetId.toByteArray()).end()
                            .add(data)
                            .end()
                            .end()
                            .build()
                    )
                    val encodedBytes = baos.toByteArray()
                    val client = CoapClient(post_delivery_url)
                    client.post(encodedBytes, MediaTypeRegistry.APPLICATION_CBOR)
                } catch (ex: Exception) {
                    Log.e("coap", ex.message, ex)
                }

            }
        }

        // list of installed devices/sensors
        fun getDeviceList(
            ioExecutor: Executor,
            query_delivery_url: String,
            handler: Handler,
            reportList: MutableLiveData<List<DeviceAddress>>
        ) {
            ioExecutor.execute {
                val response: CoapResponse? = try {
                    val client = CoapClient(query_delivery_url)
                    client.get()
                } catch (ex: Exception) {
                    Log.e("coap", ex.message, ex)
                    null
                }
                handler.post {
                    if (response != null) {
                        val bais = ByteArrayInputStream(response.payload, 0, response.payloadSize)
                        try {
                            val dataItems: List<DataItem> = CborDecoder(bais).decode()
                            val da_list: MutableList<DeviceAddress> = ArrayList()
                            for (dataItem in dataItems) { // root array row
                                val resources = (dataItem as Array).dataItems
                                for (res in resources) {
                                    val da = DeviceAddress()
                                    da_list.add(da)
                                    val r1 = (res as Array).dataItems
                                    da.resourceName = String(
                                        (r1[0] as ByteString).getBytes(),
                                        StandardCharsets.UTF_8
                                    )
                                    da.deviceName = String(
                                        (r1[1] as ByteString).getBytes(),
                                        StandardCharsets.UTF_8
                                    )
                                    da.Name = String(
                                        (r1[2] as ByteString).getBytes(),
                                        StandardCharsets.UTF_8
                                    )
                                    da.Sensor = (r1[3] as UnsignedInteger).value.toInt()
                                }
                            }
                            reportList.value = da_list
                        } catch (e: CborException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}