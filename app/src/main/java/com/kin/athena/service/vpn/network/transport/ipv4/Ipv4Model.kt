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

package com.kin.athena.service.vpn.network.transport.ipv4

data class IPv4(
    var ipVersion: Byte,
    var internetHeaderLength: Byte,
    var dscpOrTypeOfService: Byte,
    var ecn: Byte,
    var totalLength: Int,
    var identification: Int,
    var flagsAndFragmentOffset: Short,
    var timeToLive: Byte,
    var protocol: Byte,
    var headerChecksum: Int,
    var sourceIP: Int,
    var destinationIP: Int,
    var uid: Int = -1,
) {
    val fragmentOffset: Short
        get() = (flagsAndFragmentOffset.toInt() and 0x1FFF).toShort()

    val isFragmentationAllowed: Boolean
        get() = (flagsAndFragmentOffset.toInt() and 0x4000) != 0

    val iPHeaderLength: Int
        get() = internetHeaderLength.toInt() * 4
}