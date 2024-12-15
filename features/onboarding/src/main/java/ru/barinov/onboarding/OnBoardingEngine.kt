package ru.barinov.onboarding

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.barinov.preferences.AppPreferences
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue

@OptIn(ExperimentalMaterial3Api::class)
typealias OnboardingState = Map<OnBoarding, TooltipState>

@OptIn(ExperimentalMaterial3Api::class)
abstract class OnBoardingEngine(private val appPreferences: AppPreferences) {

    protected abstract val onboardings: Set<OnBoarding>

    private val savedShownData = appPreferences.shownOnBoardings.orEmpty()

    private val shownOnboardings = savedShownData.mapTo(HashSet()) { Integer.parseInt(it) }

    private val unShowedOnBoardings = onboardings.filter { !shownOnboardings.contains(it.ordinal) }

    @OptIn(ExperimentalMaterial3Api::class)
    private val initialStateMap: LinkedHashMap<OnBoarding, TooltipState> =
        (unShowedOnBoardings.associateWith {
            TooltipState(
                initialIsVisible = true,
                isPersistent = true
            )
        } as LinkedHashMap)

    private val queue = ConcurrentLinkedQueue(initialStateMap.entries)

    private var current = queue.poll()


    fun current() = if(current != null)  mapOf(current.toPair()) else mapOf()


    fun next(last: OnBoarding? = null): OnboardingState {
        if (last != null)
            appPreferences.shownOnBoardings = savedShownData + setOf(last.ordinal.toString())

        return if (queue.isNotEmpty()) {
            current = queue.poll()
            Log.d("@@@", "$current")
            mapOf(current.toPair())
        }
        else mapOf()
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
