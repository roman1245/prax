-dontwarn retrofit.**
-dontwarn okio.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-keep class android.support.v7.widget.SearchView { *; }