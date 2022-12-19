package com.example.ble.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ble.databinding.ActivityMainBinding
import com.example.ble.ui.adapter.SectionsPagerAdapter
import com.example.ble.ui.model.LocalGattCallback
import com.example.ble.ui.viewModel.MainViewModel
import com.google.android.material.tabs.TabLayout

private const val REQUEST_ENABLE_BT = 1234
private const val REQUEST_FINE_LOCATION = 12311

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    private val bluetoothManager by lazy(LazyThreadSafetyMode.NONE) {
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupViewModel()
        setupFlows()
    }

    private fun setupViewPager() {
        val sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        
        binding.viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(binding.viewPager)
    }

    private fun setupFlows() {
        lifecycleScope.launchWhenCreated {
            viewModel.requestBluetoothEnable.collect {
                requestBluetoothEnable()
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.requestLocationPermission.collect {
                requestLocationPermission()
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.unsupportedBLE.collect {
                unsupportedBLE()
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.scanCompleted.collect {
                scanCompleted()
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.unableToReadCharacteristic.collect {
                unableToReadCharacteristic()
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.goToFragment.collect {
                binding.viewPager.currentItem = it
            }
        }
    }

    private fun setupViewModel() {
        viewModel.setBluetoothAdapter(bluetoothAdapter)
        viewModel.serCallback4(::openGattCallback)
        viewModel.serCallback5(::hasBluetoothLE)
        viewModel.serCallback8(::hasLocationPermissions)
    }

    private fun openGattCallback(
        device: BluetoothDevice,
        gattCallback: LocalGattCallback
    ) {
        device.connectGatt(
            this,
            false,
            gattCallback
        )
    }

    private fun hasBluetoothLE() = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    private fun unsupportedBLE() {
        Toast.makeText(this, "El dispositivo no soporta BLE", Toast.LENGTH_SHORT).show()
    }

    private fun scanCompleted() {
        Toast.makeText(this, "scaneo finalizado", Toast.LENGTH_SHORT).show()
    }

    private fun unableToReadCharacteristic() {
        Toast.makeText(this, "No es posible leer la caracteristica", Toast.LENGTH_SHORT).show()
    }

    private fun requestBluetoothEnable() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    private fun hasLocationPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            viewModel.validationBLE()
        } else {
            Toast.makeText(
                this,
                "Se necesita bluetooth para buscar los dispositivos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_FINE_LOCATION && grantResults[0] == PERMISSION_GRANTED) {
            viewModel.validationBLE()
        } else {
            Toast.makeText(
                this,
                "Se necesita los permisos de localizacion para buscar los dispositivos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
