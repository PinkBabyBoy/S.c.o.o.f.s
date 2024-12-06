package ru.barinov.onboarding

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.barinov.preferences.AppPreferences

@OptIn(ExperimentalMaterial3Api::class)
typealias OnboardingState = Map<OnBoarding, TooltipState>

@OptIn(ExperimentalMaterial3Api::class)
abstract class OnBoardingEngine(private val appPreferences: AppPreferences) {

    protected abstract val onboardings: Set<OnBoarding>

    private val savedShownData = appPreferences.shownOnBoardings.orEmpty()

    private val shownOnboardings = savedShownData.mapTo(HashSet()) { Integer.parseInt(it) }

    private val unShowedOnBoardings = onboardings.filter { !shownOnboardings.contains(it.ordinal) }

    @OptIn(ExperimentalMaterial3Api::class)
    private val initialStateMap: Map<OnBoarding, TooltipState> =
        unShowedOnBoardings.associateWith {
            TooltipState(
                initialIsVisible = true,
                isPersistent = true
            )
        }


    fun getInitial() = initialStateMap

    fun next(last: OnBoarding, saveScope: CoroutineScope): OnboardingState {
        saveScope.launch {
            appPreferences.shownOnBoardings = savedShownData + setOf(last.ordinal.toString())
        }
        return initialStateMap.mapValues {
            if (it.key == last)
                TooltipState(initialIsVisible = false, isPersistent = false)
            else it.value
        }
    }
}

enum class OnBoarding {
    KEY_CREATION,
    ADD_SELECTED,
    CREATE_CONTAINER,
    CHANGE_SOURCE,
    SORT_FILES,
    SELECT_FILE
}

@OptIn(ExperimentalMaterial3Api::class)
fun TooltipState?.orEmpty() = this ?: TooltipState(false, false)

@OptIn(ExperimentalMaterial3Api::class)
fun TooltipState?.switchDefault(defValue: Boolean) = this ?: TooltipState(defValue, defValue)
