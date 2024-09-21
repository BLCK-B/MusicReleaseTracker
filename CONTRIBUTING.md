Most issues and suggestions can be resolved via issues or discussions tab, respectively.

You can contribute to code, e.g. a bugfix, if you wish. For any larger change and/or guidance, please ask beforehand.

To submit a change:

- create a fork
- clone repository, push your changes to fork
- verify passing tests
- submit a pull request

Tests that verify your change are required, at least where possible.

---

## Setup
- git clone fork
- in your IDE, set Java SDK or GraalVM of project and Gradle
- refresh Gradle
- `cd vue` `npm install`

## Tests
Currently, I only have backend unit tests in `src/test`. Right click the folder or `./gradlew test` to run them.

## Run

`./gradlew bootRun` - run Java backend at port set in `application.properties` (you can connect with browser at localhost:port)

`cd vue` `npm run dev` - (needs backend) run Vite frontend and `o + enter` to open in browser

`cd vue` `npm run electron` - (needs backend) run Electron window

## Distribute

If you *need* distribution details, please refer to [blog (2024)](https://blck-b.github.io/post/java-native-pipe/).

`cd vue` `npm run buildVue` - build frontend static files

`./gradlew bootJar` - build executable jar in `build/libs`

`./gradlew nativeCompile` - (needs GraalVM) native backend compilation to `build/native/nativeCompile`

scripts in `vue/package.json` - use these to export electron
