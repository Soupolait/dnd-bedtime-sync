package it.silleellie.dndsync

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

class MainFragment : PreferenceFragmentCompat() {
    private var dndPref: Preference? = null

    public override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        dndPref = findPreference("dnd_permission_key")
        checkNotNull(dndPref)

        // Add listener to power save key so that it is dependant from the "dnd_as_bedtime_key"
        // and "bedtime_sync_key" in logical OR (it should be enabled when one of the two is true)
        val dndAsBedtime: SwitchPreferenceCompat = findPreference("dnd_as_bedtime_key")
        val bedtimeSync: SwitchPreferenceCompat = findPreference("bedtime_sync_key")
        val powerSave: SwitchPreferenceCompat = findPreference("power_save_key")

        checkNotNull(dndAsBedtime)
        checkNotNull(bedtimeSync)
        checkNotNull(powerSave)

        // right at the start of app we should check if the powersave toggle should be enabled
        if (dndAsBedtime.isChecked() || bedtimeSync.isChecked()) {
            powerSave.setEnabled(true)
        }

        dndAsBedtime.setOnPreferenceChangeListener({ preference, newValue ->
            if (newValue as Boolean) {
                powerSave.setEnabled(true)
            } else if (!bedtimeSync.isChecked()) {
                powerSave.setEnabled(false)
            }
            true
        })

        bedtimeSync.setOnPreferenceChangeListener({ preference, newValue ->
            if (newValue as Boolean) {
                powerSave.setEnabled(true)
            } else if (!dndAsBedtime.isChecked()) {
                powerSave.setEnabled(false)
            }
            true
        })

        dndPref.setOnPreferenceClickListener({ preference ->
            if (!checkDNDPermission()) {
                openDNDPermissionRequest()
            } else {
                Toast.makeText(getContext(), "DND-Bedtime Permission allowed", Toast.LENGTH_SHORT).show()
            }
            true
        })

        checkDNDPermission()
    }

    private fun checkDNDPermission(): Boolean {
        val mNotificationManager: NotificationManager =
            getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val allowed: Boolean = mNotificationManager.isNotificationPolicyAccessGranted()
        if (allowed) {
            dndPref.setSummary(R.string.dnd_permission_allowed)
        } else {
            dndPref.setSummary(R.string.dnd_permission_not_allowed)
        }
        return allowed
    }

    private fun openDNDPermissionRequest() {
        val intent: Intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }
}