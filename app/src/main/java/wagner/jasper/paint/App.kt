package wagner.jasper.paint

import android.app.Application
import android.content.Context

open class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: App

        fun getContext(): Context {
            return instance.applicationContext
        }
    }
}