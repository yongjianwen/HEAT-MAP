package yong.jianwen.heatmap.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import yong.jianwen.heatmap.MainActivity
import yong.jianwen.heatmap.R
import yong.jianwen.heatmap.data.AppDataContainer
import yong.jianwen.heatmap.data.entity.TrackPoint
import yong.jianwen.heatmap.getCurrentDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LocationService : Service() {

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    val context: Context = this
    var startId = -1

    private lateinit var locationManager: LocationManager
    var locationByFused: Location? = null

    var a = 0.0
    var b = 0.0
    var c = 0.0

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    lateinit var fusedLocationListener: LocationListener

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        val APP_PREFERENCE_NAME = "heat_map_preferences"
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = APP_PREFERENCE_NAME
        )
        var temp: Long = 1

        override fun handleMessage(msg: Message) {
//            val dataStoreRepository = AppDataContainer(context).dataStoreRepository
//            scope.launch {
//                dataStoreRepository.trackSegmentId.first()
//                dataStoreRepository.trackSegmentId.collect { temp = it }
//            }
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            getCurrentLocation { a = it.first; b = it.second }
//            while (true) {
//                try {
////                    Toast.makeText(context, "Test $a $b", Toast.LENGTH_SHORT).show()
//                    Thread.sleep(5000)
//                } catch (e: InterruptedException) {
//                    // Restore interrupt status.
//                    Thread.currentThread().interrupt()
//                }
//            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
//            stopSelf(msg.arg1)
        }

        @SuppressLint("NewApi")
        private fun getCurrentLocation___(onSuccess: (Pair<Double, Double>) -> Unit) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
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
                /*locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )*/
//            return
            }

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val locationRequest = LocationRequest.Builder(5000L)
                .setQuality(LocationRequest.QUALITY_HIGH_ACCURACY)
                .build()
//            } else {
//                TODO("VERSION.SDK_INT < S")
//            }

            fusedLocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationByFused = location
                    a = location.latitude
                    b = location.longitude
                    Toast.makeText(context, "location changed: $a, $b", Toast.LENGTH_SHORT).show()

                    val trackPointRepository = AppDataContainer(context).trackPointRepository
                    //TrackPointRepository(AppDatabase.getDatabase(context).trackPointDao())

                    scope.launch {
                        delay(500)
                        Log.d("TESTSERVICE",
                            AppDataContainer(context).dataStoreRepository.trackSegmentId.first().toString()
                        )
                        trackPointRepository.insert(
                            TrackPoint(
                                id = 0,
                                trackSegmentId = AppDataContainer(context).dataStoreRepository.trackSegmentId.first(),
                                latitude = location.latitude,
                                longitude = location.longitude,
                                elevation = location.altitude.toInt(),
                                time = getCurrentDateTime()
                            )
                        )
                    }
                }
            }

            locationManager.requestLocationUpdates(
                LocationManager.FUSED_PROVIDER,
                0,
                0f,
                fusedLocationListener
            )

            Log.d("Test Service", (locationByFused == null).toString())

            if (locationByFused != null) {
//                val latitude = String.format(null, "%.6f", locationByFused!!.latitude)
//                val longitude = String.format(null, "%.6f", locationByFused!!.longitude)
//                Toast.makeText(context, "$latitude $longitude", Toast.LENGTH_LONG).show()
                val latitude = locationByFused!!.latitude
                val longitude = locationByFused!!.longitude

                onSuccess(Pair(latitude, longitude))
            } else {
//                Toast.makeText(context, "NO LOC", Toast.LENGTH_SHORT).show()
                onSuccess(Pair(-1.0, -1.0))
            }
        }
    }

    override fun onCreate() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        /*HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }*/
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        /*this.startId = startId

        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        // If we get killed, after returning from here, restart
        return START_STICKY*/

        createNotificationChannel()
        getCurrentLocation { a = it.first; b = it.second }
        startForeground(1, Notification.Builder(context, "CHANNEL_ID").build())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        Toast.makeText(this, "Trip Ended", Toast.LENGTH_SHORT).show()
//        stopSelf(startId)
        job.cancel()
        locationManager.removeUpdates(fusedLocationListener)
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            "CHANNEL_ID",
            "Location Service Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    @SuppressLint("NewApi")
    private fun getCurrentLocation(onSuccess: (Pair<Double, Double>) -> Unit) {
        /*val mLocationRequestHighAccuracy = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
        }
        val mLocationRequestHighAccuracy = LocationRequest.Builder(5000L)
            .setQuality(LocationRequest.QUALITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(1000L)
            .build()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
//            mFusedLocationClient.requestLocationUpdates(
//                mLocationRequestHighAccuracy,
//                object : LocationCallback() {
//                    override fun onLocationResult(locationResult: LocationResult) {
//                        val location = locationResult.lastLocation
//                        if (location != null) {
//                            val latitude = location.latitude
//                            val longitude = location.longitude
//                            // Process latitude and longitude as needed
//                        }
//                    }
//                },
//                Looper.myLooper()!!
//            )
        }*/

        Log.d("TEST", "service started")

        fusedLocationListener = object : LocationListener {
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                Log.d("TEST", "onStatusChanged: $status")
            }

            override fun onProviderEnabled(provider: String) {
                Log.d("TEST", "onProviderEnabled")
            }

            override fun onProviderDisabled(provider: String) {
                Log.d("TEST", "onProviderDisabled")
            }

            override fun onLocationChanged(location: Location) {
                locationByFused = location
                Toast.makeText(context, "Test Internet", Toast.LENGTH_SHORT).show()

                val trackPointRepository = AppDataContainer(context).trackPointRepository

                scope.launch {
                    delay(500)
                    trackPointRepository.insert(
                        TrackPoint(
                            id = 0,
                            trackSegmentId = AppDataContainer(context).dataStoreRepository.trackSegmentId.first(),
                            latitude = location.latitude,
                            longitude = location.longitude,
                            elevation = location.altitude.toInt(),
                            time = getCurrentDateTime()
                        )
                    )
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.FUSED_PROVIDER,
            5000L,
            0f,
            fusedLocationListener
        )

//        if (locationByFused != null) {
//            val latitude = locationByFused!!.latitude
//            val longitude = locationByFused!!.longitude
//            onSuccess(Pair(latitude, longitude))
//        } else {
//            onSuccess(Pair(-1.0, -1.0))
//        }
    }

    private fun getNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("Location Service")
            .setContentText("Getting location updates")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        return builder.build()
    }
}
