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

package com.kin.athena.domain.usecase.log

import com.kin.athena.core.utils.Error
import com.kin.athena.core.utils.Result
import com.kin.athena.domain.model.Application
import com.kin.athena.domain.model.Log
import com.kin.athena.domain.repository.ApplicationRepository
import com.kin.athena.domain.repository.LogRepository
import javax.inject.Inject

class AddLog @Inject constructor(
    private val logRepository: LogRepository
) {
    suspend fun execute(log: Log): Result<Unit, Error> {
        return try {
            logRepository.insertLog(log)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(Error.ServerError(e.message ?: "Error while adding log $log"))
        }
    }
}
