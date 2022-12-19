package com.example.ble.ui.activity.characteristics

import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ble.databinding.FragmentServicesBinding
import com.example.ble.ui.adapter.CharacteristicAdapter
import com.example.ble.ui.viewModel.MainViewModel

class CharacteristicFragment : Fragment() {

    companion object {
        fun newInstance() = CharacteristicFragment()
    }

    private val adapter by lazy {
        CharacteristicAdapter(viewModel::readCharacteristic)
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

    }

    private fun setupFlows() {
        lifecycleScope.launchWhenCreated {
            viewModel.mCharacteristicUpdate.collect {
                adapter.updateItem(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.mServiceSelected.collect {
                setDataAdapter(it)
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

    private fun setDataAdapter(service: BluetoothGattService) {
        val lst = service.characteristics
        adapter.submitList(lst)
    }

}