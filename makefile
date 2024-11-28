all:
	mkdir -p out
	javac -cp "lib/*" -d out src/**/*.java

clean:
	rm -rf out