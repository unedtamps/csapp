package com.mycomp.csmessage.config;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
@ConfigurationPropertiesBinding
public class JsonToAdminConfigListConverter
    implements Converter<String, List<AdminConfig.AdminConfigData>> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public List<AdminConfig.AdminConfigData> convert(String source) {
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
          cleanJson, new TypeReference<List<AdminConfig.AdminConfigData>>() {});
    } catch (Exception e) {
      throw new IllegalArgumentException("Gagal mem-parse JSON String ke List<AdminConfigData>", e);
    }
  }
}
