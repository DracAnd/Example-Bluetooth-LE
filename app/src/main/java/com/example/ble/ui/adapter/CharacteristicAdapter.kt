package com.example.ble.ui.adapter

import android.bluetooth.BluetoothGattCharacteristic
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ble.databinding.HolderCharacteristicBinding
import com.example.ble.extentions.genericDiffUtil
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class CharacteristicAdapter(
    private val cbRead: (BluetoothGattCharacteristic) -> Unit
): ListAdapter<BluetoothGattCharacteristic, CharacteristicAdapter.CharacteristicHolder>(
    genericDiffUtil(
        { o, n -> o.uuid == n.uuid },
        { o, n -> o.uuid == n.uuid }
    )
) {

    fun updateItem(characteristic: BluetoothGattCharacteristic) {
        val items = currentList.toList()
        items.forEachIndexed { index, bluetoothGattCharacteristic ->
            if (characteristic.uuid == bluetoothGattCharacteristic.uuid) {
                notifyItemChanged(index)
                return@forEachIndexed
            }
        }
    }

    override fun onBindViewHolder(holder: CharacteristicHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacteristicHolder {
        return CharacteristicHolder(
            HolderCharacteristicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            cbRead
        )
    }

    inner class CharacteristicHolder(
        private val binding: HolderCharacteristicBinding,
        private val cbRead: (BluetoothGattCharacteristic) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {

        fun onBind(model: BluetoothGattCharacteristic) {

            val newVal = if (model.value != null && model.value?.isNotEmpty() == true) {
                try {
                    String(model.value, Charset.forName("UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    "Unable to parse"
                }
            } else {
                ""
            }
            binding.value.text = newVal
            binding.uuid.text = model.uuid.toString()
            binding.btnRead.setOnClickListener {
                cbRead(model)
            }
        }
    }
}
