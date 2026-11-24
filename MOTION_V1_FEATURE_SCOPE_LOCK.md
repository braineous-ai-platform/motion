# Motion V1 — Feature Scope Lock

**Decision date:** November 22, 2026  
**Phase:** Feature discovery — COMPLETE / LOCKED  
**Status:** Product feature scope locked; detailed architecture, integration contracts, and implementation not authorized.

## 1. Context and Product Boundaries

Motion is the enterprise-facing system built on **Loom — Cognitive Runtime**. Loom consists of Arc (Intelligence Bridge), Axis/CGO (cognitive graph, Nexus, Context and validation), KDD (infrastructure), and Beam (Cognitive Transaction Manager, or TM). Motion is not a fifth Loom component.

The V1 feature phase identifies the enterprise observability experience and developer cognitive-query experience. This document records decisions, not claims that all features are already implemented.

**Guiding principle:** use existing capabilities naturally. Do not force Kafka Streams into Superset, fork Superset, or invent additional infrastructure simply to join them.

## 2. Feature Area #1 — Transaction Observability (LOCKED)

**Purpose:** Observe Motion cognitive transactions and the evidence Motion possesses, across SystemAnalysis and SystemMutation.

### In-scope features

- TM-authoritative **SUCCESS versus FAILURE** charts, counts, history, and trends.
- Transaction identity, named query, step results, raw cognitive results, and available execution details.
- Facts, relationships, Nexus Context, and model output associated with an execution **where captured and available**.
- For **TM-rejected FAILURE**: available rejection and validation evidence, without inventing a reason.
- SystemAnalysis results and their execution evidence.
- SystemMutation content, history, associated execution evidence, and Motion's own explicit publication evidence.
- One-to-one observability for analysis and autonomous mutation paths: not two separate observability products.
- Optional **Mutation Gate** for a particular domain feature when the enterprise wants a deliberate authorization point before mutation publication. Autonomous mutation remains available through the developer-facing API and its authorized path.

### Hard boundaries

- **TM alone determines cognitive transaction SUCCESS or FAILURE.** Motion and Superset do not reinterpret that decision.
- Only TM-rejected failures belong in the FAILURE observability scope. Development-time errors and unrelated infrastructure exceptions are not additional dashboard failure categories.
- No separate human-in-the-loop (HITL) framework; TM FAILURE is the human-intervention point for rejected cognitive transactions.
- TM SUCCESS means **the Motion cognitive transaction succeeded**, not that an external system changed successfully.
- Every external system is a **black box to Motion**. Motion makes no downstream outcome claims and does not monitor, validate, reconcile, or recover downstream effects.
- No rollback, compensating transactions, or recovery orchestration. Enterprises decide their own recovery using whatever Motion evidence is available.
- Mutation construction and mutation publication are distinct. Existing Motion mutation publication is explicit; SUCCESS does not itself imply publication.

## 3. Feature Area #2 — CGO Observability (LOCKED)

**Purpose:** Observe what Axis/CGO knows about enterprise facts, relationships, and graph state.

### Seven V1 views

1. **Fact Explorer:** facts, attributes, and values.
2. **Relationship Explorer:** connections among facts.
3. **Graph View:** visual representation of graph facts and relationships.
4. **Historical View:** state at a selected historical time.
5. **Change Explorer:** differences across selected times or intervals.
6. **Temporal Trends:** values and changes over time.
7. **Domain Filtering:** focus any applicable view on a chosen enterprise domain.

### Time is a first-class feature

- **NOW:** dynamic views reflecting the latest state known to Axis. Superset's native dashboard auto-refresh/polling is the preferred V1 presentation behavior, subject to actual integration and data freshness.
- **PAST:** frozen views at the selected historical state or interval; new incoming data must not silently move the selected historical boundary.
- **FUTURE:** excluded from V1. Predictive intelligence may be considered later at or within Motion; product placement and architecture are deliberately undecided.

CGO History is an existing core capability, including historical-view concepts; the exact current production retrieval and reconstruction contracts must be verified during design. A present graph snapshot alone must not be assumed to establish all historical states.

**Boundary:** Axis owns facts and relationships. Motion's observability represents the state Axis knows, not guaranteed instantaneous or complete knowledge of every external system.

## 4. Feature Area #3 — Developer Workbench (LOCKED)

**Purpose:** A developer can develop and test Motion cognitive SQL quickly in a terminal, in the spirit of `mongosh`.

### V1 experience

- Interactive console: enter cognitive SQL, execute, inspect, repeat.
- Execute through Motion/Loom, not against a Superset dataset.
- Raw execution output and TM SUCCESS/FAILURE.
- Inspect available execution evidence.
- Command history to recall and repeat queries.
- Easy installation and immediate command-line use; standalone distribution preferred.

**Technology preference:** Dart is favored because of developer familiarity and native executable distribution. Final implementation choice and command syntax are **not** locked. Example console commands discussed were illustrative only.

**Explicit exclusions:** no elaborate graphical workbench, IDE, charts, or dashboard framework for cognitive-query development. Superset SQL Lab is a separate conventional dataset-querying experience; do not assume it executes Motion's V14 cognitive SQL.

The existing V14 cognitive SQL contract includes `factId`, `relatedFactIds`, `expression`, and `definition`; detailed console behavior is a later design question.

## 5. Feature Area #4 — Unified Observability (LOCKED)

**Purpose:** Motion's enterprise **landing page** aggregates the already-defined observability capabilities.

- Entry and navigation for Transaction Observability and CGO Observability.
- Relevant domain and time selection, charts, tables, and detailed views.
- Prefer Superset's native dashboard composition, filters, and navigation wherever supported.
- **Aggregation only:** no new cognitive engine, observability engine, governance authority, or data model.
- Developer Workbench remains a separate terminal experience, not a feature to embed in the landing dashboard.

## 6. Three V1 Product Capabilities (LOCKED AS SCOPE)

| Capability | V1 role | Decision state |
|---|---|---|
| **Apache Superset** | Transaction and CGO observability presentation; Motion landing page | Feature scope locked; integration not designed |
| **Motion Cognitive Workbench** | Terminal-first cognitive SQL development and testing | Feature scope locked; Dart preferred, not final |
| **Apache Kafka Streams ecosystem** | Continuous processing and real-time data views | **In V1 scope**, but ecosystem/component selection deferred beyond today's discussion |

Superset can refresh dashboards by polling; this does not mean it natively ingests Kafka topics or directly queries Kafka Streams state stores. Its conventional datasets rely on supported queryable data sources. Do not equate file upload/ingestion into a connected database with native streaming ingestion into Superset.

**Architectural decision:** Do **not** force a direct Kafka Streams-to-Superset path. Evaluate Kafka Streams and its ecosystem on their own terms later. No connector, adapter, database, service API, storage choice, or refresh interval is selected here.

## 7. What Is Already Verified vs. Still Open

### Established engineering baseline (November 21, 2026)

- Five repositories (Arc, Axis/CGO, KDD, Beam, Motion) completed three sequential full-stack GREEN regressions.
- Final run: **1,136 tests total; 1,099 passed; 37 skipped; zero failures; zero errors; 6m 08s**.
- All five reactors reported `mvn clean install` BUILD SUCCESS; working trees were clean and upstreams synchronized at the last audit.
- Motion has named cognitive queries, SystemAnalysis execution and persistence, SystemMutation construction, and explicit Kafka mutation publication to `mutation_event`.
- Continuous Kafka Streams processing is **not** established as implemented.

### Not yet verified or decided

- The precise Superset integration interfaces and compatibility of individual visualizations, especially graph visualization.
- How execution-time evidence is retained and made queryable for historical transaction inspection.
- Exact CGO History retrieval/reconstruction coverage for PAST views.
- How Superset obtains NOW data from the eventual Kafka Streams ecosystem.
- Developer Workbench implementation, packaging, commands, and transport.
- Mutation Gate configuration/authorization contracts.
- Any new Motion service, data, or integration contracts.

These are **design and verification questions**, not authorization to expand V1 features or change locked Loom contracts.

## 8. Operating Contract

- Dhoni (user) calls each next move; Goose/Dravid analyzes; Virat executes locally only when explicitly directed.
- One ball at a time; read-only by default.
- No unauthorized production changes, repository writes, tests, Git operations, or publication.
- Do not redesign Loom or infer unseen current repository state.
- Preserve exact observed counts, results, and source boundaries.

## 9. Final Phase Status

**Feature phase: LOCKED.**

1. Transaction Observability — LOCKED.
2. CGO Observability (PAST + NOW) — LOCKED.
3. Developer Workbench — LOCKED.
4. Unified Observability / Motion Landing Page — LOCKED.

**Three V1 capabilities:** Superset, Cognitive Workbench, Kafka Streams ecosystem (specific choice deferred).

**No implementation authorized. No detailed integration architecture locked.**

**STUMPS until Dhoni's next call.**
