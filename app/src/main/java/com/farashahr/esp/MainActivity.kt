/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 * Matthias Kovatsch - creator and main architect
 * Vikram - added dtls client
 */
package com.farashahr.esp


import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.Debug
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import org.eclipse.californium.core.config.CoapConfig
import org.eclipse.californium.core.network.CoapEndpoint
import org.eclipse.californium.core.network.EndpointManager
import org.eclipse.californium.elements.UDPConnector
import org.eclipse.californium.elements.config.Configuration
import org.eclipse.californium.scandium.DTLSConnector
import org.eclipse.californium.scandium.config.DtlsConfig
import org.eclipse.californium.scandium.config.DtlsConnectorConfig
import org.eclipse.californium.scandium.dtls.pskstore.AdvancedSinglePskStore
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private var demoCollectionAdapter: DeviceCollectionAdapter? = null

    var obs: CoAPConnect? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        demoCollectionAdapter = DeviceCollectionAdapter(this)
        val viewPager: ViewPager2 = findViewById(R.id.pager)
        viewPager.setAdapter(demoCollectionAdapter)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(
            tabLayout,
            viewPager,
            TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                if (position == 0) tab.text = "Lights" else tab.text = "BackLights"
            }).attach()

        ConnectionData.initCoapEndpoint(ConnectionData.load_connection_data(getExternalFilesDir("/")!!))
        val observe_url = getObserveReportUrl(this)
        obs = CoAPConnect()

        val thread = Thread {
            try {
                obs!!.Observe(Executors.newSingleThreadExecutor(), observe_url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnu_touch -> {
                val intent = Intent(this, EspTouch2Activity::class.java)
                startActivity(intent)
                true
            }
            R.id.mnu_connect -> {
                val intent = Intent(this, UserConnectActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}