package hashim.gallery.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.Delegates

object Preferences {

    internal var PREFS_NAME = "GalleryAppPrefs"

    internal var appPrefence: SharedPreferences by Delegates.notNull<SharedPreferences>()

    // notifications preference
    internal var ApplicationLocale_PREF = "ApplicationLocale"

    internal var preferenceEditor: SharedPreferences.Editor by Delegates.notNull<SharedPreferences.Editor>()

    fun initializePreferences(context: Context) {
        appPrefence = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }


    fun getApplicationLocale(): String {
        return appPrefence.getString(ApplicationLocale_PREF, "")!!
    }

    fun saveApplicationLocale(local: String) {
        preferenceEditor = appPrefence.edit()
        preferenceEditor.putString(ApplicationLocale_PREF, local)

        preferenceEditor.commit()
    }


    fun clearUserData() {
    }
}