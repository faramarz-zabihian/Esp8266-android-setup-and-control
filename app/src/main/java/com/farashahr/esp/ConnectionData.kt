package com.farashahr.esp

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.eclipse.californium.core.config.CoapConfig
import org.eclipse.californium.core.network.CoapEndpoint
import org.eclipse.californium.core.network.EndpointManager
import org.eclipse.californium.elements.UDPConnector
import org.eclipse.californium.elements.config.Configuration
import org.eclipse.californium.scandium.DTLSConnector
import org.eclipse.californium.scandium.config.DtlsConfig
import org.eclipse.californium.scandium.config.DtlsConnectorConfig
import org.eclipse.californium.scandium.dtls.pskstore.AdvancedSinglePskStore
import java.io.*
import java.util.concurrent.TimeUnit

class ConnectionData {
    companion object {
        //private val connection_data : ConnectionData? = null
        fun initCoapEndpoint(conn: ConnectionData?): Boolean {
            if (conn == null)
                return false
            CoapConfig.register()
            DtlsConfig.register()
            val config: Configuration = Configuration.createStandardWithoutFile()

            val dtlsConfig: DtlsConnectorConfig.Builder = DtlsConnectorConfig.builder(config)
            dtlsConfig.set(DtlsConfig.DTLS_ROLE, DtlsConfig.DtlsRole.CLIENT_ONLY)
            dtlsConfig[DtlsConfig.DTLS_AUTO_HANDSHAKE_TIMEOUT, 30] = TimeUnit.SECONDS
            dtlsConfig.setAdvancedPskStore(
                AdvancedSinglePskStore(
                    conn.ClientId,
                    conn.ClientPass?.encodeToByteArray()
                )
            )
            //ConfigureDtls.loadCredentials(dtlsConfig)
            val dtlsConnector = DTLSConnector(dtlsConfig.build())
            val dtlsEndpointBuilder = CoapEndpoint.Builder()
            dtlsEndpointBuilder.setConfiguration(config)
            dtlsEndpointBuilder.setConnector(dtlsConnector)
            EndpointManager.getEndpointManager().defaultEndpoint = dtlsEndpointBuilder.build()
            // setup coap EndpointManager to udp connector
            val udpEndpointBuilder = CoapEndpoint.Builder()
            val udpConnector = UDPConnector(null, config)

            udpEndpointBuilder.setConfiguration(config)
            udpEndpointBuilder.setConnector(udpConnector)
            EndpointManager.getEndpointManager().defaultEndpoint = udpEndpointBuilder.build()
            return true
        }

        fun save_connection_data(path: File, conn: ConnectionData) {
//            conn
            val fos = FileOutputStream(File(path, "data"))
            fos.write(Gson().toJson(conn).encodeToByteArray())
            fos.close()
        }

        fun load_connection_data(path: File): ConnectionData? {
            try {
                val fis = FileInputStream(File(path, "data"))
                val istream = DataInputStream(fis)
                val br = BufferedReader(InputStreamReader(istream))
                val myData = br.readText();
                istream.close()
                if (!myData.isNullOrBlank()) {
                    val cd = Gson().fromJson<ConnectionData>(
                        myData,
                        object : TypeToken<ConnectionData>() {}.type
                    )
                    val c_data = ConnectionData()
                    c_data.ServerIP = cd.ServerIP
                    c_data.ClientPass = cd.ClientPass
                    c_data.ClientId = cd.ClientId
                    return c_data
                }
            } catch (e: Exception) {

            }
            return null
        }
    }

    val Scheme = "coaps://"
    var ServerIP: String? = null
    var ClientId: String? = null
    var ClientPass: String? = null
}
