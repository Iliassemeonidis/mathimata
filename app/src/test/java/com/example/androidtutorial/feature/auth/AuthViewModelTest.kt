package com.example.androidtutorial.feature.auth

import com.example.androidtutorial.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `submit with empty fields enters`() = runTest {
        val vm = AuthViewModel()
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.toList(effects) }
        vm.onIntent(AuthIntent.Submit)
        assertEquals(listOf<AuthEffect>(AuthEffect.Entered), effects)
    }

    @Test
    fun `fields update state and submit still enters`() = runTest {
        val vm = AuthViewModel()
        val effects = mutableListOf<AuthEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.toList(effects) }
        vm.onIntent(AuthIntent.Login("ilya"))
        vm.onIntent(AuthIntent.Password("secret"))
        assertEquals(AuthState("ilya", "secret"), vm.state.value)
        vm.onIntent(AuthIntent.Submit)
        assertEquals(listOf<AuthEffect>(AuthEffect.Entered), effects)
    }
}
