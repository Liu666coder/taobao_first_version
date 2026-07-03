package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.dto.ProductDTO;
import com.campus.campusTaobaoMall.entity.Category;
import com.campus.campusTaobaoMall.service.ProductService;
import com.campus.campusTaobaoMall.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 前台接口
    @GetMapping("/products")
    public Result<?> getProductList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return productService.getProductList(keyword, categoryId);
    }

    @GetMapping("/products/{id}")
    public Result<?> getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    @GetMapping("/categories")
    public Result<?> getCategoryList() {
        return productService.getCategoryList();
    }

    // 后台管理接口
    @GetMapping("/admin/products")
    public Result<?> searchAdminProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return productService.searchAdmin(keyword, categoryId, status);
    }

    @GetMapping("/admin/categories")
    public Result<?> getAllCategoryList() {
        return productService.getAllCategoryList();
    }

    @PostMapping("/admin/products")
    public Result<?> addProduct(@RequestBody ProductDTO dto) {
        return productService.addProduct(dto);
    }

    @PutMapping("/admin/products/{id}")
    public Result<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
        return productService.updateProduct(id, dto);
    }

    @DeleteMapping("/admin/products/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }

    @PutMapping("/admin/products/{id}/status")
    public Result<?> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        return productService.updateProductStatus(id, status);
    }

    @PostMapping("/admin/categories")
    public Result<?> addCategory(@RequestBody Category category) {
        return productService.addCategory(category);
    }

    @PutMapping("/admin/categories/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return productService.updateCategory(id, category);
    }

    @DeleteMapping("/admin/categories/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        return productService.deleteCategory(id);
    }

    @PutMapping("/admin/categories/{id}/status")
    public Result<?> updateCategoryStatus(@PathVariable Long id, @RequestParam Integer status) {
        return productService.updateCategoryStatus(id, status);
    }
}
