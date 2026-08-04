# Hegmark Specification

## 1. Product Scope

Hegmark is a desktop Java/libGDX editor for building and inspecting fictional spheroid planets through a hierarchical cell grid.

The editor always has two panes:

- a live planet pane for visualising feature values and selecting cells or regions;
- a persistent data/tool pane for inspecting resolved data and making edits.

The live view is a spatial editing aid. It does not replace the detailed data pane.

The initial application is local, single-user, and desktop-first. Browser deployment, collaboration, full physical simulation, and high-detail terrain meshes are deferred.

## 2. Grid and Planet Model

### 2.1 Project-Owned Grid

Uber H3 is the initial grid implementation, but application code depends on a project-owned grid interface. H3 types and assumptions must not leak into features, persistence, editor tools, or rendering.

The grid abstraction supplies:

- stable cell addresses;
- resolution and parent/child hierarchy;
- centers and actual variable-length boundaries;
- neighbors and bounded local disks;
- pentagon detection;
- deterministic cell seeds.

The complete grid must never be materialized at fine resolutions. Work is limited to visible cells, explicit sparse values, and bounded hierarchy queries.

### 2.2 Planet Aggregate and Cells

`Planet` is the aggregate root for a world. It owns the planet definition, grid adapter, feature registry, sparse explicit values, and transient derived state.

`Cell` is the domain entity representing one spatial location within a planet. It is created on demand and exposes:

- its `CellId`, resolution, center, and geometry;
- optional parent, children, neighbors, and bounded disks as `Cell` objects;
- explicitly stored features on that exact cell;
- effective and display values for stored features;
- computed provided features;
- descendant-override queries needed by feature behavior.

Cell identity is its planet plus `CellId`. Creating a `Cell` never materializes its descendants or creates persisted state.

### 2.3 Topology

Pentagons are ordinary supported cells. No code may assume:

- six neighbors;
- six boundary vertices;
- uniform child counts;
- globally consistent hex directions.

### 2.4 Fictional Geodesy

Grid latitude and longitude are angular addresses, not Earth geometry. A planet definition owns equatorial radius, polar radius, sea level, world seed, and other physical properties.

Cell boundaries are converted through the fictional spheroid model before rendering. Physical distances and areas must be calculated by the planet model, never by Earth-scaled H3 metric helpers.

Domain APIs use project-owned coordinate and geometry types rather than libGDX vectors.

## 3. Feature Model

### 3.1 Feature Contract

Every cell-indexed feature implements `Feature<T>` and exposes:

- a stable ID and display name;
- its value type;
- an inclusive viewable grid-resolution range;
- an optional inclusive settable grid-resolution range;
- an optional feature-owned aggregation strategy.

The settable range must be contained by the viewable range. A feature may therefore be visible at resolutions where it cannot be directly edited.

Examples include elevation, temperature, biome, political control, and notes. The grid's maximum resolution does not imply that every feature is meaningful or editable there.

### 3.2 Sparse Explicit Values

Only explicit user values are stored. Setting a coarse cell does not create records for every descendant.

For any cell, distinguish:

- **stored value**: an explicit value on that exact cell;
- **effective value**: direct value, nearest applicable ancestor value, or deterministic generated default;
- **display value**: a cached feature-defined summary of descendant display/effective values, falling back to the effective value when no summary exists.

Tools and inheritance use effective values. Rendering and computed features use display values. The inspector exposes both when they differ.

At resolutions finer than a feature's settable maximum, values inherit from the nearest applicable ancestor. At coarser view-only resolutions, values may be summarized through feature aggregation.

### 3.3 Deterministic Defaults

Generated values are stable for the same:

- world seed;
- cell address;
- feature ID;
- algorithm version.

Generation remains a fallback and is never persisted merely because a cell was viewed.

### 3.4 Computed Features

A feature may implement `ProvidedFeatures` to expose virtual `ComputedFeature<T>` instances. Provided features:

- are read-only;
- declare their own viewable range;
- are registered recursively with duplicate IDs rejected;
    - hold typed feature dependencies directly;
    - resolve dependencies and inspect topology through the target `Cell`;
- never read storage directly.

For example, a tectonic-plate feature can provide `fault_line` by comparing the resolved plate of a cell with its neighbors. Elevation can provide `land` by comparing displayed elevation with sea level.

Computed values are evaluated on demand. Any cache for them is derived and non-persistent.

## 4. Editing and Aggregation

### 4.1 Edit Resolution

Direct editing is rejected outside the active feature's settable range.

At the feature's finest settable resolution, painting writes the selected cell directly. At a coarser settable resolution, the user explicitly chooses how existing descendant detail is handled.

### 4.2 Fill Gaps

`FILL GAPS` performs a sparse top-down edit:

- write the value once on the selected cell;
- preserve all existing descendant overrides;
- let descendants without a nearer override inherit the new value.

This is the normal tool for adding a regional baseline without destroying local detail.

### 4.3 Overwrite

`OVERWRITE` performs a destructive top-down edit:

- write the value on the selected cell;
- remove every explicit descendant value for that feature within the selected subtree;
- preserve ancestors, siblings, and other features;
- clear derived summaries for the overwritten subtree.

When descendant values will be removed, the UI reports how many and requires explicit confirmation.

Erasing a cell removes only that cell's stored value unless a separate destructive subtree action is chosen.

### 4.4 Feature-Owned Aggregation

Aggregation is defined by each feature rather than inferred from its Java value type. The feature receives the `Cell` being summarized. The feature itself navigates that cell's children, neighbors, or other relationships, resolves typed feature dependencies through those cells, and performs the calculation. Generic resolver infrastructure must not preselect or precompute an aggregation feature's inputs.

Aggregation must support variable topology and deterministic results independent of traversal order.

Each feature also identifies which aggregate cells are affected by a mutation. A child-based feature may return ancestors; a neighbor-based feature may include adjacent cells. Cache infrastructure follows that feature-owned dependency policy rather than assuming all aggregations use descendants.

Initial elevation aggregation is arithmetic mean. Other features may choose majority, minimum, maximum, sum, `all`, `any`, a threshold, or no aggregation.

When descendants change, a parent's stored value remains its fallback for descendants without overrides. Its display value may differ because it summarizes the children's resulting values.

Example: if every child resolves below sea level, their parent elevation summary is also below sea level and the parent renders as water when zoomed out.

### 4.5 Events and Derived Cache

Feature mutations publish one immutable batch event after storage changes complete. Events identify the feature, operation, changed cells, and removed descendants.

A synchronous application-thread subscriber maintains an in-memory display-value cache:

1. deduplicate affected ancestors;
2. process them deepest-first;
3. invoke the owning feature with the parent `Cell`;
4. let the feature navigate and resolve its required cells;
5. cache the resulting parent display value;
6. notify rendering about visible affected cells.

The cache is derived state and is never persisted. Cache loss must not corrupt meaning: on a cache miss where explicit descendants exist, display resolution lazily reconstructs the required affected branch.

Events are published once per command or brush batch, not once per cell. Rejected and no-op edits do not publish changes.

## 5. Rendering and Camera

### 5.1 Cell Surface

The visible planet is formed directly from actual grid cell polygons converted through the fictional spheroid. There is no rendered sphere behind them.

Rendering supports:

- flat feature-derived cell colours;
- cell outlines and selected-cell highlighting;
- variable boundary lengths and pentagons;
- depth testing and back-face culling;
- bounded visible-cell selection;
- zoom-dependent grid resolution.

Every visible cell can render without an explicit record by resolving its display value.

### 5.2 Picking

An unrendered mathematical spheroid is used for ray intersection and camera targeting. Picking converts the intersection to planetary coordinates and asks the grid for the cell at the current display resolution.

### 5.3 Camera Controls

The camera orbits while pointing at the spheroid center. Controls include:

- mouse drag orbit;
- mouse-wheel zoom;
- zoom-in and zoom-out buttons using the same zoom behavior;
- longitude rotation buttons.

Button rotation preserves camera distance and latitude. Its longitude increment shrinks as visible grid resolution increases.

## 6. Editor UI

Initial controls are rendered directly with libGDX. Scene2D, docking, and a generic panel framework are deferred.

The MSPaint-style data/tool pane contains:

- active feature and colour meaning;
- select, fill-gaps, overwrite, and erase tools;
- feature-specific paint values and tool settings;
- camera buttons;
- selected-cell identity, topology, and coordinates;
- stored, effective, and display feature values and their sources;
- editability range and descendant override count;
- computed provided-feature values;
- operation status and overwrite confirmation.

Input is routed to UI controls before the live view. Selection and active-feature state are shared by both panes.

## 7. Persistence Boundary

The current vertical slice keeps explicit edits in memory. The architecture must allow a later project format to persist:

- planet metadata and seed;
- feature definitions and versions;
- sparse explicit feature values;
- points or other project entities introduced later.

Generated values, computed features, display summaries, render meshes, and caches are rebuildable and are not persisted.

No storage implementation may require materializing every grid cell.

## 8. Architecture and Verification

Keep domain, grid adapter, feature resolution, mutation, rendering, UI/input, and persistence responsibilities isolated behind project-owned interfaces.

Authoritative rules such as resolution applicability, inheritance, subtree mutation, aggregation, and geometry conversion must have one implementation reused by tools, rendering, and persistence.

Focused automated tests are required for nontrivial behavior, including:

- cell serialization and fictional coordinate conversion;
- hierarchy, pentagons, variable boundaries, neighbors, and child counts;
- viewable/settable range boundaries;
- deterministic generation;
- direct and ancestor precedence;
- fill-gaps preservation;
- overwrite subtree deletion and isolation from siblings/features;
- effective versus display values;
- event batching and no-op behavior;
- cache invalidation and lazy reconstruction;
- multi-level and pentagon aggregation;
- computed-feature lookup and independent ranges;
- mesh triangulation/winding, picking, LOD, and control hit testing.

## 9. MVP Vertical Slice

The first vertical slice demonstrates:

- a cell-only live spheroid view;
- zoom-dependent H3 detail;
- a permanent direct-rendered data/tool pane;
- deterministic elevation colouring with water below sea level and land above it;
- elevation view/set ranges;
- selection and inspection;
- fill-gaps, overwrite, and erase operations;
- event-driven elevation summaries across zoom levels;
- computed land state;
- orbit, wheel zoom, button zoom, and resolution-aware longitude rotation;
- in-memory sparse values with comprehensive unit tests.

Optional projection views, persistent project files, POIs, smooth terrain, advanced simulations, and additional feature types follow after this slice establishes the core semantics.
