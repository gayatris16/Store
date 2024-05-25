package com.gayatri.store.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gayatri.store.models.Product;

public interface ProductRepository extends JpaRepository<Product, Integer>{

}
