package com.example.ble.ui.activity.services

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ble.databinding.FragmentServicesBinding
import com.example.ble.ui.adapter.ServicesAdapter
import com.example.ble.ui.viewModel.MainViewModel

class ServicesFragment : Fragment() {

    companion object {
        fun newInstance() = ServicesFragment()
    }

    private val adapter by lazy {
        ServicesAdapter(viewModel::selectService)
    }

    private val viewModel by activityViewModels<MainViewModel>()

    private lateinit var binding: FragmentServicesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServicesBinding.inflate(layoutInflater)
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
            viewModel.mGattsSelected.collect {
                setupGatt(it)
                setDataAdapter()
            }
        }
    }

    private fun setupViewModel() {

    }

    private fun setupAdapters() {
        binding.rvServices.adapter = adapter
    }

    private fun setupListeners() {

    }

    @SuppressLint("MissingPermission")
    private fun setupGatt(gatt: BluetoothGatt?) {
        gatt?.let {
            binding.tvDevice.text = gatt.device.name?.toString() ?: "Unknown device"
            binding.tvDeviceMac.text = gatt.device.address
        } ?: run {
            binding.tvDevice.text = ""
            binding.tvDeviceMac.text = ""
        }
    }

    private fun setDataAdapter() {
        val lst = viewModel.getServicesDeviceSelected()
        adapter.submitList(lst)
    }
}
