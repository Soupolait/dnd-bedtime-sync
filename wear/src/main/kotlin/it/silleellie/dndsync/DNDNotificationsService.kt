package it.silleellie.dndsync


import android.service.notification.NotificationListenerService
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import org.apache.commons.lang3.SerializationUtils
import it.silleellie.dndsync.shared.WearSignal
import java.util.concurrent.ExecutionException

class DNDNotificationService : NotificationListenerService() {
    public override fun onListenerConnected() {
        Log.d(it.silleellie.dndsync.DNDNotificationService.Companion.TAG, "listener connected")
        it.silleellie.dndsync.DNDNotificationService.Companion.running = true

        //TODO enable/disable service based on app setting to save battery
//        // We don't want to run a background service so disable and stop it
//        // to avoid running this service in the background
//        disableServiceComponent();
//        Log.i(TAG, "Disabling service");
//
//        try {
//            stopSelf();
//        } catch(SecurityException e) {
//            Log.e(TAG, "Failed to stop service");
//        }
    }

    //    private void disableServiceComponent() {
    //        PackageManager p = getPackageManager();
    //        ComponentName componentName = new ComponentName(this, DNDNotificationService.class);
    //        p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    //    }
    public override fun onListenerDisconnected() {
        Log.d(it.silleellie.dndsync.DNDNotificationService.Companion.TAG, "listener disconnected")
        it.silleellie.dndsync.DNDNotificationService.Companion.running = false
    }


    public override fun onInterruptionFilterChanged(interruptionFilter: Int) {
        Log.d(
            it.silleellie.dndsync.DNDNotificationService.Companion.TAG,
            "interruption filter changed to " + interruptionFilter
        )

        // preferences are now stored on the mobile app, so we send the signal nonetheless
        // and if the user ticked the relative option then it is synced to the phone
        java.lang.Thread(java.lang.Runnable { sendDNDSync(WearSignal(interruptionFilter)) }).start()
    }

    private fun sendDNDSync(wearSignal: WearSignal?) {
        // https://developer.android.com/training/wearables/data/messages

        // search nodes for sync

        var capabilityInfo: CapabilityInfo? = null
        try {
            capabilityInfo = Tasks.await(
                Wearable.getCapabilityClient(this).getCapability(
                    it.silleellie.dndsync.DNDNotificationService.Companion.DND_SYNC_CAPABILITY_NAME,
                    CapabilityClient.FILTER_REACHABLE
                )
            )
        } catch (e: ExecutionException) {
            e.printStackTrace()
            Log.e(
                it.silleellie.dndsync.DNDNotificationService.Companion.TAG,
                "execution error while searching nodes",
                e
            )
            return
        } catch (e: java.lang.InterruptedException) {
            e.printStackTrace()
            Log.e(
                it.silleellie.dndsync.DNDNotificationService.Companion.TAG,
                "interruption error while searching nodes",
                e
            )
            return
        }

        // send request to all reachable nodes
        // capabilityInfo has the reachable nodes with the dnd sync capability
        val connectedNodes: MutableSet<Node> = capabilityInfo.getNodes()
        if (connectedNodes.isEmpty()) {
            // Unable to retrieve node with transcription capability
            Log.d(
                it.silleellie.dndsync.DNDNotificationService.Companion.TAG,
                "Unable to retrieve node with sync capability!"
            )
        } else {
            for (node in connectedNodes) {
                if (node.isNearby()) {
                    val data: ByteArray? = SerializationUtils.serialize(wearSignal)
                    val sendTask: Task<Int?> =
                        Wearable.getMessageClient(this).sendMessage(
                            node.getId(),
                            it.silleellie.dndsync.DNDNotificationService.Companion.DND_SYNC_MESSAGE_PATH,
                            data
                        )

                    sendTask.addOnSuccessListener({ integer ->
                        Log.d(
                            it.silleellie.dndsync.DNDNotificationService.Companion.TAG,
                            "send successful! Receiver node id: " + node.getId()
                        )
                    }
                    )

                    sendTask.addOnFailureListener({ e ->
                        Log.d(
                            it.silleellie.dndsync.DNDNotificationService.Companion.TAG,
                            "send failed! Receiver node id: " + node.getId()
                        )
                    }
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "DNDNotificationService"
        private const val DND_SYNC_CAPABILITY_NAME = "dnd_sync"
        private const val DND_SYNC_MESSAGE_PATH = "/wear-dnd-sync"

        var running: Boolean = false
    }
}