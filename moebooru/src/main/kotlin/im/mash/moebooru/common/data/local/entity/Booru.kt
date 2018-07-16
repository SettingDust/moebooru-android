package im.mash.moebooru.common.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "boorus",
        indices = [Index(value = ["url"], unique = true)])
data class Booru(
        @PrimaryKey(autoGenerate = true)
        var uid: Int?,
        val name: String,
        val scheme: String,
        val host: String,
        val url: String,
        val hash_salt: String
)