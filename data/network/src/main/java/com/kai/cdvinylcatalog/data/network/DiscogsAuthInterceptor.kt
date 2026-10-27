package com.kai.cdvinylcatalog.data.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Подставляет учётные данные Discogs в каждый запрос.
 *
 * Сейчас это личный токен из `local.properties`, один на всё приложение.
 * Токен идёт заголовком `Authorization`, а не query-параметром: так он не
 * попадает в URL, а значит и в логи, историю прокси и ключи кэша.
 *
 * ### Почему интерцептор, а не `@Header` в [DiscogsApi]
 * Когда появится OAuth 1.0a, авторизация останется заголовком, но станет
 * подписью HMAC-SHA1. Подпись считается по итоговому URL вместе с
 * query-параметрами, а их видит только интерцептор — Retrofit-интерфейс
 * про собранный запрос ничего не знает. Поэтому `DiscogsApi` менять
 * не придётся: изменится только этот класс и источник токена.
 */
@Singleton
class DiscogsAuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Токен может отсутствовать: local.properties не обязателен для сборки
        val token = NetworkConstants.DISCOGS_TOKEN
        if (token.isBlank()) return chain.proceed(chain.request())

        val request = chain.request().newBuilder()
            .header(AUTHORIZATION, "Discogs token=$token")
            .build()

        return chain.proceed(request)
    }

    private companion object {
        const val AUTHORIZATION = "Authorization"
    }
}
