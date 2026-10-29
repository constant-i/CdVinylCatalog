# uCrop
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

# --- Retrofit и Gson ---
# Оба используют рефлексию: прямых обращений в коде нет, и без -keep
# R8 считает эти классы неиспользуемыми.

# Gson читает поля DTO по имени из @SerializedName.
-keep class com.kai.cdvinylcatalog.data.network.dto.** { *; }

# Retrofit берёт тип ответа из сигнатуры Continuation<T> suspend-метода.
# Continuation в коде не создаётся, поэтому без -keep сигнатура не резолвится:
# ClassCastException: Class cannot be cast to ParameterizedType.
-keep,allowobfuscation class kotlin.coroutines.Continuation { *; }
-keepattributes Signature

# DiscogsApi сохраняет сам Retrofit правилом из retrofit2.pro.
# Проверено: без явного -keep поиск работает.

# --- Данные, хранимые по имени ---
# Enum из :domain пишутся в Room и SavedStateHandle именем константы.
# values()/valueOf() защищают имена от переименования, но не от удаления
# неиспользуемой константы — тогда запись прочтётся как UNKNOWN.
-keepclassmembers enum com.kai.cdvinylcatalog.domain.** { *; }
