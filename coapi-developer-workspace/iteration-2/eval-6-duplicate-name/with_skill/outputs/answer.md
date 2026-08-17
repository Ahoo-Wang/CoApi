# Eval 6 — Duplicate CoApi name startup failure (with_skill)

## What happened

Your app very likely already had two clients resolving to the same name before the upgrade — the upgrade just made it visible. Since **CoApi v2.2.0**, registration fails fast on duplicate client names instead of silently ignoring the conflict.

How the name is derived (see `CoApiDefinition.resolveClientName` in `spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiDefinition.kt`):

- If `@CoApi(name = "...")` is set, that name is used.
- Otherwise it defaults to the **interface simple name**.

So your error means two interfaces resolve to the name `UserClient`:

1. **Two interfaces with the same simple name** `UserClient` in different packages (neither sets a custom `name`), or
2. **Two interfaces that explicitly declare the same custom name**, e.g. both `@CoApi(name = "UserClient")`.

The check itself lives in `CoApiRegistrar.register` (`spring/src/main/kotlin/me/ahoo/coapi/spring/CoApiRegistrar.kt`): definitions are grouped by name, and any group with more than one entry throws:

```kotlin
throw IllegalStateException(
    "Duplicate CoApi name [${conflicting.first().name}]: " +
        "${conflicting.map { it.apiType.name }}. ..."
)
```

**Why it only broke after upgrading:** in older versions the second registration with the same bean name was skipped with just a `warn` log ("already exists - Ignore"), so one of the two interfaces silently got no (or the wrong) bean. In v2.2.0 this latent misconfiguration became a startup failure. The name must be unique because it's not only the bean name — it's also the configuration key for `coapi.clients.<name>.*`.

## How to fix it

Pick one of these:

1. **Give one interface a distinct name** (least invasive):

   ```kotlin
   @CoApi(name = "InternalUserClient", serviceId = "user-service")
   interface UserClient { ... }
   ```

2. **Rename one of the interfaces** if both simple names are `UserClient` and neither sets a custom `name`.

3. **Delete the duplicate** if one of them is dead code (check the two fully-qualified names in the error message — often it's an old copy in another package).

One caveat when you go with option 1: the client name is also the key for per-client configuration, so if you have YAML like `coapi.clients.UserClient.base-url` or per-client filter/interceptor settings, update those keys to the new name too.
