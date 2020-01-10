package com.example.googlemapsgoogleplaces

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.googlemapsgoogleplaces.databinding.ActivityMainBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    val ERROR_DIALOG_REQUEST = 90

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        if (isServicesOK()) {
            init()
        }
    }

    private fun init() {

        binding.btnMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }


    fun isServicesOK(): Boolean {
        Log.e(TAG, "isServicesOK: checking google services version")
        val available =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (available == ConnectionResult.SUCCESS) { //everything is fine and the user can make map requests
            Log.e(TAG, "isServicesOK: Google Play Services is working")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) { //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it")
            val dialog = GoogleApiAvailability.getInstance()
                .getErrorDialog(this, available, ERROR_DIALOG_REQUEST)
            dialog.show()
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
        }
        return false
    }

}
