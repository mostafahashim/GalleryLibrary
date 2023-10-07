package hashim.gallery.util

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.*

class ContextWrapper : ContextWrapper {

    constructor(base: Context) : super(base)

    fun wrap(context: Context, newLocale: Locale): ContextWrapper {
        var context = context

        val res = context.resources
        val configuration = res.configuration

        configuration.setLocale(newLocale)

        val localeList = LocaleList(newLocale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)

        context = context.createConfigurationContext(configuration)


        return ContextWrapper(context)
    }
}