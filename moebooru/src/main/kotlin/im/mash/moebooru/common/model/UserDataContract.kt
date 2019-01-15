/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package im.mash.moebooru.common.model

import im.mash.moebooru.common.data.local.entity.User
import im.mash.moebooru.core.scheduler.Outcome
import io.reactivex.subjects.PublishSubject
import okhttp3.HttpUrl

interface UserDataContract {
    interface Repository {
        val userOutcome: PublishSubject<Outcome<MutableList<User>>>
        fun loadUsers()
        fun saveUser(user: User)
        fun deleteUser(user: User)
        fun updateUser(user: User)
        fun getUser(httpUrl: HttpUrl, passwordHash: String)
        fun handleError(error: Throwable)
    }
}