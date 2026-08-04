# Project Specification: H3-Based Fictional Planet Editor

## 1. Working Title

**H3gmark Studio**
A desktop Java/libGDX editor for building, inspecting, and visualising fictional spheroid planets using a hierarchical cell grid.

---

# 2. Core Concept

The application is a two-pane worldbuilding/geography editor. One pane is a live planet view that renders visible cells at an appropriate grid resolution and colours them through a user-selected feature layer. The other is a persistent data and tool pane containing the active-layer controls, paint tools, and detailed data for the current selection. The live view helps visualise changes and select regions; it is not a replacement for the data pane.

The planet is represented primarily by a **hierarchical H3 cell address system**. Each H3 cell ID acts as a stable spatial key for asking:

> “What is it like here?”

A cell can resolve terrain, climate, ecology, political control, settlement data, resources, notes, and procedural detail.

The renderer does not assume every cell is a perfect regular hex. Instead:

```text
H3 cell ID
  ↓
H3 geometry / custom planet geodesy
  ↓
renderable cell polygon on spheroid
```

The data model is sparse and hierarchical. The application must not create or store records for all possible cells; it resolves explicit values, inherited values, and generated defaults only for cells needed by the view or an edit.

---

# 3. Chosen Technology Stack

## 3.1 Language and Runtime

```text
Java
```

Primary rationale:

- Portable.
- Good development ergonomics.
- Compatible with libGDX.
- Good enough performance for custom editor tooling.
- Easier than C++ for early development.

---

## 3.2 Rendering and Application Framework

```text
libGDX
```

Target initial platforms:

```text
Desktop: Windows, Linux, macOS
```

Possible future targets:

```text
Web via GWT/libGDX backend, if feasible
Android/tablet, if useful
```

Initial project should be desktop-first.

---

## 3.3 Graphics Backend

```text
OpenGL via libGDX/LWJGL
```

Constraint:

- Avoid Unity, Unreal, or similarly specific proprietary/game-engine-oriented stacks.
- Keep rendering portable and under project control.

---

## 3.4 H3 Integration

Use Java bindings for Uber H3, e.g.:

```text
com.uber:h3
```

or another maintained H3 Java binding if more appropriate.

The project should wrap H3 behind its own interface so that a custom H3-like grid could replace or supplement H3 later.

---

## 3.5 Supporting Libraries

Potential later dependencies, not architecture commitments:

```text
JTS Topology Suite       geometry/topology operations
SQLite JDBC              local project database
Jackson or Gson          JSON serialization
Kryo or FlatBuffers      optional binary serialization
JOML or libGDX math      vector/matrix math
SLF4J + Logback          logging
JUnit 5                  tests
Gradle                   build system
```

Optional later:

```text
Apache SIS / Proj4J      CRS/projection work
GeoTools                 heavier GIS capabilities
imgui-java               alternative editor UI panels, if needed
```

---

# 4. Design Principles

## 4.1 H3 Cell IDs Are Spatial Addresses

A cell ID is the durable primary spatial key.

Use cell IDs for:

```text
climate records
biome records
political ownership
claims
population/resource values
terrain summaries
procedural seeds
manual overrides
POI indexing
simulation states
```

Do **not** store all possible cells.

---

## 4.2 H3 Is an Addressing System, Not the Whole Planet Model

H3 provides:

```text
cell IDs
parent/child hierarchy
neighbors
rings/disks
cell boundaries
cell centers
```

The application provides:

```text
fictional planet size
spheroid shape
terrain/elevation model
semantic layers
procedural generation
rendering interpretation
physical distances/areas
```

---

## 4.3 Use Custom Planet Geodesy

H3 uses angular latitude/longitude on a sphere-like model.

The editor must allow fictional planet definitions such as:

```text
planet name
equatorial radius
polar radius
mean radius
flattening
rotation period
axial tilt, optional
sea level
world seed
```

H3 lat/lon is interpreted as angular planetary coordinates.

For rendering:

```text
H3 boundary lat/lon
  ↓
fictional spheroid conversion
  ↓
3D Cartesian vertex
```

For physical distances/areas, do not blindly trust H3’s Earth-based metric helpers. Scale or compute through the custom planet model.

---

## 4.4 Accept Pentagon Defects

H3 contains unavoidable pentagonal cells.

The application should:

- not assume every cell has six neighbors,
- not assume every cell boundary has six vertices,
- not assume every child relationship is uniform,
- not assume global hex directions are consistent.

Grid topology must be accessed through a wrapper API.

Pentagons should be treated as ordinary-but-special cells. At high resolutions, they are rare and acceptable.

---

## 4.5 Sparse, Hierarchical Data

The project must support data at mixed H3 resolutions. Each feature definition declares the inclusive resolution range where it applies and may be edited or overridden. A feature outside that range is not stored or directly overridden at that cell resolution; values at finer detail inherit from the nearest applicable ancestor or use the feature's generated rule.

Example:

```text
resolution 3: tectonic plates
resolution 4: climate regions
resolution 5: biome bands
resolution 6: political regions
resolution 8: local ecology
resolution 9+: settlements, farms, POIs, local edits
```

For example, climate features may only apply through regional resolutions, while local settlement or terrain-detail features can apply at much finer resolutions. The grid's maximum resolution does not imply that every feature is meaningful or editable there.

A fine cell resolves its data by consulting:

```text
direct value
nearest ancestor value
regional override
procedural default
manual edit
```

---

## 4.6 Rendering Uses Geometry, Not Assumptions

The renderer must ask the grid system for actual cell geometry.

```java
CellGeometry geometry = planetGrid.geometry(cellId);
```

The renderer should never generate a “regular hex” merely from cell center and radius unless explicitly using a debug/local approximation mode.

---

# 5. Main User-Facing Goals

## 5.1 Live Planet View and Data Pane

The workspace always has a live, rotatable and zoomable planet view beside a data and tool pane. The planet view selects a configurable grid resolution from zoom and renders only cells relevant to the current view. A cell must be renderable even when it has no explicit record, using its resolved layer value.

The live view should support:

```text
show cells at chosen/detail-dependent resolution
select a feature layer for colouring
select cells and apply active tools
inspect resolved cell detail
show relevant overlays and POIs
```

### Data and Tool Pane

The persistent data and tool pane shares selection and active-layer state with the live view. It shows editable values and detailed resolved data for the selected cell or region, and contains the MSPaint-style tool controls used to make changes.

### Optional Projection Views

Initial projection may be simple:

```text
equirectangular lon/lat map
```

Later projections may include:

```text
orthographic
local tangent plane
cube-face style view
selected-region unwrap
```

---

## 5.2 Toggleable Overlays

The user should be able to toggle overlays such as:

```text
elevation
land/water
temperature
rainfall
climate class
biome/ecology
political control
contested claims
population density
resources
settlements/POIs
notes
debug H3 resolution/pentagons
```

An active feature layer resolves each visible cell to a style. Overlays may be composited later, but they are not required to be a general multi-overlay system for the initial editor.

---

## 5.3 “What Is Here?” Cell Inspector

Selecting a cell should show a resolved view:

```text
cell ID
resolution
center lat/lon
parent chain
neighbor cells
terrain summary
climate values
biome/ecology
political control
claims
population/resources
POIs inside cell
manual overrides
procedural seed/debug values
```

---

## 5.4 Procedural Detail

The world should support deterministic generation from:

```text
world seed
cell ID
layer ID
optional parent/neighbor data
```

A cell should be able to answer “what is here?” even if no explicit record exists.

Example:

```java
long seed = hash(worldSeed, cellId, LayerId.TERRAIN_DETAIL);
```

Procedural generation should be stable: the same project file and seed should produce the same results.

---

# 6. Non-Goals for Initial Version

Initial versions should **not** attempt to implement everything.

Explicit non-goals for MVP:

```text
full tectonic simulation
full climate simulation
high-detail natural terrain mesh at human scale
multi-user collaboration
network/server backend
browser deployment
perfect ellipsoidal geodesy
high-performance rendering of millions of cells at once
Unity/Unreal style game runtime
```

---

# 7. Architecture Overview

Proposed high-level structure under the `core` Gradle project. The `lwjgl3` project contains only the LibGDX launcher boilerplate.

```text
planet
  ├── planet model
  ├── grid abstraction
  ├── H3-backed grid implementation
  ├── layer system
  ├── procedural resolver
  ├── storage model
  └── domain types

editor
  ├── libGDX application
  ├── renderer
  ├── UI
  ├── input tools
  ├── selection system
  └── editor commands
```

Every module should contain tests for itself.

---

# 8. Core Interfaces

## 8.1 PlanetGrid

Wrap H3 behind a project-owned abstraction.

```java
public interface PlanetGrid {
    CellId cellAt(PlanetLatLon coord, int resolution);

    PlanetLatLon center(CellId cell);

    CellGeometry geometry(CellId cell);

    CellId parent(CellId cell, int parentResolution);

    List<CellId> children(CellId cell, int childResolution);

    List<CellId> neighbors(CellId cell);

    List<CellId> disk(CellId center, int radius);

    List<CellId> cellsCovering(PlanetRegion region, int resolution);

    int resolution(CellId cell);

    boolean isPentagon(CellId cell);

    long stableSeed(CellId cell, String layerId);
}
```

This prevents the rest of the app from depending directly on H3 assumptions.

---

## 8.2 CellId

A wrapper around H3’s native index.

```java
public final class CellId {
    private final long h3;

    public long asLong();
    public String asHexString();

    public static CellId fromLong(long value);
    public static CellId fromHexString(String value);
}
```

Later, this could become more generic if replacing H3.

---

## 8.3 CellGeometry

```java
public final class CellGeometry {
    public final CellId cellId;
    public final int resolution;

    public final PlanetLatLon center;
    public final List<PlanetLatLon> boundaryLatLon;
    public final List<Vector3> boundaryCartesian;

    public final double approximateArea;
    public final boolean pentagon;
}
```

Important:

- Boundary vertex count must be variable.
- Renderer must support pentagons and possibly extra boundary points.

---

## 8.4 PlanetModel

```java
public final class PlanetModel {
    public String name;

    public double equatorialRadiusMeters;
    public double polarRadiusMeters;
    public double meanRadiusMeters;

    public double seaLevelMeters;

    public long worldSeed;

    public Vector3 toCartesian(PlanetLatLon coord, double elevationMeters);

    public double approximateCellArea(CellId cell);
}
```

---

# 9. Data Layer Model

## 9.1 Layer Types

The editor should support multiple cell-indexed features. A feature definition includes its value type and an inclusive minimum and maximum grid resolution for application and direct editing. The active tool must reject or clearly disable edits outside that feature's range.

Initial layer types:

```text
scalar numeric layer
categorical layer
boolean/mask layer
political/control layer
POI layer
manual note layer
```

Examples:

```text
elevation_mean
temperature_mean
rainfall_annual
biome_id
political_controller
contested_claims
population_density
resource_score
```

---

## 9.2 Cell Value Resolution

Layer value resolution should be hierarchical within the feature's declared resolution range. Direct values and manual overrides are considered only at applicable resolutions.

Conceptual algorithm:

```java
ResolvedCellState resolve(CellId cell) {
    ResolvedCellState state = proceduralValues(cell);

    for (CellId ancestor : grid.ancestorChain(cell)) {
        state.apply(layerValuesAt(ancestor));
    }

    state.apply(layerValuesAt(cell));
    state.apply(manualOverrides(cell));

    return state;
}
```

Resolution order should be explicit per layer. The default priority is:

```text
1. generated base value
2. coarse-to-fine ancestor values
3. direct cell value
4. manual override
```

---

## 9.3 Sparse Storage

Only explicit values are stored.

Example table:

```sql
cell_layer_value
  cell_id TEXT
  layer_id TEXT
  value_type TEXT
  value_json TEXT
  updated_at INTEGER
```

For high-volume numeric layers, later optimize to binary chunks or typed tables.

---

## 9.4 Compacted Regions

Support mixed-resolution cell sets for large regions.

Useful for:

```text
political territories
biomes
climate regions
continents
oceans
claims
restricted zones
```

Example conceptual record:

```json
{
  "regionId": "kingdom_var_control",
  "layerId": "political_control",
  "value": "Kingdom of Var",
  "cells": [
    "832830fffffffff",
    "8428347ffffffff",
    "85283473fffffff"
  ]
}
```

The cell list should be H3-compacted where possible.

---

# 10. Points of Interest

POIs should be indexed by containing cell but may have local position.

```java
public final class PointOfInterest {
    public UUID id;
    public String name;
    public PoiType type;

    public CellId indexCell;

    public PlanetLatLon position;
    public double elevationMeters;

    public Map<String, Object> properties;
}
```

Examples:

```text
city
farm
ruin
mine
port
fortress
battlefield
sacred site
landmark
custom marker
```

Rendering can either place the marker at its explicit position or at the cell center if no finer position exists.

---

# 11. Rendering Requirements

## 11.1 Globe Renderer

The globe renderer should:

```text
render spheroid planet
render visible H3 cells
support overlay colors
support cell outlines
support selected/highlighted cells
support POI markers
support zoom-dependent resolution selection
```

Initial rendering may be simple:

```text
cell polygons drawn on a sphere/spheroid
flat color per cell
wireframe outlines
```

Later:

```text
terrain height displacement
smooth terrain mesh
atmosphere/ocean rendering
labels
roads/rivers/borders
```

---

## 11.2 2D Map Renderer

Initial 2D map:

```text
equirectangular projection
longitude x-axis
latitude y-axis
```

Should support:

```text
visible cells
overlay colors
cell outlines
selection
POIs
pan/zoom
```

Later projection support should be behind an interface:

```java
public interface MapProjection {
    Vector2 project(PlanetLatLon coord);
    PlanetLatLon unproject(Vector2 point);
}
```

---

## 11.3 Viewport Cell Selection

Two strategies should be implemented eventually:

### Local View

```text
cellAt(cursor/center)
disk(centerCell, k)
```

Useful for local submaps.

### Region Covering

```text
cellsCovering(viewportPolygon, resolution)
```

Useful for map panes and broad views.

---

## 11.4 LOD Policy

The renderer should choose H3 resolution based on zoom.

Example rough policy:

```text
global view:       res 2–4
continental view:  res 4–6
regional view:     res 6–8
local view:        res 8–10
hyperlocal view:   res 10+
```

Actual values should be configurable.

---

# 12. Editor UI Requirements

Avoid Scene2D UI for the initial editor. Render simple controls directly with libGDX alongside the live view and route input through the editor's own tool and hit-testing system. This keeps the initial UI lightweight and makes the interaction model explicit.

The initial layout is a permanent two-pane editor: a live planet view and a data/tool pane. It does not need docking or arbitrary panel layouts.

```text
live planet view
data/tool pane: active layer, tool, brush, value controls, and selected-cell details
shared status: current cell, coordinates, resolution, messages
```

---

## 12.1 Main Panels

The data/tool pane contains these required early controls and detail views:

```text
active-layer selector
MSPaint-style tool palette
tool settings and paint-value controls
cell inspector
project/planet settings
debug grid information
```

---

## 12.2 Tools

The toolbox must make the active tool and its value obvious. Initial tools:

```text
select and inspect
paint a layer value onto a cell or brush footprint
erase a manual override
pan/orbit/zoom
change visible grid resolution
```

Later tools:

```text
region fill and polygon selection
place and edit POIs
political borders, rivers, and roads
bulk procedural regeneration and layer smoothing
import/export
```

---

# 13. Storage Specification

## 13.1 Project File

Initial storage can be:

```text
directory project
```

Example:

```text
MyPlanet.h3gmark/
  project.json
  planet.json
  layers.json
  cell-data/
  pois/
  assets/
  thumbnails/
```

Later, this can be packed into a single archive.

---

## 13.2 Persistent Storage

Persistent storage must support sparse cell records, POIs, and project metadata. SQLite is a candidate implementation, not an MVP architecture requirement.

If SQLite is chosen, possible tables include:

```sql
planet (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL
);

layers (
    layer_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    metadata_json TEXT
);

cell_values (
    cell_id TEXT NOT NULL,
    layer_id TEXT NOT NULL,
    value_json TEXT NOT NULL,
    updated_at INTEGER NOT NULL,
    PRIMARY KEY (cell_id, layer_id)
);

regions (
    region_id TEXT PRIMARY KEY,
    layer_id TEXT NOT NULL,
    name TEXT NOT NULL,
    value_json TEXT,
    metadata_json TEXT
);

region_cells (
    region_id TEXT NOT NULL,
    cell_id TEXT NOT NULL,
    PRIMARY KEY (region_id, cell_id)
);

pois (
    poi_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    cell_id TEXT NOT NULL,
    lat REAL,
    lon REAL,
    elevation REAL,
    properties_json TEXT
);
```

---

# 14. Procedural Generation

## 14.1 Determinism

Every generated value should be reproducible from:

```text
project version
planet seed
cell ID
layer ID
algorithm version
```

Include algorithm versioning so future changes can be managed.

---

## 14.2 Initial Procedural Layers

MVP procedural layers can be simple:

```text
elevation noise
temperature by latitude + noise
rainfall by latitude/noise
biome from temperature/rainfall
```

These need not be physically realistic initially.

---

## 14.3 Neighbor-Aware Generation

Some layers may use neighboring cells.

Examples:

```text
smoothing elevation
biome transition
local hydrology hints
political influence diffusion
```

All neighbor access must go through `PlanetGrid.neighbors(cell)`.

---

# 15. Constraints and Assumptions

## 15.1 Do Not Store the Whole Planet

Even at moderate H3 resolutions, global cell counts are huge.

The system must operate with:

```text
visible cells
explicitly edited cells
compacted regions
generated-on-demand values
cached results
```

---

## 15.2 No Perfect-Hex Assumption

Because H3 has pentagons and spherical distortion:

```text
no fixed six-neighbor assumption
no fixed six-boundary-vertex assumption
no global direction assumption
```

---

## 15.3 H3 Is Earth-Oriented Only in Metrics

H3 indexing can be used for fictional planets.

But H3 metric functions are Earth-scaled. The project must route physical size calculations through `PlanetModel`.

---

## 15.4 Desktop First

The first implementation should prioritize:

```text
Java desktop app
libGDX LWJGL backend
OpenGL rendering
local files
single user
```

---

# 16. MVP Definition

The MVP should demonstrate the core idea rather than full worldbuilding capability.

## MVP Features

1. Create/open/save a fictional planet project.
2. Define planet:
    - name,
    - radius,
    - polar/equatorial scaling,
    - world seed.
3. Show a live spheroid planet view.
4. Render visible cells at a selectable or zoom-derived resolution, coloured by the active feature layer.
5. Select a cell and open its detailed inspector.
6. Display:
    - H3 ID,
    - resolution,
    - center lat/lon,
    - whether pentagon,
    - parent,
    - neighbors.
7. Select at least three initial feature layers:
    - elevation,
    - biome,
    - political control.
8. Use a paint tool to apply and erase values on selected cells or brush footprints.
9. Persist and restore painted overrides using the selected sparse storage implementation.
10. Deterministically generate default cell values from seed.

---

# 17. Suggested Development Milestones

## Milestone 1: Core Grid Prototype

```text
Gradle project
H3 Java binding integrated
PlanetGrid interface
CellId wrapper
cellAt/center/boundary/neighbors/parent/children
basic tests
```

---

## Milestone 2: Planet Model

```text
PlanetModel
lat/lon to spheroid Cartesian conversion
radius scaling
basic cell geometry generation
tests for coordinates
```

---

## Milestone 3: libGDX Rendering Prototype

```text
desktop app launches
render sphere/spheroid
draw H3 cells at fixed resolution
camera orbit/zoom
cell picking rough implementation
```

---

## Milestone 4: Editor Tools and Detail Views

```text
permanent data/tool pane with direct-rendered controls
selection and cell inspector
active-layer selector
paint and erase tools
```

---

## Milestone 5: Layer System

```text
Layer interface
procedural elevation layer
procedural climate/biome layer
manual override layer
overlay coloring
cell inspector
```

---

## Milestone 6: Storage

```text
project directory format
chosen sparse storage schema
save/load planet metadata
save/load cell overrides
save/load POIs
```

---

## Milestone 7: Optional Projection View

```text
equirectangular map
draw cell polygons
pan/zoom
shared selection with live view
```

---

## Milestone 8: Usability Pass

```text
menus
keyboard shortcuts
basic undo/redo
status bar
error handling
debug overlays
```

---

# 18. Testing Requirements

Important test areas:

```text
Cell ID serialization
H3 parent/child consistency
neighbor queries, including pentagons
boundary conversion to Cartesian
planet radius scaling
procedural determinism
layer inheritance
manual override precedence
persistence save/load
POI indexing
```

Pentagon-specific tests are mandatory.

Example tests:

```text
select a known H3 pentagon
verify side/boundary count is not assumed to be 6
verify neighbor count handling
verify rendering geometry does not crash
verify disk query near pentagon succeeds
```

---

# 19. Future Extensions

Potential later features:

```text
custom H3-like grid backend
128-bit deeper cell IDs
true custom spheroid DGGS
advanced climate simulation
tectonic plate simulation
hydrology/rivers
roads/routes
political history timeline
contested claims model
culture/language/religion overlays
exports to GeoJSON/GeoPackage
import raster/vector data
smooth terrain rendering
local high-detail procedural terrain
scriptable generation
mod/plugin API
browser/web export
```

---

# 20. Summary

The agreed project direction is:

```text
Java + libGDX desktop editor with direct-rendered initial controls
H3 Java bindings
H3 cell IDs as sparse hierarchical spatial addresses
fictional planet model for physical interpretation
two-pane editor: live detail-dependent planet view plus persistent data/tool pane
MSPaint-style toolbox, cell inspector, and paint tools
procedural "what is here?" resolver
sparse persisted project storage
no Unity/Unreal
no assumption of perfect all-hex topology
```

The most important architectural rule is:

> Treat H3 as the address and topology system, not as the whole world model.

The application should always separate:

```text
cell identity
cell topology
cell geometry
cell semantic data
rendering
```

That separation will make the MVP feasible while leaving room for a custom H3-like fictional planet grid later.
