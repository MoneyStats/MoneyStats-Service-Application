package com.giova.service.moneystats.app.category;

import com.giova.service.moneystats.app.category.dto.Category;
import com.giova.service.moneystats.app.category.entity.CategoryEntity;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Logged
@Service
public class CategoryService {

  @Autowired private ICategoryDAO iCategoryDAO;

  @Autowired private CategoryMapper categoryMapper;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> getAllCategories() {
    List<CategoryEntity> categoryEntity = iCategoryDAO.findAll();
    List<Category> categories = categoryMapper.mapCategoryEntityToCategory(categoryEntity);

    String message = "List of category!";

    Response response =
        new Response(
            HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), categories);

    return ResponseEntity.ok(response);
  }
}
