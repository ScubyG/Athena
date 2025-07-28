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

package com.kin.athena.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kin.athena.domain.model.Ip
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkFilterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIp(ip: Ip)

    @Query("DELETE FROM NetworkFilter WHERE ip = :ip")
    suspend fun deleteIp(ip: String)

    @Query("DELETE FROM NetworkFilter")
    suspend fun deleteIps()

    @Query("SELECT * FROM NetworkFilter")
    fun getAllIps(): Flow<List<Ip>>
}
