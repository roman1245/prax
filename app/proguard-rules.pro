# retrofit
-dontwarn retrofit2.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# picasso
-dontwarn com.squareup.picasso.**

# searchview
-keep class android.support.v7.widget.SearchView { *; }

# open CSV
-dontwarn com.opencsv.**

# in app billing
-keep class com.android.vending.billing.**

-keep class org.apache.commons.** { *; }