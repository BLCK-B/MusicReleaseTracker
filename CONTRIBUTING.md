Most issues and suggestions can be resolved via issues or discussions tab, respectively.

You can contribute to code, e.g. a bugfix, if you wish. For any larger change and/or guidance, please ask beforehand.

To submit a change:

- create a fork
- clone repository, push your changes to fork
- run tests
- submit a pull request

New tests that verify your change are required, at least where possible.

---

## Setup
- fork
- git clone your fork
- in your IDE, set up Java SDK for project and Gradle
- sync Gradle
- `cd vue` `npm install`

## Tests
Backend tests can be found in `src/test/`. Right click the folder or `./gradlew test` to run them. Frontend tests are 
in `vue/tests/`. They are run with `npm run test`.

## Run

`./gradlew bootRun` - run backend at port set in `application.properties` (you can connect with browser at localhost:port)

`cd vue` `npm run dev` - (needs backend) run frontend

`cd vue` `npm run electron` - (needs backend) run Electron window

## Distribution

If you *need* distribution details, ask or refer to [blog (2024)](https://blck-b.github.io/post/java-native-pipe/).

`cd vue` `npm run buildVue` - build frontend static files

`./gradlew bootJar` - build executable jar in `build/libs`

`./gradlew nativeCompile` - (needs GraalVM) native backend compilation to `build/native/nativeCompile`

scripts in `vue/package.json` - use these to export electron

### No-nonsense distribution

Generate static frontend files: `npm run buildVue`. Build bootJar in `build/libs/`: `./gradlew bootJar`. Run using args below and click through all you can - every setting, every menu. Ctrl+C to shut down. Copy whatever is in `tracing` in same dir over to `vue/buildResources/graal-tracing`. Push. Rest is handled by pipeline.

### Tracing
Generating graal aot `tracing` works for me only by running the generated bootJar with arguments: `java -jar 
-agentlib:native-image-agent=config-output-dir=tracing MRT-X.jar`
