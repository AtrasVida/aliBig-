package co.atrasvida.alibig

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {
    private var sensorManager: SensorManager? = null
    var ax: Float = 0.0f
    var ay: Float = 0.0f
    var az: Float = 0.0f // these are the acceleration in x,y and z axis


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ///////////////////////////////////////////////////
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager?.registerListener(
            object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                        var x = event.values[0];
                        var y = event.values[1];
                        var z = event.values[2];
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
                    val latitude = "Latitude: " + loc?.latitude
                    Log.v("TAG", latitude)

                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

                override fun onProviderEnabled(provider: String?) {}

                override fun onProviderDisabled(provider: String?) {}
            }
        )
        ////////////////////////////////////////
    }


}