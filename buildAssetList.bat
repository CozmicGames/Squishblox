@echo off
@echo Building texture asset list...
break>"assets/textures.txt"
for %%f in ("assets/textures/*.png", "assets/textures/*.jpg", "assets/textures/*.bmp") do (
	@echo textures/%%f>> "assets/textures.txt"
)
for %%f in ("assets/textures/bunker/*.png", "assets/textures/bunker/*.jpg", "assets/textures/bunker/*.bmp") do (
	@echo textures/bunker/%%f>> "assets/textures.txt"
)
for %%f in ("assets/textures/characters/*.png", "assets/textures/characters/*.jpg", "assets/textures/characters/*.bmp") do (
	@echo textures/characters/%%f>> "assets/textures.txt"
)
for %%f in ("assets/textures/items/*.png", "assets/textures/items/*.jpg", "assets/textures/items/*.bmp") do (
	@echo textures/items/%%f>> "assets/textures.txt"
)
for %%f in ("assets/textures/tiles/*.png", "assets/textures/items/*.jpg", "assets/textures/tiles/*.bmp") do (
	@echo textures/tiles/%%f>> "assets/textures.txt"
)
for %%f in ("assets/textures/world/*.png", "assets/textures/world/*.jpg", "assets/textures/world/*.bmp") do (
	@echo textures/world/%%f>> "assets/textures.txt"
)
for %%f in ("assets/textures/player/*.png", "assets/textures/player/*.jpg", "assets/textures/player/*.bmp") do (
	@echo textures/player/%%f>> "assets/textures.txt"
)
for %%f in ("assets/textures/char_idle/*.png", "assets/textures/char_idle/*.jpg", "assets/textures/plachar_idleyer/*.bmp") do (
	@echo textures/char_idle/%%f>> "assets/textures.txt"
)
@echo Building shader asset list...
break>"assets/shaders.txt"
for %%f in ("assets/shaders/*.shader2d", "assets/shaders/*.shader3d") do (
	@echo shaders/%%f>> "assets/shaders.txt"
)
@echo Building font asset list...
break>"assets/fonts.txt"
for %%f in ("assets/fonts/*.fnt") do (
	@echo fonts/%%f>> "assets/fonts.txt"
)
@echo Building item type asset list...
break>"assets/itemTypes.txt"
for %%f in ("assets/itemTypes/*.json") do (
	@echo itemTypes/%%f>> "assets/itemTypes.txt"
)
@echo Building recipe asset list...
break>"assets/recipes.txt"
for %%f in ("assets/recipes/*.json") do (
	@echo recipes/%%f>> "assets/recipes.txt"
)
@echo Building languages asset list...
break>"assets/languages.txt"
for %%f in ("assets/languages/*.csv") do (
	@echo languages/%%f>> "assets/languages.txt"
)
@echo Building sounds asset list...
break>"assets/sounds.txt"
for %%f in ("assets/sounds/*.wav", "assets/sounds/*.ogg", "assets/sounds/*.mp3") do (
	@echo sounds/%%f>> "assets/sounds.txt"
)