package com.example.itracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    data class User(var email: String, var senha: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val users = findViewById<EditText>(R.id.editTextUsername)
        val password = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)

        buttonLogin.setOnClickListener {

            val user = User(users.text.toString() , password.text.toString())

            val ( _ , _ , result ) = "http://192.168.0.100:5003/loginAndroid".httpPost()
                .jsonBody(Gson().toJson(user).toString())
                .responseString()

            val array = Gson().toJson(result)
            val valorJson = JSONObject(array)

            val id = valorJson["value"]

            if (id == "erro"){

                Toast.makeText(this, "Login Inválido", Toast.LENGTH_SHORT).show()

            } else {

                Toast.makeText(this, "Bem-vindo", Toast.LENGTH_SHORT).show()
                val segundaTela = Intent(this, Maps::class.java)
                segundaTela.putExtra("idDoMotorista", id as String)
                startActivity(segundaTela)
            }
        }
    }
}