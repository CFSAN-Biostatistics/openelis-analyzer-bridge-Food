# thermo-quantstudio, revision 1: evidence

`src/main/resources/analyzer-profiles/thermo-quantstudio.json` (contract 2.0), the first baseline revision
of this profile. It replaces the pre-baseline `quantstudio` profile, which the Bridge no longer
ships, and is not a later revision of it (rule 6).

## Documents

| Short name       | Document                                                                                                                              | Where                                                                               |
| ---------------- | ------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| QuantStudio spec | QuantStudio 5 / 7 Flex, Field Mapping & Integration Spec v1.3.1, 2026-03-06, validated against three real XLS exports from Madagascar | `openelis-work/designs/analyzer-integration/quantstudio-field-mapping-spec-v131.md` |
| Thermo Fisher    | QuantStudio Design & Analysis exports (Results sheet)                                                                                 | the export itself; no host-interface document exists                                |

## Record kinds

What the export carries, by kind (OpenELIS analyzer spec, rule 11), and the section that declares it:

| Kind                        | Records                                                           | Declared in                |
| --------------------------- | ----------------------------------------------------------------- | -------------------------- |
| result for an ordered test  | the `VIH-1` row of each patient well                              | `default_test_mappings`    |
| value on a control material | standards, NTC wells and the positive control, by the rules below | `controlResultRecognition` |
| warning about the sample    | the `IC` row of each well, raised on `Undetermined`               | `sample_flags`             |
| not received                | none                                                              |                            |

## Tests and flags

| Declared                                                                                                        | Defined in          |
| --------------------------------------------------------------------------------------------------------------- | ------------------- |
| test `VIH-1` (Target Name, FAM reporter), LOINC 20447-9, copies/mL, quantitative; its result is `Quantity Mean` | section 4.3 and 5.1 |
| sample flag `IC` (Target Name, CY5 reporter), "Internal control not detected", raised when its `CT` is `Undetermined` | section 5.1 and 8; the internal control matters for a negative sample, and its failure calls for review, not a result |
| columns `Sample Name`, `Target Name`, `Quantity Mean`, `CT`, `Well Position`, `Task`                            | section 4.3         |

## Control recognition

| Rule                                                                   | Defined in                                                                                                                                                      |
| ---------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Task `STANDARD` is a control                                           | QuantStudio spec section 7.1 and 7.4                                                                                                                            |
| Task `NTC` is a control, whatever the well is named (`NC` or `NTC`)    | QuantStudio spec section 7.1 and 7.3                                                                                                                            |
| Sample Name `PC`, `pos`, `positive` or `controle positif` is a control | QuantStudio spec section 7.2; a standard-curve run has no positive-control task (Thermo, MAN0010408 Rev B.0, p. 14), so a PC is an `UNKNOWN` well found by name |

The name match is whole and ignores case, so a patient ID such as `PCR-0042` stays a patient result.

## Not verified

- A Thermo Fisher document for the export format; the spec is built from real exports, which
  check a site's configuration and do not define a profile (rule 19).
