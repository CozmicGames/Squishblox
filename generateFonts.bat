for %%f in (assets/fonts/*.ttf) do (
	java -jar tools/msdf/msdfgen.jar -g tools/msdf/msdfgen.exe -t msdf -a none -d 1024 1024 -s 48 -r 8 -c latin-9 -o assets/fonts assets/fonts/%%f
)