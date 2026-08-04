# Hegmark Agent Guide

## Build and Run

- Use the checked-in Gradle wrapper: `./gradlew :core:compileJava` for a focused compile, `./gradlew build` for the full build, and `./gradlew :lwjgl3:run` to launch the desktop app.
- Java compilation targets release 25. Ensure Gradle runs on JDK 25; the Foojay resolver plugin is applied, but this build does not declare a Java toolchain.
- There is no lint or formatter task. Place tests in `core/src/test/java`; run one with `./gradlew :core:test --tests 'fully.qualified.TestName'`.
- `:lwjgl3:jar` creates the runnable, dependency-bundled JAR in `lwjgl3/build/libs/`; `jarLinux`, `jarMac`, and `jarWin` produce platform-trimmed variants.
- The Construo package targets download JDK 21 while project classes target Java 25. Reconcile that mismatch before relying on `package*` distribution tasks.

## Layout and Resources

- Put platform-neutral application and domain code in `core`; `lwjgl3` is the LWJGL3 desktop launcher and depends on `core`. Treat everything under `lwjgl3/` as immutable and read-only.
- The desktop entrypoint is `dev.wycobar.hegmark.lwjgl3.Lwjgl3Launcher`, which instantiates `dev.wycobar.hegmark.Main`.
- Put runtime assets in the root `assets/` directory. Only `lwjgl3` adds that directory to its resource source set, and its `run` task uses `assets/` as the working directory.
- `processResources` regenerates `assets/assets.txt` from the asset directory for every subproject. Treat it as generated and do not edit or commit it.
- Preserve `StartupHelper.startNewJvmIfRequired()` in the desktop launch path; it supplies required macOS and Linux/NVIDIA LWJGL startup handling.

## Domain Constraints

- `development/Spec.md` is the project design source. Keep H3 behind a project-owned grid abstraction so a custom grid can replace or supplement it.
- Treat H3 IDs as spatial addresses/topology, not physical planet geometry. Convert H3 angular coordinates through the fictional planet model; do not use Earth-scaled H3 metrics as physical distances or areas.
- Support pentagons and variable boundary/neighbor counts. Never assume six neighbors, six vertices, uniform child counts, or global hex directions.
- Keep world data sparse and hierarchical: resolve explicit cell values and ancestor overrides over deterministic generated defaults; never materialize every possible H3 cell. Each feature declares the grid-resolution range where it applies and may be directly edited, so do not allow every feature to be overridden down to maximum H3 detail.

## UI Direction

- The initial editor has a permanent two-pane layout: a live planet view with zoom-dependent, active-layer cell colouring and a data/tool pane with the editable selected-cell data. The live view supports visualisation and region selection; it does not replace the data pane.
- Avoid Scene2D initially. Render simple controls directly and keep tool input/hit-testing under the editor's own control; the toolbox is MSPaint-style and supports selection, inspection, painting, and erase operations.

## Code Design and Tests

- Keep domain, rendering, UI/input, and persistence responsibilities isolated. Depend on narrow project-owned interfaces at boundaries, especially around the grid, feature resolution, and storage; do not leak H3 or libGDX types through domain APIs without a concrete need.
- Apply SOLID where it reduces coupling or makes a changing concern independently testable. Extract only genuinely shared or independently variable logic; do not create abstractions merely to satisfy a pattern.
- Do not duplicate domain rules such as resolution applicability, cell-value resolution, or geometry conversion. Give each rule one authoritative implementation and reuse it from tools, rendering, and persistence.
- Add focused automated tests for every nontrivial domain feature and regression. Cover boundary cases such as pentagons, feature resolution limits, hierarchy/override precedence, sparse resolution, and deterministic generation; test rendering or input seams through small deterministic units where practical.
