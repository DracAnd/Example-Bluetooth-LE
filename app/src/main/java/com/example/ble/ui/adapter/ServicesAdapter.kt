package com.example.ble.ui.adapter

import android.bluetooth.BluetoothGattService
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ble.databinding.HolderServiceBinding
import com.example.ble.extentions.genericDiffUtil

class ServicesAdapter(
    private val cbSelect: (BluetoothGattService) -> Unit
): ListAdapter<BluetoothGattService, ServicesAdapter.ServiceHolder>(
    genericDiffUtil(
        { o, n -> o.uuid == n.uuid },
        { o, n -> o.uuid == n.uuid }
    )
) {

    override fun onBindViewHolder(holder: ServiceHolder, position: Int) {
        holder.onBind(getItem(position))
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceHolder {
        return ServiceHolder(
            HolderServiceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            cbSelect
        )
    }

    class ServiceHolder(
        private val binding: HolderServiceBinding,
        private val cbSelect: (BluetoothGattService) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {

        fun onBind(model: BluetoothGattService) {
            binding.name.text = ""
            binding.uuid.text = model.uuid.toString()
            binding.btnContinue.setOnClickListener {
                cbSelect.invoke(model)
            }
        }
    }
}
