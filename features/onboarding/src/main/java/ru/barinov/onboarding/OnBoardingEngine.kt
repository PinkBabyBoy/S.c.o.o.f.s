package ru.barinov.onboarding

import ru.barinov.preferences.AppPreferences
import java.util.LinkedHashSet
import java.util.concurrent.ConcurrentLinkedQueue

typealias OnboardingInfo = Pair<OnBoarding?, Boolean>

abstract class OnBoardingEngine(private val appPreferences: AppPreferences) {

    protected abstract val onboardings: Set<OnBoarding>

    private val savedShownData get() = appPreferences.shownOnBoardings.orEmpty()

    private val shownOnboardings get() = savedShownData.mapTo(HashSet()) { Integer.parseInt(it) }

    private val unShowedOnBoardings = onboardings.filter { !shownOnboardings.contains(it.ordinal) }


    private val queue = ConcurrentLinkedQueue(unShowedOnBoardings)

    private var current: OnBoarding? = queue.poll()

    fun current(): OnboardingInfo = current to queue.isNotEmpty()


    fun next(): OnboardingInfo {
        val current = current
        if(current != null)
            appPreferences.shownOnBoardings = savedShownData + setOf(current.ordinal.toString())
        this.current = queue.takeIf { it.isNotEmpty() }?.poll()
        return this.current to queue.isNotEmpty()
    }
}

enum class OnBoarding {
    KEY_CREATION,
    ADD_SELECTED,
    CREATE_CONTAINER,
    CHANGE_SOURCE,
    SORT_FILES,
    SELECT_FILE,
    REMOVE_SELECTED
}
