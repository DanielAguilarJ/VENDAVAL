package mx.vendaval.core.di

import org.koin.core.module.Module

/**
 * Bindings that can only be satisfied with platform APIs: writable storage
 * location, location services and the hand-off to other apps.
 *
 * Declared as `expect` on the module rather than on each individual class so
 * adding a platform-specific dependency later does not require a new
 * expect/actual pair in three source sets.
 */
expect fun platformModule(): Module
