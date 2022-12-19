package com.example.ble.ui.activity.devices

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ble.databinding.FragmentDevicesBinding
import com.example.ble.ui.adapter.DevicesAdapter
import com.example.ble.ui.viewModel.MainViewModel

class DevicesFragment : Fragment() {

    private val adapter: DevicesAdapter by lazy {
        DevicesAdapter(viewModel::connectDevice, viewModel::searchGattToDisconnect, viewModel::secondFragment)
    }

    companion object {
        fun newInstance() = DevicesFragment()
    }

    private val viewModel by activityViewModels<MainViewModel>()

    private lateinit var binding: FragmentDevicesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDevicesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupListeners()
        setupAdapters()
        setupFlows()

        setDataAdapter()
    }

    private fun setupFlows() {
        lifecycleScope.launchWhenCreated {
            viewModel.updateAdapter.collect {
                updateAdapter()
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.deviceFound.collect {
                deviceFound(it)
            }
        }
    }

    private fun setupViewModel() {

    }

    private fun setupListeners() {
        activity?.runOnUiThread {
            binding.btn.setOnClickListener {
                viewModel.onDisconnectAllGatts()
                adapter.clearList()
                viewModel.validationBLE()
            }
        }
    }

    private fun setupAdapters() {
        binding.rvDevices.adapter = adapter
    }

    private fun deviceFound(device: BluetoothDevice) {
        val pair = viewModel.getPairDeviceAndGatt(device)
        adapter.addNewItem(pair)
    }

    private fun updateAdapter() {
        activity?.runOnUiThread {
            setDataAdapter()
        }
    }

    private fun setDataAdapter() {
        adapter.clearList()
        val pairs = viewModel.getPairsDeviceAndGatt()
        adapter.setNewList(pairs)
    }
}
