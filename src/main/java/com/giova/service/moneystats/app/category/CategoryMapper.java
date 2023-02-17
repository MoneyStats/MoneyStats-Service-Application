package com.giova.service.moneystats.app.category;

import com.giova.service.moneystats.app.category.dto.Category;
import com.giova.service.moneystats.app.category.entity.CategoryEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public List<Category> mapCategoryEntityToCategory(List<CategoryEntity> categoryEntities) {
    return categoryEntities.stream()
        .map(
            categoryEntity -> {
              Category category = new Category();
              BeanUtils.copyProperties(categoryEntity, category);
              return category;
            })
        .collect(Collectors.toList());
  }
}
