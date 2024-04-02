package com.example.biometricauth

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.biometricauth.BiometricPromptManager.*
import kotlinx.coroutines.launch
class MainActivity : AppCompatActivity() {
    private val promptManager by lazy {
        BiometricPromptManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.auth)
        button.setOnClickListener {
            promptManager.showBiometricPrompt("sample prompt","sample prompt description")
        }


        val enrollLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
            println("Activity is $it")
        }

        val textView: TextView = findViewById(R.id.txt)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                promptManager.promptResult.collect { result ->
                    result?.let {
                        when(result){
                            is BiometricResult.AuthenticationError -> {
                                textView.text = "Authentication error: ${result.error}"
                            }
                            BiometricResult.AuthenticationFailed -> {
                                textView.text = "Authentication Failed"
                            }
                            BiometricResult.AuthenticationNotSet -> {
                                textView.text = "Authentication not set"

                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                        putExtra(
                                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                                        )
                                    }
                                    enrollLauncher.launch(enrollIntent)
                                }
                            }
                            BiometricResult.AuthenticationSuccess -> {
                                textView.text = "Authentication Success"
                            }
                            BiometricResult.FeatureUnavailable -> {
                                textView.text = "Feature Unavailable"
                            }
                            BiometricResult.HardwareUnavailable -> {
                                textView.text = "Hardware Unavailable"
                            }
                        }
                    }
                }
            }
        }

    }
}