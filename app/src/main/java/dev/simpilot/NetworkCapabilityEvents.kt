package dev.simpilot

import android.annotation.SuppressLint
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkSpecifier
import android.net.TransportInfo
import android.os.Build
import android.os.ext.SdkExtensions
import java.util.concurrent.ConcurrentHashMap

/** Ignore estimates of bandwidth; retain public routing, signal and validation information. */
internal class NetworkCapabilityEvents {
    private data class State(
        val capabilities: Set<Int>,
        val transports: Long,
        val signal: Int,
        val specifier: NetworkSpecifier?,
        val owner: Int,
        val subscriptions: Set<Int>,
        val transportInfo: TransportInfo?,
        val enterprises: Set<Int>,
    )

    private val last = ConcurrentHashMap<Network, State>()

    // Lint also applies the extension selector's IntDef to the returned extension revision.
    // Revision 12 is a U extension level, not an Android VERSION_CODES value.
    @SuppressLint("WrongConstant")
    fun changed(network: Network, capabilities: NetworkCapabilities): Boolean {
        // These are the complete transport ranges of the supported Android releases. Avoid
        // querying a newer transport ID on older frameworks, and never modify callback data.
        val lastTransport =
            when {
                Build.VERSION.SDK_INT >= 35 -> NetworkCapabilities.TRANSPORT_SATELLITE
                Build.VERSION.SDK_INT >= 34 -> NetworkCapabilities.TRANSPORT_THREAD
                else -> NetworkCapabilities.TRANSPORT_USB
            }
        var transports = 0L
        for (transport in 0..lastTransport) {
            if (capabilities.hasTransport(transport)) transports = transports or (1L shl transport)
        }
        val state =
            State(
                capabilities.capabilities.toSet(),
                transports,
                capabilities.signalStrength,
                capabilities.networkSpecifier,
                capabilities.ownerUid,
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                        SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >=
                            12
                )
                    capabilities.subscriptionIds.toSet()
                else emptySet(),
                capabilities.transportInfo,
                if (Build.VERSION.SDK_INT >= 33) capabilities.enterpriseIds.toSet() else emptySet(),
            )
        return last.put(network, state) != state
    }

    fun lost(network: Network) {
        last.remove(network)
    }
}
