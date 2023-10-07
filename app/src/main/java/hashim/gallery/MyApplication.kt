package hashim.gallery

import android.app.Application
import android.content.Context
import hashim.gallery.util.LocaleHelper
import hashim.gallery.util.Preferences

class MyApplication : Application() {

    lateinit var context: Context
    override fun onCreate() {
        super.onCreate()
        context = LocaleHelper.updateLocale(this)
    }

    override fun attachBaseContext(base: Context) {
        context = LocaleHelper.updateLocale(base)
        super.attachBaseContext(context)
    }


    fun getAppLanguage(): String {
        if (Preferences.getApplicationLocale() != null && Preferences.getApplicationLocale()
                .compareTo("en") == 0
        ) {
            return "2"
        } else {
            return "1"
        }
    }
}