package com.github.dactiv.healthan.commons.page;


import java.io.Serial;
import java.util.List;

/**
 * 带总页数的分页
 *
 * @author maurice.chen
 * @param <T> 分页实体类型
 */
public class TotalPage<T> extends Page<T>{

    @Serial
    private static final long serialVersionUID = 6689608497142134254L;

    public static final String COUNT_FIELD = "count";

    public static final String TOTAL_COUNT = "totalCount";

    /**
     * 总记录数
     */
    private final long totalCount;

    /**
     * 带总页数的分页
     *
     * @param totalCount 总记录数
     */
    public TotalPage(long totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * 带总页数的分页
     *
     * @param pageRequest 分页请求
     * @param elements 数据集合
     * @param totalCount 总记录数
     */
    public TotalPage(PageRequest pageRequest, List<T> elements, long totalCount) {
        super(pageRequest, elements);
        this.totalCount = totalCount;
    }

    /**
     * 获取分页数量
     *
     * @return 分页数量
     */
    public int getTotalPages() {
        return this.getSize() == 0 ? 1 : (int)Math.ceil((double)this.totalCount / (double)this.getSize());
    }

    @Override
    public boolean hasNext() {
        return this.getNumber() < this.getTotalPages();
    }

    /**
     * 获取总数量
     *
     * @return 总数量
     */
    public long getTotalCount() {
        return totalCount;
    }
}
