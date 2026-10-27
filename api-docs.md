# Simple Music API
Документація API для платформи для слухання музики:

## Version: 1.0

### Available authorizations
#### bearer (HTTP, bearer)
Bearer format: JWT

---

### [POST] /api/auth/register
**Реєстрація нового користувача**

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [AuthCredentialsDto](#authcredentialsdto-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Успішна реєстрація | **application/json**: [AuthResponseDto](#authresponsedto-schema)<br> |

### [POST] /api/auth/login
**Авторизація користувача**

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [AuthCredentialsDto](#authcredentialsdto-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Успішна авторизація | **application/json**: [AuthResponseDto](#authresponsedto-schema)<br> |

### [POST] /api/auth/logout
**Вихід з системи**

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: { **"refreshToken"**: string }<br> |

#### Responses

| Code | Description |
| ---- | ----------- |
| 204 | Успішний вихід |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [POST] /api/auth/refresh
**Оновлення токенів доступу**

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Токени успішно оновлено | **application/json**: [AuthResponseDto](#authresponsedto-schema)<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

---

### [GET] /api/users/profile/{id}
**Отримати профіль користувача за ID**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Профіль користувача | **application/json**: [UserResponseDto](#userresponsedto-schema)<br> |

### [GET] /api/users/login/{id}
**Отримати логін користувача за ID**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Логін користувача | **application/json**: [UserLoginDto](#userlogindto-schema)<br> |

### [PUT] /api/users/profile
**Оновити профіль поточного користувача**

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [UpdateUserDto](#updateuserdto-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Оновлений профіль користувача | **application/json**: [UserResponseDto](#userresponsedto-schema)<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [DELETE] /api/users/profile
**Видалити профіль поточного користувача**

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Видалений профіль користувача | **application/json**: [UserResponseDto](#userresponsedto-schema)<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [GET] /api/users/history/count
**Отримати кількість пісень в історії переглядів користувача**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| search | query | Рядок пошуку | No | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Кількість пісень | **application/json**: { **"count"**: number }<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [GET] /api/users/likes/count
**Отримати кількість вподобаних користувачем пісень**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| search | query | Рядок пошуку | No | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Кількість пісень | **application/json**: { **"count"**: number }<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [GET] /api/users/history
**Отримати історію переглядів користувача**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| from | query | Початковий індекс | Yes | number |
| to | query | Кінцевий індекс | Yes | number |
| search | query | Рядок пошуку | No | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Історія переглядів | **application/json**: [ [SongHistoryItemDto](#songhistoryitemdto-schema) ]<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [GET] /api/users/likes
**Отримати вподобані користувачем пісні**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| from | query | Початковий індекс | Yes | number |
| to | query | Кінцевий індекс | Yes | number |
| search | query | Рядок пошуку | No | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Вподобані пісні | **application/json**: [ [SongRankingItemDto](#songrankingitemdto-schema) ]<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [GET] /api/users/playlists
**Отримати плейлісти користувача**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| search | query | Рядок пошуку | No | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Список плейлістів користувача | **application/json**: [ [UserPlaylistDto](#userplaylistdto-schema) ]<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [POST] /api/users/playlists
**Створити плейліст**

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [CreatePlaylistDto](#createplaylistdto-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Створений плейліст | **application/json**: { **"id"**: string }<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [PUT] /api/users/playlists/{id}
**Оновити назву плейлісту**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [UpdatePlaylistDto](#updateplaylistdto-schema)<br> |

#### Responses

| Code | Description |
| ---- | ----------- |
| 200 |  |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [GET] /api/users/playlists/{id}
**Отримати плейліст**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Плейліст з піснями | **application/json**: [PlaylistResponseDto](#playlistresponsedto-schema)<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [DELETE] /api/users/playlists/{id}
**Видалити плейліст**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |

#### Responses

| Code | Description |
| ---- | ----------- |
| 200 |  |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [POST] /api/users/playlists/{id}/songs/{songId}
**Додати пісню до плейлісту**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |
| songId | path |  | Yes | string |

#### Responses

| Code | Description |
| ---- | ----------- |
| 200 |  |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [DELETE] /api/users/playlists/{id}/songs/{songId}
**Видалити пісню з плейлісту**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |
| songId | path |  | Yes | string |

#### Responses

| Code | Description |
| ---- | ----------- |
| 200 |  |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [POST] /api/users/playlists/check-song/{songId}
**Перевірити наявність пісні у вказаних плейлістах**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| songId | path |  | Yes | string |

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [CheckSongInPlaylistsDto](#checksonginplaylistsdto-schema)<br> |

#### Responses

| Code | Description |
| ---- | ----------- |
| 200 | Масив ключ-значення (id плейлісту: boolean) |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

---

### [GET] /api/categories
**Отримати список всіх категорій**

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Список категорій | **application/json**: [ [CategoryListResponseDto](#categorylistresponsedto-schema) ]<br> |

---

### [GET] /api/song/{id}
**Отримати детальну інформацію про пісню**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Детальна інформація про пісню, якщо гість(без токену) то у `hasLiked` повертається false | **application/json**: [SongResponseDto](#songresponsedto-schema)<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [POST] /api/song/{id}/toggle-like
**Поставити або зняти лайк**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Статус лайку | **application/json**: { **"liked"**: boolean }<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [POST] /api/song/{id}/view
**Додати перегляд пісні**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Перегляд успішно додано | **application/json**: { **"success"**: boolean }<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

---

### [POST] /api/local-songs
**Створити нову пісню**

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **multipart/form-data**: [CreateSongDto](#createsongdto-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Пісню успішно створено | **application/json**: { **"id"**: string }<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [PATCH] /api/local-songs/{id}
**Оновити пісню (дані або файли)**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **multipart/form-data**: [UpdateSongParamsDto](#updatesongparamsdto-schema)<br> |

#### Responses

| Code | Description |
| ---- | ----------- |
| 200 | Пісню успішно оновлено |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [DELETE] /api/local-songs/{id}
**Видалити пісню**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |

#### Responses

| Code | Description |
| ---- | ----------- |
| 200 | Пісню успішно видалено |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

---

### [POST] /api/external-songs
**Створити нову зовнішню пісню**

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [CreateExternalSongDto](#createexternalsongdto-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Пісню успішно створено | **application/json**: { **"id"**: string }<br> |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [PATCH] /api/external-songs/{id}
**Оновити зовнішню пісню**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [UpdateExternalSongParamsDto](#updateexternalsongparamsdto-schema)<br> |

#### Responses

| Code | Description |
| ---- | ----------- |
| 200 | Пісню успішно оновлено |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

### [DELETE] /api/external-songs/{id}
**Видалити зовнішню пісню**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | string |

#### Responses

| Code | Description |
| ---- | ----------- |
| 200 | Пісню успішно видалено |

##### Security

| Security Schema | Scopes |
| --------------- | ------ |
| bearer |  |

---

### [GET] /api/recommendations/count/likes
**Отримати загальну кількість пісень (за лайками)**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| categoryId | query | ID категорії для фільтрації | No | string |
| authorId | query | ID автора для фільтрації | No | string |
| search | query | Рядок пошуку | No | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Кількість пісень | **application/json**: { **"count"**: number }<br> |

### [GET] /api/recommendations/count/views
**Отримати загальну кількість пісень (за переглядами)**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| categoryId | query | ID категорії для фільтрації | No | string |
| authorId | query | ID автора для фільтрації | No | string |
| search | query | Рядок пошуку | No | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Кількість пісень | **application/json**: { **"count"**: number }<br> |

### [GET] /api/recommendations/bylikes
**Отримати список пісень, відсортованих за лайками**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| from | query | Початковий індекс | Yes | number |
| to | query | Кінцевий індекс | Yes | number |
| categoryId | query | ID категорії для фільтрації | No | string |
| authorId | query | ID автора для фільтрації | No | string |
| search | query | Рядок пошуку | No | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Список пісень | **application/json**: [ [SongRankingItemDto](#songrankingitemdto-schema) ]<br> |

### [GET] /api/recommendations/byviews
**Отримати список пісень, відсортованих за переглядами**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| from | query | Початковий індекс | Yes | number |
| to | query | Кінцевий індекс | Yes | number |
| categoryId | query | ID категорії для фільтрації | No | string |
| authorId | query | ID автора для фільтрації | No | string |
| search | query | Рядок пошуку | No | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Список пісень | **application/json**: [ [SongRankingItemDto](#songrankingitemdto-schema) ]<br> |

### [GET] /api/recommendations/count/newest
**Отримати загальну кількість пісень (за датою додавання)**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| categoryId | query | ID категорії для фільтрації | No | string |
| authorId | query | ID автора для фільтрації | No | string |
| search | query | Рядок пошуку | No | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Кількість пісень | **application/json**: { **"count"**: number }<br> |

### [GET] /api/recommendations/bynewest
**Отримати список пісень, відсортованих за датою додавання (від новіших)**

#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| from | query | Початковий індекс | Yes | number |
| to | query | Кінцевий індекс | Yes | number |
| categoryId | query | ID категорії для фільтрації | No | string |
| authorId | query | ID автора для фільтрації | No | string |
| search | query | Рядок пошуку | No | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | Список пісень | **application/json**: [ [SongRankingItemDto](#songrankingitemdto-schema) ]<br> |

---
### Schemas

#### AuthCredentialsDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| login | string | Логін користувача | Yes |
| password | string | Пароль користувача | Yes |

#### AuthResponseDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| accessToken | string | Токен доступу (JWT) | Yes |
| refreshToken | string | Refresh токен (JWT) | Yes |

#### UserResponseDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| login | string | Логін користувача | Yes |
| createdAt | dateTime | Дата створення облікового запису | Yes |

#### UserLoginDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| login | string | Логін користувача | Yes |

#### UpdateUserDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| currentPassword | string | Поточний пароль (обов'язковий для зміни пароля) | No |
| login | string | Новий логін | No |
| password | string | Новий пароль | No |

#### SongHistoryItemDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | string | ID пісні | Yes |
| title | string | Назва пісні | Yes |
| cover | string | Обкладинка | No |
| viewedAt | number | Таймстемп останнього перегляду користувачем | Yes |
| authorId | string | Id автору | Yes |
| authorLogin | string | Логін автору | Yes |
| originalAuthor | string | Оригінальний автор | No |

#### SongRankingItemDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | string | ID пісні | Yes |
| title | string | Назва пісні | Yes |
| cover | string | Обкладинка | No |
| authorId | string | Id автору | Yes |
| authorLogin | string | Логін автору | Yes |
| originalAuthor | string | Оригінальний автор | No |

#### UserPlaylistDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | string | ID плейлісту | Yes |
| name | string | Назва плейлісту | Yes |

#### CreatePlaylistDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| name | string | Назва плейлісту<br>*Example:* `"Моя музика"` | Yes |

#### UpdatePlaylistDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| name | string | Нова назва плейлісту<br>*Example:* `"Моя нова музика"` | Yes |

#### PlaylistResponseDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | string | ID плейлісту | Yes |
| name | string | Назва плейлісту | Yes |
| userId | string | ID користувача | Yes |
| userLogin | string | Логін користувача | Yes |
| songs | [ [SongRankingItemDto](#songrankingitemdto-schema) ] | Пісні у плейлісті | Yes |

#### CheckSongInPlaylistsDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| playlistIds | [ string ] | Масив ID плейлістів | Yes |

#### CategoryListResponseDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | string |  | Yes |
| title | string |  | Yes |

#### SongResponseDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | string | ID пісні | Yes |
| authorId | string | ID автора | Yes |
| authorLogin | string | Логін автора | Yes |
| categoryIds | [ string ] | Категорії | Yes |
| likesCount | number | Кількість лайків | Yes |
| hasLiked | boolean | Чи лайкнув поточний користувач | Yes |
| viewsCount | number | Кількість переглядів | Yes |
| title | string | Назва пісні | Yes |
| description | string | Опис пісні | Yes |
| song | string | Файл пісні (якщо є) | No |
| cover | string | Обкладинка (якщо є) | No |
| originalAuthor | string | Оригінальний автор (опційно) | No |

#### CreateSongDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| title | string | Назва пісні (для локальних) | Yes |
| description | string | Опис пісні (для локальних) | No |
| originalAuthor | string | Оригінальний автор пісні (для локальних) | No |
| categoryIds | [ string ] | Категорії пісні | No |
| song | binary | Аудіофайл пісні | No |
| cover | binary | Обкладинка пісні | No |

#### UpdateSongParamsDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| title | string | Назва пісні (для локальних) | No |
| description | string | Опис пісні (для локальних) | No |
| originalAuthor | string | Оригінальний автор пісні (для локальних) | No |
| categoryIds | [ string ] | Категорії пісні | No |
| song | binary | Аудіофайл пісні | No |
| cover | binary | Обкладинка пісні | No |

#### CreateExternalSongDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| externalUrl | string | URL пісні з зовнішнього джерела ( https://audius.co/...) | Yes |
| categoryIds | [ string ] | Категорії пісні | No |

#### UpdateExternalSongParamsDto Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| externalUrl | string | URL пісні з зовнішнього джерела (https://audius.co/...) | No |
| categoryIds | [ string ] | Категорії пісні | No |
