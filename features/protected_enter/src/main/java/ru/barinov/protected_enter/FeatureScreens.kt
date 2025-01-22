package ru.barinov.protected_enter

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import ru.barinov.core.navigation.Routes
import ru.barinov.routes.EnterScreenRoute


fun NavGraphBuilder.enterScreen(
navController: NavController,
bottomNavBarVisibility: (Boolean) -> Unit,
changeColor: () -> Unit,
modifier: Modifier = Modifier
) {
    composable(route = EnterScreenRoute.ENTER_SCREEN.name) {
        val vm: EnterScreenViewModel = koinViewModel()
        EnterScreen(
            state = vm.uiState.collectAsState().value,
            sideEffects = vm.sideEffects,
            enterScreenEvent = vm::handleEvent,
            rebase = {
                navController.navigate(Routes.BROWSER.name)
                bottomNavBarVisibility(true)
                changeColor() },
        )
    }
}

fun NavGraphBuilder.permissionInfoScreen() {
    composable(route = EnterScreenRoute.PERMISSION_INFO.name) {
        InfoScreen()
    }
}
