package co.atrasvida.alibig

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.OrientationEventListener
import android.widget.Space
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private var sensorManager: SensorManager? = null

    var model = AliBigDataModel(
        0.0f,
        0.0f,
        0.0f,

        0.0f,
        0.0f,
        0.0f,
        0L,

        0.0,
        0.0,
        0.0,
        0.0f,

        0.0f,
        0.0f,
        0.0f,

        0.0f,
        0.0f,
        0.0f,

        0.0f,
        0.0f,
        0.0f,
        0.0f,
        arrayListOf(),
        arrayListOf()
    )
    var ax: Float = 0.0f
    var ay: Float = 0.0f
    var az: Float = 0.0f // these are the acceleration in x,y and z axis

    var wifiManager: WifiManager? = null

    val wifiScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }

    private val mDeviceList = ArrayList<String>()
    private var mBluetoothAdapter: BluetoothAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saver.setOnClickListener { onSaveClick() }

        ///////////////////////////////////////////////////
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager!!.startScan()
        if (!success) {
            // scan failure handling
            scanFailure()
        }
        ///////////////////////////////////////////////////

        //val pairedDevices = mBluetoothAdapter.bondedDevices

        //val s: MutableList<String> = ArrayList()
        //for (bt in pairedDevices) s.add(bt.name)

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)

        ///////////////////////////////////////////////////
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager?.registerListener(
            object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                        var x = event.values[0]
                        var y = event.values[1]
                        var z = event.values[2]

                        model.accel_x = x
                        model.accel_y = y
                        model.accel_z = z

                        // if (x != ax || y != ay || z != az) {
                        //     ax = x
                        //     ay = y
                        //     az = z
                        //     Log.i("TAG", "onSensorChanged: ax =$ax az =$ay az =$az ")
                        // }
                    }
                }
            },
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        );

        ////////////////////////////////////////////////////////////////


        var sensorManagermagnetic = getSystemService(SENSOR_SERVICE) as SensorManager
        // Capture magnetic sensor related view elements


        // Register magnetic sensor
        sensorManagermagnetic.registerListener(
            object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        var magneticX = event.values[0]
                        var magneticY = event.values[1]
                        var magneticZ = event.values[2]

                        model.magnetic_x = magneticX
                        model.magnetic_y = magneticY
                        model.magnetic_z = magneticZ
                    }
                }
            },
            sensorManagermagnetic.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_NORMAL
        );

        ////////////////////////////////////////////////////////////////

        val orientationEventListener: OrientationEventListener =
            object : OrientationEventListener(this) {
                override fun onOrientationChanged(orientation: Int) {
                    Log.d("FragmentActivity.TAG", "orientation = $orientation")
                }
            }

        orientationEventListener.enable()
        ////////////////////////////////////////////////////////////////
        var mSensorManager = getSystemService(SENSOR_SERVICE) as (SensorManager)
        var mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent?) {
                try {
                    model.orient_azmith = event?.values?.get(0) ?: 0f
                    model.orient_pitch = event?.values?.get(1) ?: 0f
                    model.orient_roll = event?.values?.get(2) ?: 0f
                } catch (e: Exception) {

                }

            }
        }, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        ////////////////////////////////////////////////////////////////
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 5, 1f, object : LocationListener {
                override fun onLocationChanged(loc: Location?) {
                    val longitude = "Longitude: " + loc?.longitude
                    Log.v("TAG", longitude)
                    val latitude = "Latitude: " + loc?.longitude!!
                    Log.v("TAG", latitude)

                    model.gps_lat = loc.latitude
                    model.gps_lon = loc.longitude
                    model.gps_altitude = loc.altitude
                    model.gps_speed = loc.speed

                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

                override fun onProviderEnabled(provider: String?) {}

                override fun onProviderDisabled(provider: String?) {}
            }
        )
        ////////////////////////////////////////


        var mySensorManager = getSystemService(SENSOR_SERVICE) as (SensorManager)

        var lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor != null) {

            mySensorManager.registerListener(
                lightSensorListener,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            );

        } else {

        }


        var bluetoothManager: BluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as (BluetoothManager)

        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.


        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
            //finish();
            return;
        } else {
            scanLeDevice(true)
        }
        ////////////////////////////////////////
        timerValitate()

        refresh.setOnClickListener {
            model.ble_list = arrayListOf()
            model.wifi_list = arrayListOf()

            scanLeDevice(true)

            val success = wifiManager!!.startScan()
            if (!success) {
                // scan failure handling
                scanFailure()
            }
        }
    }

    private val SCAN_PERIOD: Long = 10000
    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(Runnable {
                mScanning = false
                mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
                invalidateOptionsMenu()
            }, SCAN_PERIOD)
            mScanning = true
            mBluetoothAdapter!!.startLeScan(mLeScanCallback)
        } else {
            mScanning = false
            mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
        }
        invalidateOptionsMenu()
    }

    // Device scan callback.
    private val mLeScanCallback =
        LeScanCallback { device, rssi, scanRecord ->
            runOnUiThread {
                var blm =
                    BleModel(
                        device.address,
                        device.name ?: "",
                        "" + device.bondState,
                        rssi ?: 0,
                        device.type ?: 0
                    )
                var isExist = false
                for (i in 0..model.ble_list.size - 1) {
                    if (blm.address == model.ble_list[i].address) {
                        isExist = true
                        model.ble_list.set(i, blm)
                    }
                }
                if (!isExist)
                    model.ble_list.add(blm)

                print("mo" + model.ble_list.size)

            }
        }
    private var lightSensorListener: SensorEventListener = object : SensorEventListener {

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }

        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                model.light = event.values[0]
            }
        }
    }

    var handler = Handler()
    var timer = Timer()

    fun timerValitate() {

        var handler = Handler()
        var timer = Timer()
        timer.schedule(object : TimerTask() {

            override fun run() {
                val gson = Gson()
                val json: String = arrangeAndFormatJson(gson.toJson(model))
                handler.post {
                    textView.text = json
                    print("asdasdasdsdfghjkljhgfdsasdfghjkl/")
                }
            }
        }, 100, 100)
    }


    var mScanning: Boolean = false


    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()

    }

    fun onSaveClick() {
        var myExternalFile: File =
            File(Environment.getExternalStorageDirectory().path, "bigdel.json")
        File(Environment.getExternalStorageDirectory().path + "/MyFileStorage").mkdirs()
        try {

            val gson = Gson()
            val json: String = gson.toJson(model)

            val fileOutPutStream = FileOutputStream(myExternalFile)
            fileOutPutStream.write(json.toByteArray())
            fileOutPutStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun scanSuccess() {
        val results = wifiManager!!.scanResults
        model.wifi_list = arrayListOf()
        for (result in results) {
            model.wifi_list.add(
                WifiModel(
                    result.BSSID,
                    result.SSID,
                    result.level
                    //,result.capabilities
                )
            )
        }
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager!!.scanResults
        model.wifi_list = arrayListOf()
        for (result in results) {
            model.wifi_list.add(
                WifiModel(
                    result.BSSID,
                    result.SSID,
                    result.level//, result.capabilities
                )
            )
        }
        //... potentially use older scan results ...
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent
                    .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                mDeviceList.add(
                    """
                    ${device.name}
                    ${device.address}
                    """.trimIndent()
                )

                //model.ble_list.add(BleModel(device.address, device.name, "", device.type))

            }
        }
    }

    fun arrangeAndFormatJson(j: String): String {
        var spase = ""
        var result = ""
        for (i in j.indices) {
            when {
                j[i] == '{' -> {
                    spase += "    "
                    result += "{\n$spase"
                }
                j[i] == '}' -> {
                    spase = spase.substring(0, spase.length - 4)
                    result += ",\n$spase}"
                } j[i] == '[' -> {
                    spase += "    "
                    result += "[\n$spase"
                }
                j[i] == ']' -> {
                    spase = spase.substring(0, spase.length - 4)
                    result += ",\n$spase]"
                }
                j[i] == ',' -> {
                    result += ",\n$spase"
                }
                else -> {
                    result += j[i]
                }
            }
        }
        return result;
    }
}