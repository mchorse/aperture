build_mod: copy_assets build_lang
	./gradlew build

copy_assets:
	rm -f ./src/main/resources/*.md
	cp ./README.md ./src/main/resources/README.md
	cp ./README_CN.md ./src/main/resources/README_CN.md
	cp ./CHANGELOG.md ./src/main/resources/CHANGELOG.md
	cp ./LICENSE.md ./src/main/resources/LICENSE.md

build_lang:
	mkdir -p src/main/resources/assets/aperture/lang
	php php/help.php

check: build_lang
	php php/language.php
