package it.silleellie.dndsync

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class MainFragment : PreferenceFragmentCompat() {
    private var dndPref: Preference? = null
    private var secureSettingsPref: Preference? = null

    public override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        dndPref = findPreference("dnd_permission_key")
        secureSettingsPref = findPreference("secure_settings_permission_key")

        dndPref.setOnPreferenceClickListener({ preference ->
            if (!checkDNDPermission()) {
                Toast.makeText(
                    getContext(),
                    "Follow the instructions to grant the permission via ADB!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            true
        })

        secureSettingsPref.setOnPreferenceClickListener({ preference ->
            if (!checkSecureSettingsPermission(getContext())) {
                Toast.makeText(
                    getContext(),
                    "Follow the instructions to grant the permission via ADB!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            true
        })

        checkDNDPermission()
        checkSecureSettingsPermission(getContext())
    }

    private fun checkDNDPermission(): Boolean {
        val mNotificationManager: NotificationManager =
            getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val allowed: Boolean = mNotificationManager.isNotificationPolicyAccessGranted()
        if (allowed) {
            dndPref.setSummary(R.string.granted)
        } else {
            dndPref.setSummary(R.string.denied)
        }
        return allowed
    }

    private fun checkSecureSettingsPermission(context: Context?): Boolean {
        val allowed: Boolean
        allowed = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_SECURE_SETTINGS
        ) === PackageManager.PERMISSION_GRANTED
        if (allowed) {
            secureSettingsPref.setSummary(R.string.granted)
        } else {
            secureSettingsPref.setSummary(R.string.denied)
        }
        return allowed
    }
}