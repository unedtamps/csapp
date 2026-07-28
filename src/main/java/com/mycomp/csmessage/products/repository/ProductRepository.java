package com.mycomp.csmessage.products.repository;

import com.mycomp.csmessage.products.models.Product;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
  List<Product> findByOwnerId(String ownerId);
}
