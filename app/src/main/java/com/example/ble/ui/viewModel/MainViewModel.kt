package com.example.ble.ui.viewModel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble.ui.model.LocalGattCallback
import com.example.ble.ui.model.LocalScanCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class MainViewModel: ViewModel() {

    private val dispatcher = viewModelScope.coroutineContext + Dispatchers.Main

    private val _mGattsSelected = MutableSharedFlow<BluetoothGatt?>()
    val mGattsSelected: SharedFlow<BluetoothGatt?> = _mGattsSelected

    private val _mServiceSelected = MutableSharedFlow<BluetoothGattService>()
    val mServiceSelected: SharedFlow<BluetoothGattService> = _mServiceSelected

    private val _mCharacteristicUpdate = MutableSharedFlow<BluetoothGattCharacteristic>()
    val mCharacteristicUpdate: SharedFlow<BluetoothGattCharacteristic> = _mCharacteristicUpdate

    private val _deviceFound = MutableSharedFlow<BluetoothDevice>()
    val deviceFound: SharedFlow<BluetoothDevice> = _deviceFound

    private val _goToFragment = MutableSharedFlow<Int>()
    val goToFragment: SharedFlow<Int> = _goToFragment

    private val _unableToReadCharacteristic = MutableSharedFlow<Unit>()
    val unableToReadCharacteristic: SharedFlow<Unit> = _unableToReadCharacteristic

    private val _scanCompleted = MutableSharedFlow<Unit>()
    val scanCompleted: SharedFlow<Unit> = _scanCompleted

    private val _updateAdapter = MutableSharedFlow<Unit>()
    val updateAdapter: SharedFlow<Unit> = _updateAdapter

    private val _unsupportedBLE = MutableSharedFlow<Unit>()
    val unsupportedBLE: SharedFlow<Unit> = _unsupportedBLE

    private val _requestBluetoothEnable = MutableSharedFlow<Unit>()
    val requestBluetoothEnable: SharedFlow<Unit> = _requestBluetoothEnable

    private val _requestLocationPermission = MutableSharedFlow<Unit>()
    val requestLocationPermission: SharedFlow<Unit> = _requestLocationPermission

    /** Callback 4 */
    private var openGattCallback: ((device: BluetoothDevice, gattCallback: LocalGattCallback) -> Unit)? = null

    /** Callback 5 */
    private var hasBluetoothLE: (() -> Boolean)? = null

    /** Callback 8 */
    private var hasLocationPermissions: (() -> Boolean)? = null


    private val SCAN_PERIOD = 10000L

    /** Dispositivo seleccionado*/
    private var deviceSelected: BluetoothDevice? = null

    private val mDevices = arrayListOf<BluetoothDevice>()
    private val mGatts = arrayListOf<BluetoothGatt>()

    private var mScanning: Boolean = false
    private var mScanCallback: LocalScanCallback? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null

    private var bluetoothAdapter: BluetoothAdapter? = null

    fun serCallback4(cb: (device: BluetoothDevice, gattCallback: LocalGattCallback) -> Unit) { openGattCallback = cb }
    fun serCallback5(cb: () -> Boolean) { hasBluetoothLE = cb }
    fun serCallback8(cb: () -> Boolean) { hasLocationPermissions = cb }

    fun getServicesDeviceSelected(): List<BluetoothGattService> {
        return findGattOrNull(deviceSelected)?.services ?: listOf()
    }

    fun setBluetoothAdapter(mBluetoothAdapter: BluetoothAdapter?) {
        this.bluetoothAdapter = mBluetoothAdapter
    }

    fun validationBLE() {
        if (hasBluetoothLE?.invoke() == false) {
            viewModelScope.launch {
                _unsupportedBLE.emit(Unit)
            }
            return
        }


        if (mScanning) return
        if (bluetoothAdapter == null || bluetoothAdapter?.isEnabled == false) {
            viewModelScope.launch {
                _requestBluetoothEnable.emit(Unit)
            }
        } else {
            validationPermissions()
        }
    }

    private fun validationPermissions() {
        if (hasLocationPermissions?.invoke() == false) {
            viewModelScope.launch {
                _requestLocationPermission.emit(Unit)
            }
        } else {
            starScan()
        }
    }

    private fun starScan() {
        mDevices.clear()
        stopScan()
        mScanCallback = LocalScanCallback { device ->
            if (mDevices.none { it.address == device.address }) {
                mDevices.add(device)
                viewModelScope.launch {
                    _deviceFound.emit(device)
                }
            }
        }

        val filters: List<ScanFilter> = ArrayList()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        mBluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        mBluetoothLeScanner?.startScan(filters, settings, mScanCallback)

        mScanning = true

        viewModelScope.launch {
            delay(SCAN_PERIOD)
            stopScan()
        }
    }

    private fun stopScan() {
        if (mScanning && bluetoothAdapter != null && bluetoothAdapter?.isEnabled == true && mBluetoothLeScanner != null) {
            mBluetoothLeScanner?.stopScan(mScanCallback)
            viewModelScope.launch {
                _scanCompleted.emit(Unit)
            }
        }
        mScanCallback = null
        mScanning = false
        mBluetoothLeScanner = null

    }

    fun getPairsDeviceAndGatt(): List<Pair<BluetoothDevice, BluetoothGatt?>> {
        val data = arrayListOf<Pair<BluetoothDevice, BluetoothGatt?>>()
        mDevices.forEach { device ->
            data.add(getPairDeviceAndGatt(device))
        }
        return data
    }

    fun getPairDeviceAndGatt(device: BluetoothDevice): Pair<BluetoothDevice, BluetoothGatt?> {
        val gatt = mGatts.find { gatt ->  gatt.device.address == device.address }
        return Pair(device, gatt)
    }

    private fun connectGatt(gatt: BluetoothGatt) {
        if (mGatts.none { it.device.address ==  gatt.device.address }) {
            mGatts.add(gatt)
            viewModelScope.launch {
                _mGattsSelected.emit(null)
            }
        }
        viewModelScope.launch {
            _updateAdapter.emit(Unit)
        }
    }

    fun searchGattToDisconnect(device: BluetoothDevice) {
        val index = mGatts.indexOfFirst { it.device.address == device.address }
        if (index >= 0) {
            deviceSelected = null
            viewModelScope.launch {
                _mGattsSelected.emit(null)
            }
            mGatts[index].disconnect()
            mGatts.removeAt(index)
        }
    }

    private fun gattDisconnected(device: BluetoothDevice) {
        val index = mGatts.indexOfFirst { it.device.address == device.address }
        if (index >= 0) {
            deviceSelected = null
            viewModelScope.launch {
                _mGattsSelected.emit(null)
            }
            mGatts.removeAt(index)
        }
        viewModelScope.launch {
            _updateAdapter.emit(Unit)
        }
    }

    private fun servicesFound(address: String) {
        print("")
    }

    private fun characteristicRead(characteristic: BluetoothGattCharacteristic) {
        viewModelScope.launch {
            _mCharacteristicUpdate.emit(characteristic)
        }
    }

    fun onDisconnectAllGatts() {
        mGatts.forEach {
            it.disconnect()
        }
    }

    fun connectDevice(device: BluetoothDevice) {
        openGattCallback?.invoke(device,
            LocalGattCallback(
                cbConnected = ::connectGatt,
                cbDisconnected = { gattDisconnected(device) },
                cbServicesFound = ::servicesFound,
                cbCharacteristicRead = ::characteristicRead
            )
        )
    }

    fun secondFragment(device: BluetoothDevice) {
        deviceSelected = device
        val gatt = findGattOrNull(device) ?: return
        viewModelScope.launch {
            _mGattsSelected.emit(gatt)
            _goToFragment.emit(1)
        }
    }

    private fun findGattOrNull(device: BluetoothDevice?): BluetoothGatt? {
        device ?: return null
        return mGatts.find { it.device.address == device.address }
    }

    fun selectService(service: BluetoothGattService) {
        viewModelScope.launch {
            _mServiceSelected.emit(service)
            _goToFragment.emit(2)
        }
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        val gatt = findGattOrNull(deviceSelected) ?: return

        if (!gatt.readCharacteristic(characteristic)) {
            viewModelScope.launch {
                _unableToReadCharacteristic.emit(Unit)
            }
        }
    }
}
