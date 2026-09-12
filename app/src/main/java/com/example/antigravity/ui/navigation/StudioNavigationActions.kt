package com.example.antigravity.ui.navigation

/**
 * Interface Segregation Principle (ISP):
 * Segregated contract for studio navigation actions, decoupled from stateful screen models.
 */
interface StudioNavigationActions {
    fun navigateTo(screen: AntigravityAppScreen)
    fun openMatrix()
    fun openDrawer()
    fun lockStudio()
}

/**
 * Default implementation of [StudioNavigationActions] delegating to functional lambdas.
 */
class DefaultStudioNavigationActions(
    private val onNavigate: (AntigravityAppScreen) -> Unit,
    private val onMatrix: () -> Unit = {},
    private val onDrawer: () -> Unit = {},
    private val onLock: () -> Unit = {}
) : StudioNavigationActions {
    override fun navigateTo(screen: AntigravityAppScreen) = onNavigate(screen)
    override fun openMatrix() = onMatrix()
    override fun openDrawer() = onDrawer()
    override fun lockStudio() = onLock()
}
