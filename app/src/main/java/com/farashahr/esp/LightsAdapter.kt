package com.farashahr.esp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.warkiz.tickseekbar.OnSeekChangeListener
import com.warkiz.tickseekbar.SeekParams
import com.warkiz.tickseekbar.TickSeekBar

class LightsAdapter(val list: ArrayList<DeviceAddress>) :
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
            LayoutInflater.from(parent.context).inflate(R.layout.lights_adapter, parent, false)
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
        private var light_dimmer: TickSeekBar = mView.findViewById(R.id.light_dimmer)
        internal fun bindView(item: DeviceAddress) {
            tv_View.setText(item.Name);
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
                    item.state.value == 1 - item.state.value!! // toggle a 0/1 choice
                    mOnClickListener?.onClick(mView);
                }
            }
            tv_View.setOnClickListener(listener)
            iv_View.setOnClickListener(listener)
            mView.setOnClickListener(listener)
            light_dimmer.setOnSeekChangeListener(object : OnSeekChangeListener {
                override fun onSeeking(seekParams: SeekParams) {
                    val c = command(item.state.port!!, seekParams.progress)
                    if (mOnClickListener != null) {
                        mView.tag = item
                        item.state.value == seekParams.progress + 1 // 1->2, ... , 5->6
                        mOnClickListener?.onClick(mView);
                    }
                }

                override fun onStartTrackingTouch(seekBar: TickSeekBar) {}
                override fun onStopTrackingTouch(seekBar: TickSeekBar) {}
            })
        }
    }
}