package com.example.androidtutorial.feature.shell

/**
 * Returns the tab route that starts a new visit, or null when nothing starts one.
 *
 * A visit starts only when the selected tab route changes from a known previous route.
 * The first route has no previous one, so it is not a new visit. Opening a topic or the
 * profile does not change the tab route, so returning from them is not a new visit either.
 */
internal fun routeToCountAsVisit(previousRoute: String?, currentRoute: String): String? =
    if (previousRoute != null && previousRoute != currentRoute) currentRoute else null
