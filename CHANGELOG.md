# Changelog

All notable changes to this project will be documented in this file.


## [2.0.3] - 2020-06-20

### Changed

- Fixed initialization of the default book pack on servers
- Added 'generateDefaultBookPackAtStart' ('True' by default) option to allow disabling the default book pack. The default book pack in the config gets recreated every time if this is 'True'. Set this to 'False' if you want to alter/remove the default pack.
 
## [2.0.2] - 2020-06-15

### Changed

- Fixed another server side crash with config writer
 
## [2.0.1] - 2020-06-07

### Changed

- Fixed server side crash 

## [2.0.0] - 2020-05-16

### Changed

- Updated to Minecraft 1.12.2 from 1.7.10! 
- New item texture for the Dusty Book. (Previously the vanilla Written Book texture was used.)

## [1.2.2]

- Fixed villager spawning crash.

## [1.2.1]

- Potentially fixed a server crash bug.

## [1.2.0]

- Completely reset config names in prep of ingame configs, all options will be reset to default.
- Two new "utility" configs!
  - openToPreviousPage, books now reopen to the page they were closed on instead of page 1.
  - pauseWhileReading, allows you to read books without stopping time in SSP.
- Two new commands, both require cheats or OP to be used.
  - `/lbspawn <filepath> <player>` spawns a specific book for testing.
  - `/lbreset <player>` resets unique book blackouts for the player.
- Added dusty books, can be right-clicked to generate a random book.
- Dusty books will now appear as chest loot.
- Added "biomes" book property, a list of biome ids the book can drop in.

## [1.1.0]

- Featuring: actual lost books! When a book is dropped and left to expire, it has a chance (default 100%) to be captured and dropped or offered in a villager trade once at a later time. These books are saved on a per-world basis, like unique book blackouts.
- Generated books will appear in villager trades (sold by librarians)!
- New file structure! Your current book folders will work the same, apart from moving your words folder out of the adLib folder, but now you can also make your own folders within the main book folders with books (or more folders!) inside of them.
- Individual book properties! Adjust drop weight and more!
- Loads of new ad lib book functions, including better word categories and codes that generate more than just random words!
- Lost Books will no longer cut off formats when it automatically inserts a page break!
- Unique book data is now stored based on file name, so Lost Books will no longer reset all data any time you change the number of books you have!
- Unique books now have an extra line on their tooltip that says "Unique". (Can be disabled.)
- Many new stories! Several old stories (namely, ad libs) improved! Now over a thousand default words!

## [1.0]

- Officially released!
- Not really different from the prerelease, just has a couple more books, fixed some typos, etc.

## [0.1]

- Prerelease!
- Includes basic functionality.