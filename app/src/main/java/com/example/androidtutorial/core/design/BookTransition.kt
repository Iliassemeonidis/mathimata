package com.example.androidtutorial.core.design

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

fun bookEnter(): EnterTransition =
    slideInHorizontally(tween(320)) { it } + fadeIn(tween(320)) +
        scaleIn(tween(320), initialScale = 0.98f)

fun bookExit(): ExitTransition =
    slideOutHorizontally(tween(320)) { -it / 4 } + fadeOut(tween(320))

fun bookPopEnter(): EnterTransition =
    slideInHorizontally(tween(320)) { -it / 4 } + fadeIn(tween(320))

fun bookPopExit(): ExitTransition =
    slideOutHorizontally(tween(320)) { it } + fadeOut(tween(320))
