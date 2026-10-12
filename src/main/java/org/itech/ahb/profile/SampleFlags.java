package org.itech.ahb.profile;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The records an instrument sends as yes or no warnings about a sample, such as a hematology
 * analyzer's alarms or a PCR run's internal-control validity, as the profile's {@code sample_flags}
 * declare them: the codes, the value that means raised, and a name per code where the record
 * carries none. A record carrying one of these codes is never a result: a raised one is a flag on
 * the sample, and one not raised says nothing. The record's code and value are read from the
 * same positions as a result's, whatever the protocol.
 */
public record SampleFlags(String raisedValue, Set<String> codes, Map<String, String> names) {
  public SampleFlags {
    codes = codes == null ? Set.of() : Set.copyOf(codes);
    names = names == null ? Map.of() : Map.copyOf(names);
  }

  public static SampleFlags none() {
    return new SampleFlags(null, Set.of(), Map.of());
  }

  /** The flags a profile declares; a profile that declares none has no record that is a flag. */
  public static SampleFlags fromProfile(JsonNode profile) {
    JsonNode flags = profile.path("sample_flags");
    if (!flags.isObject()) {
      return none();
    }
    Set<String> codes = new LinkedHashSet<>();
    flags.path("codes").forEach(code -> codes.add(code.asText()));
    Map<String, String> names = new LinkedHashMap<>();
    flags.path("names").fields().forEachRemaining(entry -> names.put(entry.getKey(), entry.getValue().asText()));
    String raisedValue = flags.path("raisedValue").asText(null);
    if (raisedValue == null || raisedValue.isBlank() || codes.isEmpty()) {
      throw new IllegalArgumentException("sample_flags names the codes and the value that means raised");
    }
    if (!codes.containsAll(names.keySet())) {
      throw new IllegalArgumentException("sample_flags names a code it does not list");
    }
    return new SampleFlags(raisedValue, codes, names);
  }

  /** A record that carries a flag: its code, the name it goes under, its value and whether that raises it. */
  public record Reported(String code, String name, String value, boolean raised) {}

  /**
   * The flag a record carries, or null when its code is not one the profile names. The name is the
   * profile's where it gives one, else the instrument's, else the code.
   */
  public Reported read(String code, String instrumentName, String value) {
    if (code == null || !codes.contains(code)) {
      return null;
    }
    String name = names.get(code);
    if (name == null || name.isBlank()) {
      name = instrumentName == null || instrumentName.isBlank() ? code : instrumentName;
    }
    String text = value == null ? "" : value;
    return new Reported(code, name, text, raisedValue.equals(text));
  }
}
