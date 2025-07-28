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

package com.kin.athena.service.utils.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

interface AppChangeCallback {
    fun onAppInstalled(packageName: String?)
    fun onAppUninstalled(packageName: String?)
}

class AppChangeReceiver(private val callback: AppChangeCallback) : BroadcastReceiver() {
    
    private var isRegistered = false

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val packageName = intent.data?.encodedSchemeSpecificPart

        when (action) {
            Intent.ACTION_PACKAGE_ADDED -> callback.onAppInstalled(packageName)
            Intent.ACTION_PACKAGE_REMOVED -> callback.onAppUninstalled(packageName)
        }
    }

    fun register(context: Context) {
        if (!isRegistered) {
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addDataScheme("package")
            }

            context.registerReceiver(this, intentFilter)
            isRegistered = true
        }
    }

    fun unregister(context: Context) {
        if (isRegistered) {
            try {
                context.unregisterReceiver(this)
                isRegistered = false
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered, ignore
                isRegistered = false
            }
        }
    }
}