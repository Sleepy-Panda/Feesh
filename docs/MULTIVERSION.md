# Multi-version support (1.21.10 / 1.21.11)

Проект собирает два JAR: для **1.21.10-fabric** и **1.21.11-fabric** через [Deftu multiversion-root](https://github.com/Deftu/Gradle-Toolkit) и препроцессинг.

## Конфигурация

- **settings.gradle.kts** — в `listOf` указаны `"1.21.10-fabric"` и `"1.21.11-fabric"`; у каждого подпроекта `projectDir = versions/<version>` и общий `build.gradle.kts`.
- **root.gradle.kts** — в `preprocess` задана иерархия: `1.21.11-fabric` (1_21_11) → `1.21.10-fabric` (1_21_10). Код пишется для старшей версии (1.21.11), препроцессор вырезает блоки для младшей (1.21.10) при сборке 1.21.10.
- **gradle.properties** — версии зависимостей: `rconfig.version.1.21.10`, `rconfig.version.1.21.11`.
- **versions/1.21.11-fabric/gradle.properties** — переопределение `fabric.yarn.version` (Yarn для 1.21.11), т.к. в Deftu Toolkit нет встроенной записи для 1.21.11. При появлении новых build Yarn (например 1.21.11+build.5) обновить значение там.
- **build.gradle.kts** — в `when (mcData.version)` ветки для `VERSION_1_21_10` и `VERSION_1_21_11` (Fabric API, resourcefulconfig и т.д.).

## Mixins и совместимость 1.21.10 / 1.21.11

Используемые миксины и точки инжекта рассчитаны на общий набор классов/методов между 1.21.10 и 1.21.11. Если в одной из версий меняются сигнатуры или имена (Yarn), потребуются правки.

### Текущие миксины

| Миксин | Класс | Метод / точка | Риск расхождения |
|--------|--------|----------------|------------------|
| **EntityRendererMixin** | `EntityRenderer` | `shouldRender(T, Frustum, double, double, double)` | Низкий — стабильный API рендера сущностей. |
| **MinecraftClientMixin** | `MinecraftClient` | `joinWorld(ClientWorld)`, `setScreen(Screen)` | Низкий. |
| **ScreenMixin** | `Screen` | `renderBackground(DrawContext, int, int, float)` | Низкий. |
| **InGameHudMixin** | `InGameHud` | `render(…)` → перед `renderPlayerList(…)` | Средний — при изменении порядка вызовов в `render` или переименовании `renderPlayerList` нужно обновить `target`. |
| **ClientPlayerInteractionManagerMixin** | `ClientPlayerInteractionManager` | `interactItem(PlayerEntity, Hand)`, `interactBlock(ClientPlayerEntity, Hand, BlockHitResult)` | Низкий. |

### Что проверять при обновлении

1. **InGameHudMixin** — `@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderPlayerList(...)")`. При смене маппингов (Yarn) или изменении сигнатуры в 1.21.11 обновить `target` или добавить условный препроцессинг по версии (если появятся расхождения).
2. **fabric.mod.json** — `"minecraft":"~${minor_mc_version}"` подставляет версию при сборке; для 1.21.10 и 1.21.11 подходит.
3. При появлении ошибок миксинов в одной из версий:
   - проверить [Yarn mappings](https://github.com/FabricMC/yarn) для нужной версии;
   - при необходимости обернуть отличающийся код в препроцессорные блоки (`#if 1_21_11` / `#else` / `#endif`), см. документацию Deftu preprocess.

## Сборка

- Сборка всех версий: `./gradlew build` (JAR попадают в корень проекта при `moveBuildsToRootProject.set(true)`).
- Одна версия: `./gradlew :1.21.10-fabric:build` или `./gradlew :1.21.11-fabric:build`.

## Зависимости 1.21.11

- **Fabric API**: `0.141.3+1.21.11` (Modrinth: [Fabric API 0.141.3+1.21.11](https://modrinth.com/mod/fabric-api/version/0.141.3+1.21.11)).
- **Resourceful Config**: для 1.21.11 используется артефакт `resourcefulconfig-fabric-1.21.9` с версией из `rconfig.version.1.21.11` (совместимость 1.21.x у Team Resourceful). При появлении отдельного артефакта для 1.21.11 можно сменить имя артефакта в `build.gradle.kts`.

## Устранение ошибки «файл занят другим процессом» (Loom cache)

При сборке может появиться ошибка вида:
`FileSystemException: ...\essential-loom\1.21.10\...\mappings.jar: Процесс не может получить доступ к файлу, так как этот файл занят другим процессом`

**Причина:** Loom решает пересобрать кэш (например, после обрыва предыдущей сборки) и удаляет старую папку. Файл `mappings.jar` при этом держит **Java Language Server** (расширение Red Hat Java в Cursor/VS Code) — в сообщении об ошибке внизу указан процесс `java.exe` из `redhat.java` или `jre\...\bin\java.exe` с родителем `Cursor.exe`.

**Что сделать:**

1. **Вариант А — собрать без IDE:** полностью закрыть Cursor, в обычной консоли (cmd/PowerShell) выполнить:
   ```bat
   gradlew --stop
   rmdir /s /q "%USERPROFILE%\.gradle\caches\essential-loom"
   cd "D:\Minecraft Modding\Feesh"
   gradlew build --no-daemon
   ```
   После успешной сборки можно снова открыть проект в Cursor.

2. **Вариант Б — не закрывая Cursor:** отключить расширение «Language Support for Java» (Red Hat), выполнить в терминале Cursor:
   ```bat
   .\gradlew --stop
   ```
   Удалить папку вручную в проводнике: `C:\Users\<Имя>\.gradle\caches\essential-loom` (если проводник не даёт — какой‑то процесс всё ещё держит файлы; закрой все терминалы с Gradle и повтори). Затем:
   ```bat
   .\gradlew build --no-daemon
   ```
   Включить расширение Java обратно.

3. **Чтобы реже попадать в эту ситуацию:** не прерывать сборку по возможности (Ctrl+C может оставить «disowned» lock). При необходимости чистить кэш только при закрытом Cursor или при отключённом Java-расширении.

## Версия Deftu Gradle Toolkit и обновления

Плагин **dev.deftu.gradle.tools.minecraft.loom** входит в [Deftu Gradle Toolkit](https://github.com/Deftu/Gradle-Toolkit); его версия задаётся версией **multiversion-root** в `settings.gradle.kts`.

### Как посмотреть текущую версию

- В проекте: в **settings.gradle.kts** в `pluginManagement.plugins` указано  
  `id("dev.deftu.gradle.multiversion-root") version("2.64.0")` — значит используется **2.64.0** (в т.ч. для Loom и остальных tools).
- Через Gradle: в корне проекта выполнить  
  `.\gradlew buildEnvironment`  
  и в выводе найти зависимости с `dev.deftu.gradle` (номера версий будут в дереве зависимостей).

### Как проверить, есть ли обновления и поддержка 1.21.11

1. **Maven (список версий):**  
   - [multiversion-root](https://maven.deftu.dev/releases/dev/deftu/gradle/multiversion-root/dev.deftu.gradle.multiversion-root.gradle.plugin/)  
   - [gradle-toolkit](https://maven.deftu.dev/releases/dev/deftu/gradle/gradle-toolkit/)  
   Номера версий совпадают (например, последняя **2.73.0** на момент написания).

2. **GitHub — релизы и changelog:**  
   [Releases · Deftu/Gradle-Toolkit](https://github.com/Deftu/Gradle-Toolkit/releases)  
   В описании релизов смотри, не добавлена ли поддержка новых версий Minecraft (в т.ч. 1.21.11) или Yarn.

3. **Проверить поддержку 1.21.11 в коде:** в репозитории [Deftu/Gradle-Toolkit](https://github.com/Deftu/Gradle-Toolkit) поиск по `1.21.11` или `getFabricYarnVersion` / MinecraftInfo — если для 1.21.11 добавлена запись, в новых версиях toolkit ошибка «No Fabric Yarn version found for 1.21.11» может исчезнуть.

### Как обновить и проверить, решит ли это проблему 1.21.11

В **settings.gradle.kts** смени версию на последнюю (например 2.73.0):

```kotlin
id("dev.deftu.gradle.multiversion-root") version("2.73.0")
```

Затем:

1. Временно закомментируй или удали строку `fabric.yarn.version=...` в **versions/1.21.11-fabric/gradle.properties**.
2. Выполни `.\gradlew :1.21.11-fabric:build`.
3. Если сборка прошла — в новой версии Toolkit уже есть Yarn для 1.21.11, свойство `fabric.yarn.version` можно не использовать.
4. Если снова «No Fabric Yarn version found for 1.21.11» — верни `fabric.yarn.version=1.21.11+build.4` в `versions/1.21.11-fabric/gradle.properties` и при желании откати версию плагина на 2.64.0 или оставь новую (она не мешает переопределению).
