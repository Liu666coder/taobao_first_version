package com.campus.campusTaobaoMall.service;

import com.campus.campusTaobaoMall.dto.ProductDTO;
import com.campus.campusTaobaoMall.entity.Category;
import com.campus.campusTaobaoMall.entity.Product;
import com.campus.campusTaobaoMall.mapper.CartMapper;
import com.campus.campusTaobaoMall.mapper.CategoryMapper;
import com.campus.campusTaobaoMall.mapper.OrderItemMapper;
import com.campus.campusTaobaoMall.mapper.ProductMapper;
import com.campus.campusTaobaoMall.vo.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    public Result<?> getProductList(String keyword, Long categoryId) {
        List<Product> products = productMapper.search(keyword, categoryId);
        return Result.success(products);
    }

    public Result<?> getProductDetail(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    public Result<?> getCategoryList() {
        List<Category> categories = categoryMapper.findEnabled();
        return Result.success(categories);
    }

    public Result<?> getAllCategoryList() {
        List<Category> categories = categoryMapper.findAll();
        return Result.success(categories);
    }

    public Result<?> addProduct(ProductDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setStatus(1);

        // 如果没有设置分类名称，根据分类ID获取分类名称
        if (product.getCategoryName() == null && product.getCategoryId() != null) {
            Category category = categoryMapper.findById(product.getCategoryId());
            if (category != null) {
                product.setCategoryName(category.getName());
            }
        }

        productMapper.insert(product);
        return Result.success("添加成功");
    }

    public Result<?> updateProduct(Long id, ProductDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setId(id);

        // 如果分类ID发生变化，更新分类名称
        if (product.getCategoryId() != null) {
            Category category = categoryMapper.findById(product.getCategoryId());
            if (category != null) {
                product.setCategoryName(category.getName());
            }
        }

        productMapper.update(product);
        return Result.success("更新成功");
    }

    public Result<?> deleteProduct(Long id) {
        // 先删除关联记录
        cartMapper.deleteByProductId(id);
        orderItemMapper.deleteByProductId(id);
        // 再删除商品
        productMapper.deleteById(id);
        return Result.success("删除成功");
    }

    public Result<?> updateProductStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        productMapper.update(product);
        return Result.success("操作成功");
    }

    // 后台管理搜索（包含下架商品）
    public Result<?> searchAdmin(String keyword, Long categoryId, Integer status) {
        List<Product> products = productMapper.searchAdmin(keyword, categoryId, status);
        return Result.success(products);
    }

    // 分类管理
    public Result<?> addCategory(Category category) {
        category.setStatus(1);
        categoryMapper.insert(category);
        return Result.success("添加成功");
    }

    public Result<?> updateCategory(Long id, Category category) {
        category.setId(id);
        categoryMapper.update(category);
        return Result.success("更新成功");
    }

    public Result<?> deleteCategory(Long id) {
        // 检查分类下是否有商品
        int count = productMapper.countByCategoryId(id);
        if (count > 0) {
            return Result.error("该分类下还有商品，无法删除");
        }
        categoryMapper.deleteById(id);
        return Result.success("删除成功");
    }

    public Result<?> updateCategoryStatus(Long id, Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        categoryMapper.update(category);
        return Result.success("操作成功");
    }
}
