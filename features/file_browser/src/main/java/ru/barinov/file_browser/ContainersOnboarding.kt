package ru.barinov.file_browser

import ru.barinov.onboarding.OnBoarding
import ru.barinov.onboarding.OnBoardingEngine
import ru.barinov.preferences.AppPreferences

class ContainersOnboarding(appPreferences: AppPreferences): OnBoardingEngine(appPreferences) {
    override val onboardings: Set<OnBoarding>
        get() = setOf(OnBoarding.CREATE_CONTAINER)
}