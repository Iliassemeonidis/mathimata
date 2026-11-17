package com.example.androidtutorial.feature.shell

import com.example.androidtutorial.navigation.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TabVisitPolicyTest {
    @Test
    fun firstRouteIsNotAVisit() {
        assertNull(routeToCountAsVisit(null, AppDestination.Home))
    }

    @Test
    fun homeToKotlinCountsKotlin() {
        assertEquals(AppDestination.Kotlin, routeToCountAsVisit(AppDestination.Home, AppDestination.Kotlin))
    }

    @Test
    fun kotlinToHomeCountsHome() {
        assertEquals(AppDestination.Home, routeToCountAsVisit(AppDestination.Kotlin, AppDestination.Home))
    }

    @Test
    fun sameRouteTwiceIsNotAVisit() {
        assertNull(routeToCountAsVisit(AppDestination.Home, AppDestination.Home))
        assertNull(routeToCountAsVisit(AppDestination.Kotlin, AppDestination.Kotlin))
    }
}
