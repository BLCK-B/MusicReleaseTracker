Most issues and suggestions can be resolved via issues or discussions tab, respectively.

You can contribute to code, e.g. a bugfix, if you wish. For any larger change and/or guidance, please ask beforehand.

- create a fork
- clone repository, push your changes to fork
- run tests
- submit a pull request

---

## Setup

- fork
- git clone your fork
- in IDE, set up Java SDK for project and Gradle
- sync Gradle
- `cd vue` `npm install`

## Tests

Backend tests can be found in `src/test/`. Right click the folder or `./gradlew test` to run them.

## Run

`./gradlew bootRun` - run backend at port set in `application.properties` (you can connect with browser at localhost:
port)

`cd vue` `npm run dev` - (needs backend) run frontend

`cd vue` `npm run electron` - (needs backend) run Electron window

## Distribution

`cd vue` `npm run build` - build frontend static files

`./gradlew bootJar` - build executable jar in `build/libs`

`./gradlew nativeCompile` - (needs Graal JDK) native backend compilation to `build/native/nativeCompile`

scripts in `vue/package.json` - use these to export electron

### Pipeline

Distribution is manually triggered in [Actions](https://github.com/BLCK-B/MusicReleaseTracker/blob/main/.github/workflows/Distribution.yml). Build-related resources are located in `vue/buildResources`, which is also used by the pipeline. No steps are needed to make a release, just run the workflow. App version is specified in `application.properties` and in `package.json`.
