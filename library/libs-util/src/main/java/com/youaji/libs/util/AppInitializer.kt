@file:Suppress("unused")
package com.youaji.libs.util

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

internal class AppInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        application = context as Application
        application.doOnActivityLifecycle(
            onActivityCreated = { activity, _ ->
                activityCache.add(activity)
            },
            onActivityResumed = { activity ->
                if (!activityResume.contains(activity)) {
                    activityResume.add(activity)
                }
            },
            onActivityStopped = { activity ->
                activityResume.remove(activity)
            },
            onActivityDestroyed = { activity ->
                activityCache.remove(activity)
            }
        )
    }

    override fun dependencies() = emptyList<Class<Initializer<*>>>()
}
