package com.example.itracker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPut
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import org.json.JSONObject
import java.util.*

class Maps : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    data class Coordenadas(var latitude: String, var longitude: String , var id : String)

    data class GetId(var idColeta: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val ss = intent.getStringExtra("idDoMotorista").toString()

        val btnCompartilhar = findViewById<Button>(R.id.btnCompartilhar)

        btnCompartilhar.setOnClickListener {

            Toast.makeText(this, "Compartilhando", Toast.LENGTH_SHORT).show()
            TimerLocation(ss)
        }

        val id = ss

        val (_, _, result) = "http://192.168.0.100:5003/getColetasAndroid/${id}".httpGet()
            .jsonBody(Gson().toJson(id).toString())
            .responseString()

        val array = Gson().toJson(result)
        val valorJson = JSONObject(array)

        val response = valorJson["value"]

        val view = findViewById<TextView>(R.id.textViewResponse)
        view.text = response.toString()

    }

    fun TimerLocation(a: String) {
        Timer().scheduleAtFixedRate( object : TimerTask() {
            override fun run() {
                GetCurrentLocation(a)
            }
        }, 0, 3000)
    }

    private fun GetCurrentLocation(a: String) {
        if(checkPermission()) {

            if(isLocationEnabled()) {

                if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION
                    )!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )!= PackageManager.PERMISSION_GRANTED){
                    requestPermission()
                    return
                }

                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task->
                    val location: Location?=task.result
                    if(location==null) {
                        Toast.makeText(this,"Sem conexão", Toast.LENGTH_SHORT).show()

                    } else {

                        val coordenadas = Coordenadas(location.latitude.toString() , location.longitude.toString(), a)

                        val (_, _, result) = "http://192.168.0.100:5003/updateAndroid".httpPut()
                            .jsonBody(Gson().toJson(coordenadas).toString())
                            .responseString()

                        println(result)
                    }
                }

            } else {
                Toast.makeText(this,"Ative sua Localização", Toast.LENGTH_SHORT).show()
                val intent= Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {

            requestPermission()
        }
    }

    private fun isLocationEnabled():Boolean {
        val locationManager:LocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermission() {

        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACESS_LOCATION
        )
    }

    companion object {

        private const val PERMISSION_REQUEST_ACESS_LOCATION=100
    }

    private fun checkPermission() : Boolean {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
        {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode== PERMISSION_REQUEST_ACESS_LOCATION) {

            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(applicationContext,"Acesso permitido", Toast.LENGTH_SHORT).show()
                val responseId = intent.getStringExtra("idDoMotorista").toString()
                GetCurrentLocation(responseId)

            } else {

                Toast.makeText(applicationContext,"Acesso negado", Toast.LENGTH_SHORT).show()

            }
        }
    }
}