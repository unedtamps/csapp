package com.mycomp.csmessage.products.service;

import com.mycomp.csmessage.products.models.Attribute;
import com.mycomp.csmessage.products.models.Attribute.AttributeKey;
import com.mycomp.csmessage.products.models.Product;
import com.mycomp.csmessage.products.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;

  public Product createProduct(String ownerId) {
    List<Attribute> attributes =
        List.of(
            Attribute.of(AttributeKey.COLOR, "red"),
            Attribute.of(AttributeKey.SIZES, List.of("S", "M", "L")),
            Attribute.of(AttributeKey.FABRIC, "cotton"));
    Product product =
        Product.builder()
            .name("Shirt")
            .category("clothing")
            .price(BigDecimal.valueOf(19.99))
            .ownerId(ownerId)
            .attributes(attributes)
            .build();

    return productRepository.save(product);
  }

  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }
}
