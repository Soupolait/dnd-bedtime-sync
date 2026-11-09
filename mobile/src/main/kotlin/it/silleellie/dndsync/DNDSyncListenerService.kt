package it.silleellie.dndsync

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.apache.commons.lang3.SerializationUtils
import it.silleellie.dndsync.shared.WearSignal

class DNDSyncListenerService : WearableListenerService() {
    public override fun onMessageReceived(@NonNull messageEvent: MessageEvent) {
        if (messageEvent.getPath()
                .equalsIgnoreCase(it.silleellie.dndsync.DNDSyncListenerService.Companion.DND_SYNC_MESSAGE_PATH)
        ) {
            Log.d(
                it.silleellie.dndsync.DNDSyncListenerService.Companion.TAG,
                "received path: " + it.silleellie.dndsync.DNDSyncListenerService.Companion.DND_SYNC_MESSAGE_PATH
            )

            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

            val data: ByteArray? = messageEvent.getData()
            val wearSignal: WearSignal = SerializationUtils.deserialize(data)
            val dndStateWear: Int = wearSignal.dndState

            Log.d(it.silleellie.dndsync.DNDSyncListenerService.Companion.TAG, "dndStateWear: " + dndStateWear)

            // get dnd state
            val mNotificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val currentDndState: Int = mNotificationManager.getCurrentInterruptionFilter()

            Log.d(it.silleellie.dndsync.DNDSyncListenerService.Companion.TAG, "currentDndState: " + currentDndState)
            if (currentDndState < 0 || currentDndState > 4) {
                Log.d(
                    it.silleellie.dndsync.DNDSyncListenerService.Companion.TAG,
                    "Current DND state it's weird, should be in range [0,4]"
                )
            }

            val shouldSync: Boolean = prefs.getBoolean("watch_dnd_sync_key", false)

            if (currentDndState != dndStateWear && shouldSync) {
                Log.d(
                    it.silleellie.dndsync.DNDSyncListenerService.Companion.TAG,
                    "currentDndState != dndStateWear: " + currentDndState + " != " + dndStateWear
                )
                if (mNotificationManager.isNotificationPolicyAccessGranted()) {
                    mNotificationManager.setInterruptionFilter(dndStateWear)
                    Log.d(it.silleellie.dndsync.DNDSyncListenerService.Companion.TAG, "DND set to " + dndStateWear)
                } else {
                    Log.d(
                        it.silleellie.dndsync.DNDSyncListenerService.Companion.TAG,
                        "attempting to set DND but access not granted"
                    )
                }
            }
        } else {
            super.onMessageReceived(messageEvent)
        }
    }

    companion object {
        private const val TAG = "DNDSyncListenerService"
        private const val DND_SYNC_MESSAGE_PATH = "/wear-dnd-sync"
    }
}