package com.farashahr.esp

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.model.UnsignedInteger
import com.farashahr.esp.ConnectionData.Companion.load_connection_data
import com.farashahr.esp.ConnectionData.Companion.save_connection_data
import com.farashahr.esp.databinding.ActivityUserConnectBinding
import kotlinx.coroutines.delay
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapResponse
import java.text.SimpleDateFormat
import java.util.*

class UserConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserConnectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var c_data = load_connection_data(getExternalFilesDir("/")!!)

        binding.apServerEdit.setText(c_data?.ServerIP)
        binding.apClientIdEdit.setText(c_data?.ClientId)
        binding.confirmBtn.setOnClickListener {
            if(c_data == null)
                c_data = ConnectionData()
            c_data!!.ServerIP = binding.apServerEdit.text.toString()
            c_data!!.ClientId = binding.apClientIdEdit.text.toString()
            c_data!!.ClientPass = binding.apClientPasswordEdit.text.toString()
            if( ConnectionData.initCoapEndpoint(c_data)) {
                val coap = CoapClient(c_data!!.Scheme + c_data!!.ServerIP + "/time")
                var resp: CoapResponse
                try {
                    resp = coap.get();
                    if (resp.isSuccess) {
                        save_connection_data(getExternalFilesDir("/")!!, c_data!!);
                        val list = CborDecoder.decode(resp.payload);
                        val di = (list.get(0) as UnsignedInteger).value.toLong()
                        val fmt = SimpleDateFormat(
                            "زمان روی سرور yyyy/MM/dd HH:mm:ss",
                            Locale("ir-fa", "IR")
                        )
                        binding.messageView.text = fmt.format(java.util.Date(di * 1000))
                        Handler().postDelayed({
                            finish()
                        }, 1000)

                    } else
                        binding.messageView.text = getString(R.string.ap_connection_problem)
                } catch (ex: Exception) {
                    binding.messageView.text = getString(R.string.app_connection_error)
                }
            } else
                binding.messageView.text = "اطلاعات انصال درست نیستند"
        }

        //setSupportActionBar(binding.toolbar)
    }
}