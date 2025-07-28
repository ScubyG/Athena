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

package com.kin.athena.presentation.screens.settings.subSettings.network

import android.content.Intent
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMissedOutgoing
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.filled.CallMissedOutgoing
import androidx.compose.material.icons.filled.MultipleStop
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kin.athena.R
import com.kin.athena.core.utils.extensions.safeNavigate
import com.kin.athena.presentation.navigation.routes.LogRoutes
import com.kin.athena.presentation.screens.settings.components.IconType
import com.kin.athena.presentation.screens.settings.components.IpDialog
import com.kin.athena.presentation.screens.settings.components.SettingType
import com.kin.athena.presentation.screens.settings.components.SettingsBox
import com.kin.athena.presentation.screens.settings.components.SettingsScaffold
import com.kin.athena.presentation.screens.settings.components.settingsContainer
import com.kin.athena.presentation.screens.settings.subSettings.network.viewModel.IpDialogViewModel
import com.kin.athena.presentation.screens.settings.viewModel.SettingsViewModel
import com.kin.athena.service.utils.manager.FirewallManager
import kotlinx.coroutines.runBlocking

@Composable
fun NetworkScreen(
    navController: NavController,
    settings: SettingsViewModel,
) {
    val networkViewModel = hiltViewModel<IpDialogViewModel>().apply {
        updateIpv4DialogText(TextFieldValue(settings.settings.value.ipv4 ?: ""))
        updateIpv6DialogText(TextFieldValue(settings.settings.value.ipv6 ?: ""))
    }

    SettingsScaffold(
        settings = settings,
        title = stringResource(id = R.string.network_title),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        settingsContainer {
            SettingsBox(
                title = stringResource(id = R.string.manage_ips),
                description = stringResource(id = R.string.manage_ips_description),
                icon = IconType.VectorIcon(Icons.Filled.MultipleStop),
                actionType = SettingType.CUSTOM,
                customAction = {
                    navController.safeNavigate(LogRoutes.Ips.route)
                },
            )
            SettingsBox(
                title = stringResource(id = R.string.always_allow_ips),
                description = stringResource(id = R.string.always_allow_ips_description),
                icon = IconType.VectorIcon(Icons.AutoMirrored.Filled.CallMissedOutgoing),
                actionType = SettingType.SWITCH,
                variable = settings.settings.value.allowLocal,
                onSwitchEnabled = { settings.update(settings.settings.value.copy(allowLocal = it)) },
            )
        }
        settingsContainer {
            SettingsBox(
                title = stringResource(R.string.interface_ipv4),
                description = stringResource(R.string.interface_ipv4_description),
                icon = IconType.VectorIcon(Icons.Rounded.ArrowDropUp),
                actionType = SettingType.CUSTOM,
                isEnabled = !(settings.settings.value.useRootMode ?: false),
                customAction = { onExit ->

                    LaunchedEffect(Unit) {
                        networkViewModel.updateIpv4DialogText(
                            TextFieldValue(
                                settings.settings.value.ipv4 ?: ""
                            )
                        )
                    }
                    IpDialog(
                        onExit = {
                            networkViewModel.updateIpv4DialogText(TextFieldValue(settings.settings.value.ipv4?: ""))
                            onExit()
                        },
                        textFieldValue = networkViewModel.dialogIpv4TextField.value,
                        onValueChange = networkViewModel::updateIpv4DialogText,
                        onFinished = {
                            settings.update(settings.settings.value.copy(ipv4 = networkViewModel.dialogIpv4TextField.value.text))
                            onExit()
                        },
                        allowOnlyIPv4 = true,
                    )
                }
            )

            SettingsBox(
                title = stringResource(R.string.interface_ipv6),
                description = stringResource(R.string.interface_ipv6_description),
                icon = IconType.VectorIcon(Icons.Rounded.ArrowDropDown),
                isEnabled = !(settings.settings.value.useRootMode ?: false),
                actionType = SettingType.CUSTOM,
                customAction = { onExit ->
                    LaunchedEffect(Unit) {
                        networkViewModel.updateIpv6DialogText(TextFieldValue(settings.settings.value.ipv6 ?: ""))
                    }

                    IpDialog(
                        onExit = {
                            networkViewModel.updateIpv6DialogText(TextFieldValue(settings.settings.value.ipv6 ?: ""))
                            onExit()
                        },
                        textFieldValue = networkViewModel.dialogIpv6TextField.value,
                        onValueChange = networkViewModel::updateIpv6DialogText,
                        onFinished = {
                            settings.update(settings.settings.value.copy(ipv6 = networkViewModel.dialogIpv6TextField.value.text))
                            onExit()
                        },
                        allowOnlyIPv6 = true,
                    )
                }
            )
        }
        settingsContainer {
            SettingsBox(
                title = stringResource(R.string.block_wifi_when_screen_off),
                description = stringResource(R.string.block_wifi_when_screen_off_description),
                icon = IconType.VectorIcon(Icons.Filled.WifiOff),
                actionType = SettingType.SWITCH,
                isEnabled = settings.settings.value.useRootMode != true,
                variable = settings.settings.value.blockWifiWhenScreenOff,
                onSwitchEnabled = {
                    runBlocking {
                        settings.update(settings.settings.value.copy(blockWifiWhenScreenOff = it)) {
                            networkViewModel.firewallManager.updateScreen(it)
                        }
                    }
                },
            )
            SettingsBox(
                title = stringResource(R.string.block_cellular_when_screen_off),
                description = stringResource(R.string.block_cellular_when_screen_off_description),
                icon = IconType.VectorIcon(Icons.Filled.SignalCellularConnectedNoInternet0Bar),
                actionType = SettingType.SWITCH,
                isEnabled = settings.settings.value.useRootMode != true,
                variable = settings.settings.value.blockCellularWhenScreenOff,
                onSwitchEnabled = {
                    runBlocking {
                        settings.update(settings.settings.value.copy(blockCellularWhenScreenOff = it)) {
                            networkViewModel.firewallManager.updateScreen(it)
                        }
                    }
                },
            )
        }
        settingsContainer {
            SettingsBox(
                title = stringResource(R.string.kill_switch),
                description = stringResource(R.string.kill_switch_description),
                icon = IconType.VectorIcon(Icons.Filled.StopCircle),
                actionType = SettingType.CUSTOM,
                isEnabled = settings.settings.value.useRootMode != true,
                customAction = {
                    val intent = Intent(Settings.ACTION_VPN_SETTINGS)
                    LocalContext.current.startActivity(intent)
                }
            )
        }
    }
}