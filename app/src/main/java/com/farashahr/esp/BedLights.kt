package com.farashahr.esp

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BedLights : Fragment() {
    companion object {
        fun newInstance() = BedLights()
    }

    private lateinit var mainContext: MainActivity
    private lateinit var viewModel: LightsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bed_lights_fragment, container, false)
        var rec_view = view.findViewById<RecyclerView>(R.id.rec_lights)
        val dv_adapter = BedLightsAdapter(ArrayList<DeviceAddress>())
        rec_view.layoutManager = LinearLayoutManager(requireContext());
        rec_view.adapter = dv_adapter

        val query_url = getQueryUrl(requireContext(), BedLightsViewModel.DEVICE_TYPE)

        var viewModel = ViewModelProvider(
            this,
            BedLightsViewModelFactory(query_url)
        ).get(BedLightsViewModel::class.java)

        viewModel.listLive().observe(this.viewLifecycleOwner) { dl ->
            dl.filter { it.dirty }.forEach {
                viewModel.readStatus(requireContext(), it)
            }
            dv_adapter.ResetData(dl)
        }

        dv_adapter.setOnClickListener {
            viewModel.sendCommand(requireContext(), it.tag as DeviceAddress)
        }
        mainContext.obs!!.register_for_reports(viewModel.listLive())
        viewModel.getDeviceList() // read device list
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity)
            mainContext = context
    }
}

class BedLightsAdapter(val list: ArrayList<DeviceAddress>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mOnClickListener: View.OnClickListener? = null
    fun setOnClickListener(listener: View.OnClickListener) {
        mOnClickListener = listener
    }

    fun ResetData(dl: List<DeviceAddress>) {
        list.clear()
        list.addAll(dl)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.bedlights_adapter, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindView(getItem(position)!!)
    }

    private fun getItem(position: Int): DeviceAddress? {
        return list.get(position)
    }

    override fun getItemCount(): Int {
        return list.size;
    }

    inner class ViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {
        //        private val mId: String? = null
        private val tv_View: TextView = mView.findViewById(R.id.tvName)
        private val iv_View: ImageView = mView.findViewById(R.id.iv_state)

        internal fun bindView(item: DeviceAddress) {
            tv_View.setText(item.Name);
            //val state = item.ports.firstOrNull { it.name == BedLightsViewModel.DEVICE_TYPE }?.value
            val state = item.state.value
            iv_View.visibility = View.INVISIBLE
            if (state != null) {
                iv_View.visibility = View.VISIBLE
                iv_View.setImageDrawable(
                    iv_View.context.resources.getDrawable(
                        if (state == 1) R.drawable.ic_baseline_flash_on_24 else R.drawable.ic_baseline_flash_off_24,
                        iv_View.context.theme
                    )
                )
            } else {

            }
            //todo: iv_View call for state
            val listener: (View) -> Unit = {
                if (mOnClickListener != null) {
                    mView.tag = item
                    mOnClickListener?.onClick(mView);
                }
            }
            tv_View.setOnClickListener(listener)
            iv_View.setOnClickListener(listener)
            mView.setOnClickListener(listener)
        }
    }

}
