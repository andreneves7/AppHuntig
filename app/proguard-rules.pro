# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ⚠️ Estas regras foram preparadas mas NUNCA testadas com uma compilação real
# de release (minifyEnabled está a "false" no build.gradle). Antes de ativares
# minifyEnabled=true, testa uma build de release completa no teu telemóvel —
# login, registo, criação de eventos, mapa — porque regras incompletas para
# bibliotecas baseadas em reflexão (Firebase, Gson) causam falhas silenciosas
# em runtime que só aparecem numa build de release, nunca em debug.
# Documentação oficial: https://developer.android.com/build/shrink-code

# --- Firebase (Auth, Realtime Database, Storage, Crashlytics) ---
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# O Realtime Database serializa objetos por reflexão (ex: Model.kt em
# setValue()/getValue(Classe::class.java)) — sem isto, o R8 pode remover
# construtores/campos que a app precisa em runtime.
-keepclassmembers class com.example.app.** {
  public <init>();
  public <init>(...);
}
-keep class com.example.app.Model { *; }

# --- Google Maps / Places ---
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.android.libraries.places.** { *; }

# --- Glide (carregamento de imagens) ---
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
