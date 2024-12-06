package ru.barinov.protected_enter.fingerprint

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import ru.barinov.protected_enter.R

class BiometricEnterHelper(private val context: Context) {

    private val bioManager = BiometricManager.from(context)


    fun startFingerprintAuth(activity: FragmentActivity, onSuccess: () -> Unit, onError: () -> Unit){
        if(checkFingerprint()) {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Биометрическая аутентификация")
                .setDescription("Используйте отпечаток пальца или камеру для аутентификации")
                .setNegativeButtonText(context.getString(android.R.string.cancel))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(activity, executor, object:  BiometricPrompt.AuthenticationCallback(){
               override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                   super.onAuthenticationSucceeded(result)
                   onSuccess()
               }

               override fun onAuthenticationFailed() {
                   super.onAuthenticationFailed()
                   onError()
               }

               override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                   super.onAuthenticationError(errorCode, errString)
                   onError()
               }
           })
            biometricPrompt.authenticate(promptInfo)
        } else onError()
    }

    private fun checkFingerprint(): Boolean =
      bioManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
}
