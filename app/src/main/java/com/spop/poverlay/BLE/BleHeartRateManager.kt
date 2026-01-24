package com.spop.poverlay.BLE

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.*

class BleHeartRateManager(private val context: Context) {

    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _connectionState = MutableStateFlow(BluetoothProfile.STATE_DISCONNECTED)
    val connectionState: StateFlow<Int> = _connectionState.asStateFlow()

    private var bluetoothGatt: BluetoothGatt? = null
    private var deviceAddress: String? = null

    private val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    private val HEART_RATE_MEASUREMENT_CHAR_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    private val CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // BLE Server / Advertiser members
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var bluetoothGattServer: BluetoothGattServer? = null
    private val registeredDevices = mutableSetOf<BluetoothDevice>()

    init {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
        bluetoothLeAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        if (address.isBlank()) return
        if (deviceAddress == address && _connectionState.value == BluetoothProfile.STATE_CONNECTED) return

        disconnect()

        deviceAddress = address
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
             Timber.e("Bluetooth not enabled")
             return
        }

        val device = adapter.getRemoteDevice(address)
        bluetoothGatt = device.connectGatt(context, true, gattCallback)

        startAdvertising()
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _heartRate.value = 0
        _connectionState.value = BluetoothProfile.STATE_DISCONNECTED

        stopAdvertising()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            _connectionState.value = newState
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Timber.i("Connected to GATT server.")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Timber.i("Disconnected from GATT server.")
                _heartRate.value = 0
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(HEART_RATE_SERVICE_UUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true)
                        val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                }
            } else {
                Timber.w("onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (HEART_RATE_MEASUREMENT_CHAR_UUID == characteristic.uuid) {
                 val flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                 val format = if ((flags and 0x01) != 0) {
                    BluetoothGattCharacteristic.FORMAT_UINT16
                } else {
                    BluetoothGattCharacteristic.FORMAT_UINT8
                }
                val heartRateValue = characteristic.getIntValue(format, 1) ?: 0
                _heartRate.value = heartRateValue
                broadcastHeartRate(heartRateValue)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertising() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) return

        setupGattServer()

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(android.os.ParcelUuid(HEART_RATE_SERVICE_UUID))
            .build()

        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    @SuppressLint("MissingPermission")
    private fun stopAdvertising() {
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        bluetoothGattServer?.close()
        bluetoothGattServer = null
        registeredDevices.clear()
    }

    @SuppressLint("MissingPermission")
    private fun setupGattServer() {
        val service = BluetoothGattService(HEART_RATE_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val heartRateMeasurement = BluetoothGattCharacteristic(
            HEART_RATE_MEASUREMENT_CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        val configDescriptor = BluetoothGattDescriptor(
            CLIENT_CHARACTERISTIC_CONFIG_UUID,
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        heartRateMeasurement.addDescriptor(configDescriptor)

        service.addCharacteristic(heartRateMeasurement)

        bluetoothGattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
        bluetoothGattServer?.addService(service)
    }

    @SuppressLint("MissingPermission")
    private fun broadcastHeartRate(value: Int) {
        val characteristic = bluetoothGattServer?.getService(HEART_RATE_SERVICE_UUID)
            ?.getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID) ?: return

        // Format: [Flags (uint8), Heart Rate (uint8)] - assuming HR fits in uint8 for simplicity
        val data = ByteArray(2)
        data[0] = 0x00 // Flags: UINT8 format, no sensor contact info
        data[1] = (value and 0xFF).toByte()
        characteristic.value = data

        for (device in registeredDevices) {
            bluetoothGattServer?.notifyCharacteristicChanged(device, characteristic, false)
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Timber.i("HR Advertising started successfully")
        }
        override fun onStartFailure(errorCode: Int) {
            Timber.e("HR Advertising failed: $errorCode")
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                registeredDevices.add(device)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                registeredDevices.remove(device)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWriteRequest(
            device: BluetoothDevice, requestId: Int, descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray
        ) {
            if (CLIENT_CHARACTERISTIC_CONFIG_UUID == descriptor.uuid) {
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    registeredDevices.add(device)
                }
            }
            if (responseNeeded) {
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }
        }
    }
}
