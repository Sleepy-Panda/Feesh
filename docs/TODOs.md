## Players feedback

FeeshMod.LOGGER.info("Nessie destination alert: ${mobEntity.x}, ${mobEntity.y}, ${mobEntity.z}") // TODO Remove

- TODO: Test in 1.21
- Add Flash announce
- Sometimes current world&zone is detected wrongly
- +/-/x for sea creatures
- Limit SCs count in the view
- Magma Core fishing widget
- Someone has no Vial title/sound (I checked and did not reproduce, I had my title/sound as usual)
- Fished coins to add via the command.
- Carmine dye into tracker
- Autoupdates
- Items counted after /gfs: Moved 39 Enchanted Sea Lumies from your Sacks to your inventory.
- Settings are not saved after exiting the game, probably because user closes window using X button to exit
- On 1.21.11, original fishing timer armorstand appears for a second when the fish starts swimming towards bobber

[Feesh] Error occurred in LinkedHashMap.java:1023 in method nextNode - Failed to save Feesh data data
java.util.ConcurrentModificationException: null
	at java.base/java.util.LinkedHashMap$LinkedHashIterator.nextNode(LinkedHashMap.java:1023) ~[?:?]
	at java.base/java.util.LinkedHashMap$LinkedEntryIterator.next(LinkedHashMap.java:1058) ~[?:?]
	at java.base/java.util.LinkedHashMap$LinkedEntryIterator.next(LinkedHashMap.java:1055) ~[?:?]
	at knot/com.google.gson.internal.bind.MapTypeAdapterFactory$Adapter.write(MapTypeAdapterFactory.java:220) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.internal.bind.MapTypeAdapterFactory$Adapter.write(MapTypeAdapterFactory.java:154) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper.write(TypeAdapterRuntimeTypeWrapper.java:73) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$2.write(ReflectiveTypeAdapterFactory.java:247) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$Adapter.write(ReflectiveTypeAdapterFactory.java:490) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper.write(TypeAdapterRuntimeTypeWrapper.java:73) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$2.write(ReflectiveTypeAdapterFactory.java:247) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$Adapter.write(ReflectiveTypeAdapterFactory.java:490) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper.write(TypeAdapterRuntimeTypeWrapper.java:73) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$2.write(ReflectiveTypeAdapterFactory.java:247) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$Adapter.write(ReflectiveTypeAdapterFactory.java:490) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.Gson.toJson(Gson.java:944) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.Gson.toJson(Gson.java:899) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.Gson.toJson(Gson.java:848) ~[gson-2.11.0.jar:?]
	at knot/com.google.gson.Gson.toJson(Gson.java:825) ~[gson-2.11.0.jar:?]
	at knot/com.github.sleepypanda.feesh.utils.FileUtils.saveJsonToFileSync(FileUtils.kt:65) ~[Feesh-1.5.0-alpha+1.21.10-fabric.jar:?]
	at knot/com.github.sleepypanda.feesh.utils.FileUtils.saveJsonToFileAsync$lambda$0(FileUtils.kt:95) ~[Feesh-1.5.0-alpha+1.21.10-fabric.jar:?]
	at java.base/java.util.concurrent.CompletableFuture$AsyncRun.run(CompletableFuture.java:1804) ~[?:?]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144) ~[?:?]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642) ~[?:?]
	at java.base/java.lang.Thread.run(Thread.java:1583) [?:?]

## Tech Debt

- Go through TODOs in the code
- Rework ticks counters across all overlays
- Keybinds periodically reset, probably after MC crashes or turning off PC
- Version check to correctly detect newer version on Modrinth (e.g. 1.1.0 and 1.1.0-beta)
- Will NEU prices API be available? Do I need to hop to another one?
- Enabled highlighters / slot renderers to update dynamically instead of check every render event.

## Settings

Make sure newly added values in the dropdowns are selected if needed. E.g. if I add new rare drop type to Alerts, alert for this drop should be enabled.

## Alerts

- Player Death sound is not played. :c Probably because player's world is not loaded while they go to spawn on death.

## Overlays

- Check if "0" key works for numpad in /feeshMoveAllGuis
