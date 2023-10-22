package com.farashahr.esp

class command() {
    var data: Int
        get() {
            return (port?:0)  + ((value?:0) shl 2) + ((order?:0) shl 5)
        }
        set(d) {
            port = d            //00000011
            value = d shr 2     //00011100
            order = d shr 5     //11100000
        }
    var  port: Int? = null
        get() = field
        set(value)
        {
            field = (value?:0) and 0b11
        }
    var value: Int? = null
        get() = field
        set(v)
        {
            field = (v?:0) and 0b111 //00011100 shr 2
        }
    var order: Int? = null
        get() = field
        set(value)
        {
            field = (value?:0) and 0b111 // 11100000 shr 5
        }
    constructor(d : Int) : this() { data = d}
    constructor(port : Int, value: Int) : this() {
        this.port = port
        this.value = value
    }
}
class DeviceAddress {
    var Sensor: Int = 0
    var deviceName: String? = null
    var resourceName: String? = null
    var Name: String? = null
    var code : String? = null
    var rtt: Long? = null
    var codeName: String? = null
    var state : command = command(0)
    var dirty = true
}