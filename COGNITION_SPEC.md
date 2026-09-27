# Cognition Specification

Internal architecture and continuity document for Motion Perceptive Intelligence cognition.

This specification locks the semantic model of Cognitive Operations so later build sessions do not reconstruct it from conversation. It is not public narrative, not implementation documentation, and not a Cognitive Pipeline design.

It does not freeze Java types, orchestrator mechanics, orchestra internals, Historical Window selection, or the mechanism that promotes a Comparison into Evaluate.

---

## 1. Where cognition begins

Cognitive Operations begin after Perception.

Perception is the system's perceived state through a perspective/lens. It is what was understood from Observation, not Observation itself and not raw Operational View / Reasoning View memory.

Cognition operates over that perceived state.

```text
Observation
     ↓
Perception
[perceived state through a perspective/lens]
     ↓
Cognitive Operations
```

Cognition does not start at Observation. It does not start by reading Operational View or Reasoning View as its primary input. Those memories already exist upstream. Cognition consumes Perception.

---

## 2. Cognitive Operation vocabulary

Retention is not a Cognitive Operation.

Operational View and Reasoning View already retain Motion's operational and reasoning memories of reality. Explicitly preserving perceived state is not a cognition-stage operation in this model. **Retain is absent from the Cognitive Operation vocabulary.**

Current Cognitive Operations:

- Compare
- Interpret
- Evaluate
- Act
- Learn
- Ignore

This is the locked vocabulary for continuity. It is not declared as a permanently closed ontology. Additional operations may appear later. None of Compare, Interpret, Evaluate, Act, Learn, or Ignore is removed by this document.

---

## 3. Compare

Compare establishes comparative state.

It does not produce Comparison. It does not interpret what differences mean. It does not invoke Interpret.

### Two conceptual sides

**LEFT — Current Snapshot**

Derived from current perceived state (Perception through the relevant perspective/lens).

**RIGHT — Historical Window**

Bounded historical **system state**. Comparison against unbounded system history is semantically inappropriate and potentially impractical. System state evolves; arbitrarily old state may not be useful comparison context.

How the Historical Window is selected is **not specified here**. Duration, count, recency, relevance, and any inference mechanism remain deliberately unresolved.

### Domain ownership

Compare is domain/developer defined. There is no universal BraineousAI comparison algorithm.

The developer supplies the comparison function appropriate to the domain. Payments, manufacturing, inventory, security, and other domains may differ completely in what “compare” means.

### Product of Compare

Compare consumes the two sides and produces **Compare State**.

Compare State is the deterministic, domain-specific comparative substrate available for subsequent cognition. It is not Comparison. It is not meaning. It is not operational judgment.

```text
Current Snapshot          Historical Window
[current perceived state] [bounded historical SYSTEM STATE]
            \                 /
             \               /
              ↓             ↓
                   Compare
        [developer/domain comparison function]
                      ↓
                Compare State
```

---

## 4. Interpret

Interpret consumes Compare State and interprets it through the relevant Perception perspective/lens.

Interpret is where machine intelligence may enter.

The current architectural expectation is that interpretation can execute through LMQuery / llm-orchestra. Orchestra is a **black-box execution substrate** until a concrete Cognitive Pipeline requirement requires opening or modifying it.

Interpret produces **Comparison**.

```text
Current Snapshot + Historical Window
        ↓
      Compare
[developer/domain comparison function]
        ↓
   Compare State
        ↓
     Interpret
[perspective/lens + machine intelligence]
        ↓
    Comparison
```

Interpret does not decide whether the resulting understanding warrants Evaluate, Act, or mutation of Reality.

---

## 5. Comparison

Comparison is the cognitive artifact produced after comparative state has been interpreted.

It represents **real-time system analytics**. It may contain structured analytical understanding and report/summary representation. Its Java shape is **not frozen** here.

Comparison is a legitimate terminal product.

The normal continuous cognition path may end here:

```text
Perception
    ↓
Compare
    ↓
Compare State
    ↓
Interpret
    ↓
Comparison
    ↓
STOP
```

A Comparison does **not** automatically trigger Evaluate.

---

## 6. Real-time system analytics

Compare + Interpret form the continuous analytical cognition path.

The system continuously understands current perceived state relative to bounded historical system state.

This is real-time system analytics over evolving operational reality.

It is distinct from automatically deciding whether every analytical finding warrants system intervention.

---

## 7. Evaluate is selective

Evaluate is **not** the automatic next stage for every Comparison.

Constant evaluation would be semantically wrong: not everything the system notices or understands deserves operational judgment. It could also create unnecessary machine-intelligence, token, and runtime cost.

Only a **selected / interesting** Comparison proceeds into Evaluate.

The mechanism by which a Comparison becomes selected or interesting is **not designed here**. It may eventually involve developer metadata, automation, operator interest, declared conditions, or another mechanism. This specification does not choose that mechanism.

Evaluate receives a selected Comparison and applies **developer-declared control**.

Semantic question:

> Given this analytical understanding, are the developer-declared conditions for operational consideration satisfied?

Evaluation's Java representation is **not frozen** here.

Evaluate itself does not mutate Reality.

---

## 8. Act

Act follows a qualifying Evaluation.

Act operates only through **developer-specified system functions / capabilities**. Act does not receive unrestricted authority to mutate arbitrary system state.

```text
selected Comparison
        ↓
     Evaluate
[developer-declared control]
        ↓
       Act
[developer-specified system functions]
        ↓
      Result
        ↓
  AI Governance
        ↓
 System Mutation
        ↓
     Reality
```

Invoking developer-declared capabilities is not the same as authoritative mutation of Reality. Governance / authority protects the final mutation boundary.

Governance implementation is **not designed** in this document.

---

## 9. Learn

Learn is **longitudinal cognition**.

Semantically, learning emerges over accumulated cognitive experience rather than being an obligatory step after every current Perception.

From Learn's perspective, historical cognitive experience consists primarily of prior:

- Comparisons
- Evaluations

```text
Comparison₁ + Evaluation₁
Comparison₂ + Evaluation₂
Comparison₃ + Evaluation₃
...
Comparisonₙ + Evaluationₙ
        ↓
      Learn
        ↓
 Learned Knowledge
        ↓
future interpretation / planning / cognition
```

### Two histories that must not be conflated

**Compare's history — Historical Window**

- Historical **system state**
- What the system was like before
- Input side of Compare

**Learn's history — past Comparisons and Evaluations**

- Historical **cognitive experience**
- What the system previously understood and judged
- Input to Learn

These are not the same thing.

Learn does **not** mean “machine learning.” Implementation may someday involve machine learning, an LM, deterministic analysis, statistical techniques, or something else. The Cognitive Operation defines semantic responsibility, not technique.

Learned Knowledge exists to improve future cognition, especially future interpretation and planning.

Storage, representation, execution cadence, and consumers beyond that semantic statement are **not defined** here.

---

## 10. Ignore

Ignore is a Cognitive Operation in this vocabulary.

Cognition may explicitly terminate or decline further processing. No additional Ignore semantics are specified here.

---

## 11. Developer ownership vs BAI ownership

**Developer / domain owns:**

- Compare function and domain comparison semantics
- Controls used by Evaluate
- System functions / capabilities available to Act
- Pipeline composition and intent where applicable

**BAI owns:**

- Cognitive runtime abstractions
- Execution boundaries

Machine intelligence does not independently invent operational authority.

---

## 12. Orchestra boundary

llm-orchestra is a **black box** from the Cognitive Operations architecture.

The project is owned and its internals are known, but Cognitive Operations must not be designed around current orchestra implementation quirks.

Open or change orchestra only when a concrete Cognitive Pipeline requirement demands it.

Current expected intelligence seam:

```text
Interpret → LMQuery → orchestra
```

This seam is recorded, not expanded into integration design here.

---

## 13. Full conceptual model

### Continuous analytics

```text
Perception
    ↓
Current Snapshot + Historical Window
    ↓
Compare
[developer/domain comparison]
    ↓
Compare State
    ↓
Interpret
[perspective/lens + intelligence]
    ↓
Comparison
[real-time system analytics]
    ↓
STOP
```

### Selective operational path

```text
selected/interesting Comparison
    ↓
Evaluate
[developer-declared control]
    ↓
Act
[developer-specified system functions]
    ↓
Result
    ↓
AI Governance / Authority
    ↓
System Mutation
    ↓
Reality
```

### Longitudinal learning

```text
Past Comparisons + Evaluations
    ↓
Learn
    ↓
Learned Knowledge
    ↓
Future interpretation / planning / cognition
```

Ignore may terminate cognition without following these paths further. That is vocabulary, not a designed control-flow product.

---

## 14. Deliberately deferred

The following are not designed yet. They are deferred on purpose, not omitted by accident:

- Historical Window selection / convergence
- Historical Window duration, size, and relevance rules
- Exact Compare State representation
- Exact Comparison representation
- Exact Evaluation representation
- Mechanism selecting a Comparison for Evaluate
- Learned Knowledge representation and storage
- Learn cadence and execution mechanism
- Exact Cognitive Pipeline API / composition model
- Cognitive Operation Java contracts
- Orchestrator mechanics
- Orchestra integration details
- Governance implementation
- System-function invocation mechanics
