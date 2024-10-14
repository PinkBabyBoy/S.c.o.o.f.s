pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Scoof"
include(":app")
include(":features")
include(":features:protected_enter")
include(":core")
include(":component_initializer")
include(":features:protected_enter:navigation")
include(":navhost")
include(":password_manager")
include(":preferences")
include(":cryptography")
include(":usb_connection")
include(":providers")
include(":providers:internal_data")
include(":providers:external_data")
include(":permission_manager")
include(":transaction_manager")
include(":file_works")
include(":features:protected_enter:routes")
include(":features:file_browser")
include(":features:file_browser:navigation")
include(":file_works:read_worker")
include(":file_works:write_worker")
include(":file_process_worker")
