// EspTouch-v2
// this file has been converted to kt from the above demo app.
package com.farashahr.esp
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import com.espressif.iot.esptouch2.provision.*
import com.farashahr.esp.databinding.ActivityEsptouch2Binding
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference
import java.net.InetAddress
import java.nio.charset.StandardCharsets
import java.security.MessageDigest


class EspTouch2Activity : EspTouchActivityAbs() {
    private var mProvisioner: EspProvisioner? = null
    private var mBinding: ActivityEsptouch2Binding? = null
    private var mAddress: InetAddress? = null
    private var mSsid: String? = null
    private lateinit var mSsidBytes: ByteArray
    private var mBssid: String? = null
    private var mMessage: CharSequence? = null
    private var mMessageVisible = 0
    private var mControlVisible = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityEsptouch2Binding.inflate(
            layoutInflater
        )
        setContentView(mBinding!!.root)
        mBinding!!.controlGroup.visibility = View.INVISIBLE
        mBinding!!.confirmBtn.setOnClickListener { v: View? ->
            val request = genRequest() ?: return@setOnClickListener
            if (mProvisioner != null) {
                mProvisioner!!.close()
            }
            val intent = Intent(this@EspTouch2Activity, EspProvisioningActivity::class.java)
            intent.putExtra(EspProvisioningActivity.KEY_PROVISION_REQUEST, request)
            startActivityForResult(intent, REQUEST_PROVISIONING)
            mBinding!!.confirmBtn.isEnabled = false
        }
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        EspTouchApp.getInstance().observeBroadcast(this, { action: String? -> check() })
        check()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        mProvisioner = EspProvisioner(applicationContext)
        val syncListener = SyncListener(mProvisioner!!)
        mProvisioner!!.startSync(syncListener)
    }

    override fun onStop() {
        super.onStop()
        if (mProvisioner != null) {
            mProvisioner!!.stopSync()
            mProvisioner!!.close()
            mProvisioner = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION) {
            check()
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PROVISIONING) {
            mBinding!!.confirmBtn.isEnabled = true
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun getEspTouchVersion(): String {
        return getString(R.string.esptouch2_about_version, IEspProvisioner.ESPTOUCH_VERSION)
    }

    private fun checkState(): Boolean {
        var stateResult = checkPermission()
        if (!stateResult.permissionGranted) {
            mMessage = stateResult.message
            mBinding!!.messageView.setOnClickListener { v: View? ->
                val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                ActivityCompat.requestPermissions(
                    this@EspTouch2Activity,
                    permissions,
                    REQUEST_PERMISSION
                )
            }
            return false
        }
        stateResult = checkLocation()
        if (stateResult.locationRequirement) {
            mMessage = stateResult.message
            mBinding!!.messageView.setOnClickListener(null)
            return false
        }
        stateResult = checkWifi()
        mSsid = stateResult.ssid
        mSsidBytes = stateResult.ssidBytes
        mBssid = stateResult.bssid
        mMessage = stateResult.message
        mAddress = stateResult.address
        return stateResult.wifiConnected && !stateResult.is5G
    }

    private val bssidBytes: ByteArray?
        get() = if (mBssid == null) null else TouchNetUtil.convertBssid2Bytes(mBssid)

    private fun invalidateAll() {
        mBinding!!.controlGroup.visibility = mControlVisible
        mBinding!!.apSsidText.text = mSsid
        mBinding!!.apBssidText.text = mBssid
        mBinding!!.ipText.text = if (mAddress == null) "" else mAddress!!.hostAddress
        mBinding!!.messageView.text = mMessage
        mBinding!!.messageView.visibility = mMessageVisible
    }

    private fun check() {
        if (checkState()) {
            mControlVisible = View.VISIBLE
            mMessageVisible = View.GONE
        } else {
            mControlVisible = View.GONE
            mMessageVisible = View.VISIBLE
            if (mProvisioner != null) {
                if (mProvisioner!!.isSyncing) {
                    mProvisioner!!.stopSync()
                }
                if (mProvisioner!!.isProvisioning) {
                    mProvisioner!!.stopProvisioning()
                }
            }
        }
        invalidateAll()
    }

    private fun genRequest(): EspProvisioningRequest? {
        ////////////////
        mBinding!!.apServerEdit.error = null
        val serverEditChars: CharSequence? = mBinding!!.apServerEdit.text
        if (serverEditChars == null || serverEditChars!!.length < 10) {
            mBinding!!.apServerEdit.error = "server name incorrect"
            return null
        }
        ///////////
        mBinding!!.apDevicePassEdit.error = null
        val passwordEditChars: CharSequence? = mBinding!!.apDevicePassEdit.text
        if (passwordEditChars == null || passwordEditChars.length < 6) {
            mBinding!!.apDevicePassEdit.error = "Password must be atleast 6 chars"
            return null
        }
        //////////////////////
        mBinding!!.apAESKeyEdit.error = null
        val aesKeyChars = mBinding!!.apAESKeyEdit.text
        if (aesKeyChars == null || aesKeyChars.length != 16) {
            mBinding!!.apAESKeyEdit.error = getString(R.string.app_aes_key_error)
            return null
        }
        val aesKeyHashed = MessageDigest.getInstance("SHA-256").digest(aesKeyChars.toString().toByteArray(StandardCharsets.US_ASCII)).copyOfRange(0,5)
        //......................................
        val baos = ByteArrayOutputStream()
        CborEncoder(baos).encode(
            CborBuilder()
                .addArray()
                .add(aesKeyHashed)
                .add(serverEditChars.toString())
                .add(passwordEditChars.toString())
                .end()
                .build()
        )
        val customData = baos.toByteArray()

        if (customData == null || customData.size > 127) {
            if(passwordEditChars.length > 6)
                mBinding!!.apDevicePassEdit.error = "Shorten password length"
            if(serverEditChars.length > 12)
                mBinding!!.apServerEdit.error = "Shorten server name"
            return null
        }
        val wifi_password: CharSequence? =
        return EspProvisioningRequest.Builder(applicationContext)
            .setSSID(mSsidBytes)
            .setBSSID(bssidBytes!!)
            .setPassword(mBinding!!.apWifiPasswordEdit.text.toString()?.toByteArray())
            .setAESKey(aesKeyChars.toString().toByteArray())
            .setReservedData(customData)
            .build()
    }

    private class SyncListener constructor(provisioner: EspProvisioner) : EspSyncListener {
        private val provisioner: WeakReference<EspProvisioner>
        override fun onStart() {
            Log.d(TAG, "SyncListener onStart")
        }

        override fun onStop() {
            Log.d(TAG, "SyncListener onStop")
        }

        override fun onError(e: Exception) {
            e.printStackTrace()
            val provisioner = provisioner.get()
            provisioner?.stopSync()
        }

        init {
            this.provisioner = WeakReference(provisioner)
        }
    }

    companion object {
        private val TAG = EspTouch2Activity::class.java.simpleName
        private const val REQUEST_PERMISSION = 0x01
        private const val REQUEST_PROVISIONING = 0x02
    }
}