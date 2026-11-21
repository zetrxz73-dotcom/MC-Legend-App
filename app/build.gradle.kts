[1mdiff --git a/app/build.gradle.kts b/app/build.gradle.kts[m
[1mindex dc02ec6..bd24264 100644[m
[1m--- a/app/build.gradle.kts[m
[1m+++ b/app/build.gradle.kts[m
[36m@@ -28,7 +28,7 @@[m [mandroid {[m
         compose = true[m
     }[m
     composeOptions {[m
[31m-        kotlinCompilerExtensionVersion = "1.5.14"[m
[32m+[m[32m        kotlinCompilerExtensionVersion = "1.5.4"[m
     }[m
 }[m
 dependencies {[m
[1mdiff --git a/build.gradle.kts b/build.gradle.kts[m
[1mindex e3e570a..4055907 100644[m
[1m--- a/build.gradle.kts[m
[1m+++ b/build.gradle.kts[m
[36m@@ -1,4 +1,4 @@[m
 plugins {[m
     id("com.android.application") version "8.7.3" apply false[m
[31m-    id("org.jetbrains.kotlin.android") version "1.9.22" apply false[m
[32m+[m[32m    id("org.jetbrains.kotlin.android") version "1.9.24" apply false[m
 }[m
