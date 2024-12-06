package ru.barinov.file_browser

import ru.barinov.onboarding.OnBoarding
import ru.barinov.onboarding.OnBoardingEngine
import ru.barinov.preferences.AppPreferences

class FileBrowserOnboarding(appPreferences: AppPreferences): OnBoardingEngine(appPreferences) {

    override val onboardings: Set<OnBoarding>
        get() = setOf(OnBoarding.SORT_FILES, OnBoarding.ADD_SELECTED, OnBoarding.CHANGE_SOURCE, OnBoarding.SELECT_FILE)
}
