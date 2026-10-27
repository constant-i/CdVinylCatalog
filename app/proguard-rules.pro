# uCrop
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

# Enum из :domain хранятся по имени константы:
# Format — в колонке Room, SortOrder — в SavedStateHandle.
#
# Имена констант R8 сохраняет сам, пока живы values() и valueOf(String) —
# иначе valueOf("CD") перестал бы находить значение. А вот неиспользуемую
# константу он вправе выкинуть: тогда запись из старой базы прочитается
# как UNKNOWN. Поэтому сохраняем состав целиком, а не только методы.
-keepclassmembers enum com.kai.cdvinylcatalog.domain.** { *; }
