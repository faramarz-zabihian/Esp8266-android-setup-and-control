package com.farashahr.esp

import android.content.Context
import androidx.lifecycle.*
import java.util.concurrent.Executors


class LightsViewModel(val queryDeliveryUrl:String) : ViewModel() {
    val io_executor = Executors.newFixedThreadPool(4)
    companion object {
        const val DEVICE_TYPE = "lights"
    }
    private val repo = Repository(io_executor, queryDeliveryUrl)


    fun listLive(): MutableLiveData<List<DeviceAddress>> {
        return repo.getliveData()
    }
    fun getDeviceList()
    {
        repo.getDeviceList();
    }
    private fun send(ctx: Context, d: DeviceAddress, c : Int)
    {
        repo.post(
            getOutboxUrl(ctx, d.resourceName!!, d.deviceName!!) ,
            ctx.getString(R.string.app_user_resource),
            ctx.getString(R.string.app_user_name),
            d.resourceName!!,
            d.deviceName!!,
            c
        )
    }
    fun sendCommand(ctx: Context, d: DeviceAddress) {
        send(ctx, d, command(d.Sensor, if (d.state.value != 0) 0 else 1).data)
    }
    fun readStatus(ctx: Context, d: DeviceAddress) {
        send(ctx, d, command(0, 0).data)
    }
}



