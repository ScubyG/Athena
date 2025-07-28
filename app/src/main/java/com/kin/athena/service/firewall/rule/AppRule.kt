/*
 * Copyright (C) 2025 Vexzure
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.kin.athena.service.firewall.rule

import com.kin.athena.core.logging.Logger
import com.kin.athena.data.service.ConnectionStateManager
import com.kin.athena.data.service.NetworkManager
import com.kin.athena.domain.model.Application
import com.kin.athena.domain.usecase.application.ApplicationUseCases
import com.kin.athena.domain.usecase.log.LogUseCases
import com.kin.athena.service.firewall.model.FireWallModel
import com.kin.athena.service.firewall.model.FirewallResult
import com.kin.athena.service.firewall.utils.ConnectivityUtils
import com.kin.athena.service.vpn.network.transport.dns.DNSModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppRule @Inject constructor(
    private val applicationUseCases: ApplicationUseCases,
    private val networkConnectionStateManager: ConnectionStateManager,
    private val connectionUtils: ConnectivityUtils,
) : FirewallRule {
    private var wifiBlockedUids: IntArray = IntArray(0)
    private var cellularBlockedUids: IntArray = IntArray(0)

    init {
        observePackages(updatedApplication = null)
    }

    fun observePackages(updatedApplication: Application?) {
        CoroutineScope(Dispatchers.IO).launch {
            val packagesList = applicationUseCases.getApplications.execute()
            packagesList.fold(
                ifSuccess = { applications ->
                    val wifiBlocked = applications.filter { !it.internetAccess }.map { it.uid }.toIntArray()
                    val cellularBlocked = applications.filter { !it.cellularAccess }.map { it.uid }.toIntArray()

                    wifiBlockedUids = wifiBlocked
                    cellularBlockedUids = cellularBlocked
                }
            )
        }
    }

    override fun check(
        packet: FireWallModel,
        dnsModel: DNSModel?,
        logUseCases: LogUseCases,
        result: FirewallResult
    ): FirewallResult {
        if (packet.uid == 0) {
            packet.uid = connectionUtils.getConnectionOwnerUid(packet)
        } else {
            packet.shouldLog = false
        }

        val isBlocked = when (networkConnectionStateManager.getCurrentConnectionType()) {
            NetworkManager.ConnectionType.WIFI -> { packet.uid in wifiBlockedUids }
            NetworkManager.ConnectionType.MOBILE -> {
                packet.networkType = NetworkManager.ConnectionType.MOBILE
                packet.uid in cellularBlockedUids
            }
            NetworkManager.ConnectionType.NONE -> false
        }

        return if (isBlocked) FirewallResult.DROP else FirewallResult.ACCEPT
    }
}