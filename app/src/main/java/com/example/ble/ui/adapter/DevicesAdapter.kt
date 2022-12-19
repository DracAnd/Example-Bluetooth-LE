package com.example.ble.ui.adapter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ble.R
import com.example.ble.databinding.HolderDeviceBinding

class DevicesAdapter(
    private val callbackConnect: (BluetoothDevice) -> Unit,
    private val callbackDisconnect: (BluetoothDevice) -> Unit,
    private val callbackContinue: (BluetoothDevice) -> Unit
): RecyclerView.Adapter<DevicesAdapter.DeviceHolder>() {

    private val lst = arrayListOf<Pair<BluetoothDevice, BluetoothGatt?>>()

    fun setNewList(newLst: List<Pair<BluetoothDevice, BluetoothGatt?>>) {
        lst.clear()
        lst.addAll(newLst)
        this.notifyDataSetChanged()
    }

    @SuppressLint("MissingPermission")
    fun clearList() {
        while (true) {
            if (lst.size > 0) {
                lst.removeAt(0)
                notifyItemRemoved(0)
            } else {
                break
            }
        }
    }

    fun addNewItem(item: Pair<BluetoothDevice, BluetoothGatt?>) {
        lst.add(item)
        notifyItemInserted(lst.size - 1)
    }

    override fun getItemCount(): Int = lst.size

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        holder.onBind(lst[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        return DeviceHolder(
            HolderDeviceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            callbackConnect,
            callbackDisconnect,
            callbackContinue
        )
    }

    class DeviceHolder(
        private val binding: HolderDeviceBinding,
        private val callbackConnect: (BluetoothDevice) -> Unit,
        private val callbackDisconnect: (BluetoothDevice) -> Unit,
        private val callbackContinue: (BluetoothDevice) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingPermission")
        fun onBind(model: Pair<BluetoothDevice, BluetoothGatt?>) {
            binding.name.text = model.first.name ?: "Unknown name"
            binding.address.text = model.first.address
            binding.btnConnect.setOnClickListener {
                callbackConnect(model.first)
                binding.btnConnect.visibility = View.GONE
                binding.progress.visibility = View.VISIBLE
            }
            binding.btnDisconnect.setOnClickListener {
                callbackDisconnect(model.first)
                binding.btnDisconnect.visibility = View.GONE
                binding.progress.visibility = View.VISIBLE
            }
            binding.contine.setOnClickListener {
                callbackContinue(model.first)
            }
            if (model.second  == null) {
                binding.progress.visibility = View.GONE
                binding.btnConnect.visibility = View.VISIBLE
                binding.btnDisconnect.visibility = View.GONE
                binding.contine.visibility = View.GONE
                binding.state.setCardBackgroundColor(ContextCompat.getColor(binding.state.context, R.color.disconnected))
            } else {
                binding.progress.visibility = View.GONE
                binding.btnConnect.visibility = View.GONE
                binding.btnDisconnect.visibility = View.VISIBLE
                binding.contine.visibility = View.VISIBLE
                binding.state.setCardBackgroundColor(ContextCompat.getColor(binding.state.context, R.color.connected))
            }
        }
    }
}