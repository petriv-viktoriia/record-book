package org.pnurecord.recordbook.category;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryDto categoryDto);
    CategoryDto toCategoryDto(Category category);

    List<CategoryDto> toCategoryDtoList(List<Category> categoryList);
    List<Category> toCategoryList(List<CategoryDto> categoryDtoList);

}
