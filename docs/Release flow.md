# Release flow

## Git branches

- Make sure `README.md` and `docs/CHANGELOG.md` is actualized
- Set proper `mod.version` number in `gradle.properties` - e.g. remove `-alpha` postfix used while development
- Build the mod using `./gradlew build`
- Make sure the `build/versions` folder contains .jar file for each supported MC version
- For each MC version, create a branch named like this: `release/1.5.0+1.21.11`

## Modrinth

- For each .jar, create separate version on Modrinth
- Use latest section of CHANGELOG.md as version description
- Update project description page based on README.md

## GitHub release

- For each .jar, create separate release on GitHub releases page, linking proper branch as previous version.
  - This is used as a backup, also users who can't use Modrinth might want to download from here.

## Discord

- Announce in `feesh-mod` channel of `Casters` DC
