package im.mash.moebooru.core.module

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import moe.shizuku.preference.PreferenceManager
import javax.inject.Singleton

@Module
class StorageModule {
    @Provides
    @Singleton
    fun providesSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}