package com.farashahr.esp

class DeviceAddress {
    var Sensor: Int = 0
    var deviceName: String? = null
    var resourceName: String? = null
    var Name: String? = null
    var code : String? = null
    var rtt: Long? = null
    var codeName: String? = null
    var state = DeviceCommand(0)
    var dirty = true
}