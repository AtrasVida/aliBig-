package co.atrasvida.alibig

class AliBigDataModel(
    var accel_x: Float,
    var accel_y: Float,
    var accel_z: Float,
    var gyro_x: Float,
    var gyro_y: Float,
    var gyro_z: Float,
    var gyro_timestamp: Long,
    var gps_lat: Double,
    var gps_lon: Double,
    var gps_altitude: Double,
    var gps_speed: Float,
    var magnetic_x: Float,
    var magnetic_y: Float,
    var magnetic_z: Float,
    var orient_azmith: Float,
    var orient_pitch: Float,
    var orient_roll: Float,
    var proximity: Float,
    var light: Float,
    var pedometer_dis: Float,
    var pedometer_simp: Float,
    var ble_list: ArrayList<BleModel>,
    var wifi_list: ArrayList<WifiModel>
)

data class BleModel(
    var address: String,
    var name: String,
    var macAddress: String,
    var rssi: Int,
    var type: Int
)

data class WifiModel(
    var address: String,
    var name: String,
    var level: Int
    //,var capabilities: String
)