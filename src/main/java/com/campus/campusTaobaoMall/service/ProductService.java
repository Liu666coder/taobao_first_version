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

/**
 * 商品业务服务层
 * 提供商品和分类的增删改查、上下架管理，以及后台商品搜索和分类关联校验
 */
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

    /**
     * 前台商品搜索（支持关键字和分类筛选）
     */
    public Result<?> getProductList(String keyword, Long categoryId) {
        List<Product> products = productMapper.search(keyword, categoryId);
        return Result.success(products);
    }

    /**
     * 获取商品详情
     */
    public Result<?> getProductDetail(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 获取前台展示的已启用分类列表
     */
    public Result<?> getCategoryList() {
        List<Category> categories = categoryMapper.findEnabled();
        return Result.success(categories);
    }

    /**
     * 获取后台所有分类列表（含未启用的）
     */
    public Result<?> getAllCategoryList() {
        List<Category> categories = categoryMapper.findAll();
        return Result.success(categories);
    }

    /**
     * 新增商品：DTO转实体，自动补充分类名称，设为上架状态
     */
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

    /**
     * 更新商品信息，若分类变更则同步更新分类名称
     */
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

    /**
     * 删除商品：先清除购物车和订单明细中的关联数据，再删除商品本身
     */
    public Result<?> deleteProduct(Long id) {
        // 先删除关联记录
        cartMapper.deleteByProductId(id);
        orderItemMapper.deleteByProductId(id);
        // 再删除商品
        productMapper.deleteById(id);
        return Result.success("删除成功");
    }

    /**
     * 更新商品上下架状态
     */
    public Result<?> updateProductStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        productMapper.update(product);
        return Result.success("操作成功");
    }

    /**
     * 后台商品搜索（支持关键字、分类、上下架状态筛选）
     */
    public Result<?> searchAdmin(String keyword, Long categoryId, Integer status) {
        List<Product> products = productMapper.searchAdmin(keyword, categoryId, status);
        return Result.success(products);
    }

    /**
     * 新增分类，默认为启用状态
     */
    public Result<?> addCategory(Category category) {
        category.setStatus(1);
        categoryMapper.insert(category);
        return Result.success("添加成功");
    }

    /**
     * 更新分类信息
     */
    public Result<?> updateCategory(Long id, Category category) {
        category.setId(id);
        categoryMapper.update(category);
        return Result.success("更新成功");
    }

    /**
     * 删除分类：若分类下有商品则拒绝删除
     */
    public Result<?> deleteCategory(Long id) {
        // 检查分类下是否有商品
        int count = productMapper.countByCategoryId(id);
        if (count > 0) {
            return Result.error("该分类下还有商品，无法删除");
        }
        categoryMapper.deleteById(id);
        return Result.success("删除成功");
    }

    /**
     * 更新分类启用/禁用状态
     */
    public Result<?> updateCategoryStatus(Long id, Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        categoryMapper.update(category);
        return Result.success("操作成功");
    }
}
