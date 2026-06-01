# Pirate Hat

Pirate Hat is a 2D Java platformer with Swing-based rendering, level PNG data,
player movement, enemies, traps, pickups, menus, audio, and enemy AI systems.

## Project Structure

```text
Platformer_ON_Java/
|-- PirateHat/
|   |-- src/    # Java source code
|   `-- res/    # Game resources: sprites, audio, UI images, level PNG files
|-- Treasure Hunters all assets/  # Source asset library for future development
|-- .gitignore
`-- README.md
```

Useful paths:

- Source code: `PirateHat/src/`
- Game resources: `PirateHat/res/`
- Backup/source asset library: `Treasure Hunters all assets/`
- Main class: `main.MainClass`

The `Treasure Hunters all assets/` directory is intentionally kept in the
repository as a source asset library for future development.

## Run In IntelliJ IDEA

1. Open the repository root in IntelliJ IDEA.
2. Configure a Java SDK for the project.
3. Mark `PirateHat/src/` as Sources Root if IntelliJ does not detect it
   automatically.
4. Mark `PirateHat/res/` as Resources Root if resource loading needs it.
5. Run `main.MainClass`.

The project does not require external libraries for compilation.

## Controls

These controls are determined from the current source code:

- `A` / `Left`: move left
- `D` / `Right`: move right
- `W` / `Up` / `Space`: jump
- Left mouse button: normal attack
- Right mouse button: power attack
- `Q`: use red potion
- `I`: inventory
- `Esc`: pause or close inventory
- `7` on the level select screen: open the AI demo level
- `F3`..`F7`: AI debug and metrics tools

## Assets

TODO: specify source and usage terms for external assets before publishing the
final version.
