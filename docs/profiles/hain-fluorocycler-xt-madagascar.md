# hain-fluorocycler-xt-madagascar, revision 1: evidence

`src/main/resources/analyzer-profiles/hain-fluorocycler-xt-madagascar.json` (contract 2.0). The
FluoroCycler XT exports no host-interface file; a lab copies results from FluoroSoftware XT into a
workbook of its own. Each workbook is its own "instrument" to the Bridge, so each gets its own
profile: `hain-fluorocycler-xt` describes the template the integration spec proposes, and this
profile describes the workbook the Madagascar site keeps. A lab picks the profile whose columns
match its workbook when it sets the analyzer up.

## Documents

| Short name        | Document                                                                                                 | Where                                                                                 |
| ----------------- | -------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- |
| Site workbook     | The Madagascar site workbook, as the analyzer mock carries it: `fixtures/fluorocycler-xt/results.xlsx`  | DIGI-UW/analyzer-mock-server, commit 835261c (column shape read from the site file)   |
| FluoroCycler spec | Bruker FluoroCycler XT, Molecular PCR Flat File Import Spec v1.0, 2026-03-06, section 2.1               | `openelis-work/designs/analyzer-integration/fluorocycler-xt-integration-spec-v1.0.md` |

The site workbook itself is not yet filed in openelis-work; the mock's copy is the example this
profile is read from (agreed 11 Oct 2026), and the spec gains the workbook when it is.

## The workbook

| Row | Col | Sample ID            | Type    | Calc. Conc. | Result   |
| --- | --- | -------------------- | ------- | ----------- | -------- |
| A   | 1   | DEV01263000000000001 | Unknown | 1250        | Positive |
| A   | 2   | DEV01263000000000001 | Unknown |             | Valid    |
| B   | 1   | DEV01263000000000002 | Unknown | 450         | Positive |
| B   | 2   | DEV01263000000000002 | Unknown |             | Valid    |

One sample, two rows: the spec's section 2.1 observes that the export writes one row per detection
channel. Channel 1 is the target (the concentration and its call), channel 2 the internal control
(valid or not). `Col` carries the channel number, so the profile maps `Col` to the test code and
`test_code_values` says what each number means. A channel the profile does not name keeps its
number, which OpenELIS holds as a code nobody has mapped.

## Tests and values

| Declared                                                                                                      | Defined in                                                                                                                     |
| ------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| columns `Sample ID`, `Type`, `Col`, `Calc. Conc.`, `Result`; `Row` is not read                                | the workbook's header row                                                                                                      |
| `Col` 1 is `VIH-1`, `Col` 2 is `IC`                                                                           | rows 2 to 5 of the workbook; spec section 2.1 for one row per channel                                                          |
| test `VIH-1`, LOINC 20447-9, unit copies/mL, quantitative, with a call                                        | rows 2 and 4 (`Calc. Conc.` is the number, `Result` the call)                                                                  |
| values `Positive`, `Negative`, `Invalid`                                                                      | `Positive` from the workbook; `Negative` and `Invalid` from spec section 3.3 (FluoroType assays report Positive/Negative/Invalid) |
| `Positive` LOINC LA11882-0, SNOMED 260373001, CIEL 1301; `Negative` LA11883-8, 260415000, CIEL 1302; `Invalid` LA15841-2, CIEL 163611 | the same codings `hain-fluorocycler-xt` cites for Detected, Not detected and Invalid                                           |
| sample flag `IC`, "Internal control invalid", raised on `Invalid`                                            | rows 3 and 5 carry `Valid`; `Invalid` is the other state the spec's section 3.3 names for an internal-control failure          |
| controls by `Type` `Positive`, `Negative`, `Standard`                                                         | `hain-fluorocycler-xt`, whose `Type` rules came from this workbook's `Type` column                                   |

The profile has no translations: the workbook is filled in by the lab in English.

## Record kinds

What the workbook carries, by kind (OpenELIS analyzer spec, rule 11), and the section that declares it:

| Kind                        | Records                                                    | Declared in                                     |
| --------------------------- | ---------------------------------------------------------- | ----------------------------------------------- |
| result for an ordered test  | the channel 1 row: `VIH-1`, its concentration and its call | `default_test_mappings`                         |
| value on a control material | a row whose `Type` is `Positive`, `Negative` or `Standard` | `controlResultRecognition`                      |
| warning about the sample    | the channel 2 row: the internal control, raised on Invalid | `sample_flags`                                  |
| not received                | none                                                       |                                                 |

## Not verified

- The site workbook itself: only the mock's copy of its shape and values has been seen. A row from
  the lab with a negative sample, a failed internal control, or a control settles `Negative`,
  `Invalid` and the `Type` values.
