pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // The original Xposed API repository no longer serves API 82.
        // Restrict the mirror to this compile-only API; it is not packaged in the APK.
        exclusiveContent {
            forRepository { maven { url = uri("https://maven.aliyun.com/repository/public") } }
            filter { includeGroup("de.robv.android.xposed") }
        }
    }
}
rootProject.name = "WpsRootToastFilter"
include(":app")
