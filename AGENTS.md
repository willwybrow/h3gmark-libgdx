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
- Features implement the project-owned `Feature` contract with separate viewable and settable resolution ranges. Read-only computed features are exposed through `ProvidedFeatures`, declare their own view range, and resolve typed dependencies by navigating the target `Cell` rather than storage.
- Every feature lives in its own subpackage under `feature` (for example, `feature.elevation`). A feature class exposes no public instance methods beyond methods declared by `Feature`; capability interfaces such as `StoredFeature`, `ComputedFeature`, and `ProvidedFeatures` are markers, not alternate public APIs. Keep feature-specific helpers package-private or private.
- Coarse feature edits use explicit `FILL GAPS` (preserve descendants through sparse inheritance) or `OVERWRITE` (remove descendant overrides) semantics. Feature-owned aggregation produces transient display values through batched change events; never persist or treat aggregate caches as explicit data.

## UI Direction

- The initial editor has a permanent two-pane layout: a live planet view with zoom-dependent, active-layer cell colouring and a data/tool pane with the editable selected-cell data. The live view supports visualisation and region selection; it does not replace the data pane.
- Avoid Scene2D initially. Render simple controls directly and keep tool input/hit-testing under the editor's own control; the toolbox is MSPaint-style and supports selection, inspection, painting, and erase operations.

## Code Design and Tests

- Keep domain, rendering, UI/input, and persistence responsibilities isolated. Depend on narrow project-owned interfaces at boundaries, especially around the grid, feature resolution, and storage; do not leak H3 or libGDX types through domain APIs without a concrete need.
- Apply SOLID where it reduces coupling or makes a changing concern independently testable. Extract only genuinely shared or independently variable logic; do not create abstractions merely to satisfy a pattern.
- Do not duplicate domain rules such as resolution applicability, cell-value resolution, or geometry conversion. Give each rule one authoritative implementation and reuse it from tools, rendering, and persistence.
- Use domain language literally: child cells have feature values; there is no "child feature" concept. Before introducing an abstraction, verify that its name represents a real domain concept and that the dependency direction follows the behavior owner.
- Before designing services or DTOs, identify the ubiquitous-language entities, value objects, and aggregate roots. Prefer behavior-rich domain objects when identity, navigation, state, and invariants naturally belong together; do not reduce an entity to an ID passed among procedural services.
- Keep complete domain decisions with their owner. Features receive the target `Cell` and navigate its relationships or typed feature dependencies themselves; infrastructure may trigger/cache behavior but must not precompute its decision inputs. Avoid service-locator interfaces and generic context bags when an aggregate relationship expresses the model directly.
- For every new abstraction, ask: "Would a domain expert name this?", "Who owns the invariant and input selection?", and "Does this make the code read like the product model?" Prefer screaming architecture and DDD boundaries over generic data-processing shapes.
- Domain and application commands must not be `void` methods that either succeed silently or throw. Return an explicit outcome such as a boolean, affected count, enum, or result type, and require callers to act on it. Do not use exceptions for expected control flow; reserve them for violated invariants, programming errors, and unrecoverable failures. Framework lifecycle callbacks and genuinely unconditional side effects are exempt.
- Add focused automated tests for every nontrivial domain feature and regression. Cover boundary cases such as pentagons, feature resolution limits, hierarchy/override precedence, sparse resolution, and deterministic generation; test rendering or input seams through small deterministic units where practical.
