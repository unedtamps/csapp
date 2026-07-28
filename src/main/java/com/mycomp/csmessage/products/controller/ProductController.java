package com.mycomp.csmessage.products.controller;

import com.mycomp.csmessage.accounts.dto.AuthClaims;
import com.mycomp.csmessage.products.models.Product;
import com.mycomp.csmessage.products.service.ProductService;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
  private final ProductService productService;

  @PostMapping("/create")
  public ResponseEntity<Product> createProduct(@AuthenticationPrincipal AuthClaims user) {
    return ResponseEntity.ok(productService.createProduct(user.id()));
  }

  @GetMapping("/all")
  public ResponseEntity<List<Product>> getAllProducts() {
    return ResponseEntity.ok(productService.getAllProducts());
  }
}
