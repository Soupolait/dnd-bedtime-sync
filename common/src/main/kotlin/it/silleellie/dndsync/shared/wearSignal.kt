package it.silleellie.dndsync.shared

class WearSignal(dndState: Int) : java.io.Serializable {
    var dndState: Int? = null

    init {
        // DnD disabled:
        // 0 = INTERRUPTION_FILTER_UNKNOWN
        // 1 = INTERRUPTION_FILTER_ALL

        // DnD enabled:
        // 2 = INTERRUPTION_FILTER_PRIORITY
        // 3 = INTERRUPTION_FILTER_NONE (no notification passes)
        // 4 = INTERRUPTION_FILTER_ALARMS

        if (0 <= dndState && dndState <= 4) {
            this.dndState = dndState
        }
    }
}