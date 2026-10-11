package org.itech.ahb.fhir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.itech.ahb.profile.SampleFlags;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A FILE profile names the rows that are warnings about the sample by their test code. The
 * Madagascar FluoroCycler workbook's channel 2 row says whether the sample's internal control
 * worked: Invalid raises "Internal control invalid" on that sample, Valid says nothing, and
 * neither is a result.
 */
@DisplayName("A file's flag rows become sample flags, never results")
class FileSampleFlagsTest {

  private static final String PROFILE =
    """
    {"sample_flags": {"raisedValue": "Invalid", "codes": ["IC"], "names": {"IC": "Internal control invalid"}}}
    """;

  private static SampleFlags flags() throws Exception {
    return SampleFlags.fromProfile(new ObjectMapper().readTree(PROFILE));
  }

  @Test
  void anInvalidInternalControlRaisesTheFlagAndAValidOneSaysNothing() throws Exception {
    HL7ResultParser.ParsedResults valid = new HL7ResultParser.ParsedResults(
      "S1",
      List.of(numeric("VIH-1", "1250"), text("IC", "Valid"))
    ).withFileSampleFlags(flags());
    HL7ResultParser.ParsedResults invalid = new HL7ResultParser.ParsedResults(
      "S2",
      List.of(text("VIH-1", "Negative"), text("IC", "Invalid"))
    ).withFileSampleFlags(flags());

    assertThat(valid.results()).extracting(FhirBundleBuilder.AnalyzerResult::testCode).containsExactly("VIH-1");
    assertThat(valid.sampleFlags()).isEmpty();
    assertThat(invalid.results()).extracting(FhirBundleBuilder.AnalyzerResult::testCode).containsExactly("VIH-1");
    assertThat(invalid.sampleFlags()).containsExactly(new SampleFlag("IC", "Internal control invalid", "Invalid"));
  }

  @Test
  void aProfileWithoutFlagRowsLeavesEveryRowAResult() throws Exception {
    HL7ResultParser.ParsedResults parsed = new HL7ResultParser.ParsedResults(
      "S1",
      List.of(numeric("VIH-1", "1250"), text("IC", "Invalid"))
    );

    assertThat(parsed.withFileSampleFlags(SampleFlags.none()).results()).hasSize(2);
    assertThat(parsed.withFileSampleFlags(null).results()).hasSize(2);
  }

  @Test
  void flagsNameTheValueThatRaisesThem() throws Exception {
    assertThatThrownBy(
      () -> SampleFlags.fromProfile(new ObjectMapper().readTree("{\"sample_flags\": {\"codes\": [\"IC\"]}}"))
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("the value that means raised");
  }

  private static FhirBundleBuilder.AnalyzerResult numeric(String code, String value) {
    return FhirBundleBuilder.AnalyzerResult.numeric(code, code, value, "");
  }

  private static FhirBundleBuilder.AnalyzerResult text(String code, String value) {
    return FhirBundleBuilder.AnalyzerResult.text(code, code, value);
  }
}
