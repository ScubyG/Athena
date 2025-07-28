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

package com.kin.athena.service.utils.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.kin.athena.R
import com.kin.athena.service.vpn.network.util.NetworkConstants
import com.kin.athena.presentation.components.PermissionModal
import com.kin.athena.service.vpn.service.VpnConnectionClient
import kotlinx.coroutines.launch

object VpnManager {
    fun start(context: Context) {
        startVpnService(context)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PermissionRequester(
        context: Context,
        isVpnRequestPermission: Boolean,
        onPermissionUpdate: (Boolean) -> Unit
    ) {
        val scope = rememberCoroutineScope()

        val sheetState = rememberModalBottomSheetState()
        val showBottomSheet = remember { mutableStateOf(false) }
        val askForPermission = remember { mutableStateOf(false) }
        val vpnPermissionIntent = VpnService.prepare(context)

        fun onEnd(granted: Boolean) {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onPermissionUpdate(granted)
                }
            }
        }

        if (isVpnRequestPermission) {
            PermissionModal(
                permissionName = stringResource(id = R.string.vpn_permission),
                permissionDescription = stringResource(id = R.string.vpn_permission_description),
                onPermissionRequest = {
                    askForPermission.value = true
                    showBottomSheet.value = false
                },
                onDismiss = { onEnd(false) },
                sheetState = sheetState
            )
        }

        val vpnPermissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            onEnd(result.resultCode == Activity.RESULT_OK)
        }

        if (askForPermission.value) {
            vpnPermissionLauncher.launch(vpnPermissionIntent)
            askForPermission.value = false
        }
    }

    fun permissionChecker(context: Context): Boolean {
        return VpnService.prepare(context) == null
    }

    private fun startVpnService(context: Context) {
        val vpnServiceIntent = Intent(context, VpnConnectionClient::class.java).apply {
            action = NetworkConstants.ACTION_START_VPN
        }
        context.startService(vpnServiceIntent)
    }

    fun stop(context: Context) {
        val stopVpnServiceIntent = Intent(context, VpnConnectionClient::class.java).apply {
            action = NetworkConstants.ACTION_STOP_VPN
        }
        context.startService(stopVpnServiceIntent)
    }
}
