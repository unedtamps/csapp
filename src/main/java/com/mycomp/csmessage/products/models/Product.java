package com.mycomp.csmessage.products.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "products")
public class Product {

  @Id private String id;

  private String name;
  private String category;

  private String ownerId;

  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal price; // Menggunakan Decimal128 agar presisi nilai uang aman

  // Di sini List<Attribute> ditampung
  private List<Attribute> attributes = new ArrayList<>();
}
