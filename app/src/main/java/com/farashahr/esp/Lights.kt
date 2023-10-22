package com.farashahr.esp
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Lights : Fragment() {
    private lateinit var viewModel: LightsViewModel
    private lateinit var rec_view: RecyclerView
    private lateinit var mainContext: MainActivity
    private val dv_adapter = LightsAdapter(ArrayList<DeviceAddress>())

    companion object {
        fun newInstance() = Lights()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.lights_fragment, container, false)
        var rec_view = view.findViewById<RecyclerView>(R.id.rec_lights)
        rec_view.adapter = dv_adapter
        rec_view.layoutManager = LinearLayoutManager(requireContext());
        dv_adapter.setOnClickListener {
            viewModel.sendCommand(requireContext(), it.tag as DeviceAddress)
        }
        viewModel = ViewModelProvider(
            this,
            LightsViewModelFactory(getQueryUrl(requireContext(), LightsViewModel.DEVICE_TYPE))
        ).get(LightsViewModel::class.java)

        // updates adapter data, also requests for device status if necessary
        viewModel.listLive().observe(this.viewLifecycleOwner) { dl ->
            dl.filter { it.dirty }.forEach {
                viewModel.readStatus(requireContext(), it) // ask for latest status of sensor
            }
            dv_adapter.ResetData(dl)
        }
        // updates client status data based on report request
        mainContext.obs!!.register_for_reports(viewModel.listLive())
        // asks for simple list of devices
        viewModel.getDeviceList()
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity)
            mainContext = context
    }

}
