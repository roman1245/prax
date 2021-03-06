# retrofit
-dontwarn retrofit2.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn okhttp3.internal.platform.*
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**


# picasso
-dontwarn com.squareup.picasso.**

# searchview
-keep class android.support.v7.widget.SearchView { *; }

# open CSV
-dontwarn com.opencsv.**

# in app billing
-keep class com.android.vending.billing.**

-keepclassmembers class xyz.kandrac.library.model.firebase.** {
  *;
}

-keep class org.apache.commons.** { *; }