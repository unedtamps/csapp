package com.mycomp.csapp.bootstrap.admin;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
@ConfigurationPropertiesBinding
public class AdminPropertiesConverter
    implements Converter<String, List<AdminProperties.AdminPropertiesData>> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public List<AdminProperties.AdminPropertiesData> convert(String source) {
    if (source == null || source.isBlank()) {
      return Collections.emptyList();
    }
    try {
      // Unescape quote jika terbawa tanda petik pembungkus
      String cleanJson = source.trim();
      if (cleanJson.startsWith("\"") && cleanJson.endsWith("\"")) {
        cleanJson = cleanJson.substring(1, cleanJson.length() - 1).replace("\\\"", "\"");
      }
      return objectMapper.readValue(
           cleanJson, new TypeReference<List<AdminProperties.AdminPropertiesData>>() {});
    } catch (Exception e) {
      throw new IllegalArgumentException("Gagal mem-parse JSON String ke List<AdminConfigData>", e);
    }
  }
}
