package com.google.gerrit.index.query;

import com.google.common.collect.ImmutableMap;
import com.google.gerrit.index.FieldDef;
import java.util.Map;

/** TODO */
public class FieldsBundle {

  private ImmutableMap<String, Object> fields;

  public FieldsBundle(Map<String, Object> fields) {
    this.fields = ImmutableMap.copyOf(fields);
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(FieldDef<?, T> def) {
    if (def.isRepeatable()) {
      return (T) fields.get(def.getName());
    } else {
      // TOOD: figure out how to distinguish long and int
      return (T) fields.get(def.getName());
    }
  }
}
