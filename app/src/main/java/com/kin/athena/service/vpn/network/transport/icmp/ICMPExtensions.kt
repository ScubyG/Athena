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

package com.kin.athena.service.vpn.network.transport.icmp

import java.nio.ByteBuffer

fun ByteBuffer.toICMPPacket(): ICMPModel {
    val type = get().toInt()
    val code = get().toInt()
    val checksum = getShort().toInt()
    val identifier = getShort().toInt()
    val sequenceNumber = getShort().toInt()

    val data = ByteArray(remaining())
    get(data)

    return ICMPModel(type, code, checksum, identifier, sequenceNumber, data)
}