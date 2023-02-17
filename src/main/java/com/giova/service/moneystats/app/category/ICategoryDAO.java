package com.giova.service.moneystats.app.category;

import com.giova.service.moneystats.app.category.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoryDAO extends JpaRepository<CategoryEntity, Long> {}
