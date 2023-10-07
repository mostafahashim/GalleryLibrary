package hashim.gallerylib.util
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.*

class ContextWrapper(base: Context) : ContextWrapper(base) {

    fun wrap(context1: Context, newLocale: Locale): ContextWrapper {
        var context = context1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.setLocale(newLocale)
            val localeList = LocaleList(newLocale)
            LocaleList.setDefault(localeList)
            context.resources.configuration.setLocales(localeList)
            context = context.createConfigurationContext(context.resources.configuration)
        } else {
            context.resources.configuration.setLocale(newLocale)
            context = context.createConfigurationContext(context.resources.configuration)
        }

        return ContextWrapper(context)
    }
}