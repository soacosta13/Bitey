Bitey App 🍳

App Android de recetas hecha con Kotlin sobre la API pública de [TheMealDB](https://www.themealdb.com/api.php). 2do parcial, Desarrollo de Aplicaciones Móviles 2.

## Video de demostración
https://drive.google.com/file/d/1l5PvhyZYXo8oUpvU3znF85NLu-9SsT2p/view?usp=drive_link

## Funcionalidades
- Listado de platos por categoría, con miniatura y título.
- Buscador por nombre del plato o por ingrediente.
- Detalle en Jetpack Compose: categoría, origen, ingredientes y preparación paso a paso.
- "Mi Recetario": recetas guardadas en SQLite, disponibles sin conexión.
- Splash, Home y modo claro/oscuro (SharedPreferences).

## Tecnologías
Kotlin · MVVM + Repository · Fragments + XML + RecyclerView · Jetpack Compose (detalle)
Retrofit + Gson · SQLite (SQLiteOpenHelper) · Picasso · Corrutinas · LiveData / StateFlow

## Arquitectura
Fragment / Compose → MainViewModel → MealRepository → MealApiService (Retrofit) y MealDbHelper (SQLite)

## Endpoints usados
categories.php · filter.php?c= · filter.php?i= · search.php?s= · lookup.php?i=

## Cómo ejecutarlo
Abrir en Android Studio, sincronizar Gradle y correr en un emulador o dispositivo (minSdk 24).
