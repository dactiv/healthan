package com.github.dactiv.healthan.commons.page;


import java.io.Serial;

/**
 * 分页请求对象
 *
 * @author maurice.chen
 **/
public class PageRequest extends ScrollPageRequest {

    @Serial
    private static final long serialVersionUID = -7063877675141922463L;

    public static final String PAGE_FIELD_NAME = "page";

    public static final String NUMBER_FIELD_NAME = "number";

    public static final String SIZE_FIELD_NAME = "size";

    /**
     * 默认当前页数
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * 页号
     */
    private int number = DEFAULT_PAGE;

    /**
     * 分页请求对象
     */
    public PageRequest() {

    }

    /**
     * 分页请求对象，用于在分页查询时，通过该对象得知要查询的页数。
     *
     * @param size 分页大小
     */
    public PageRequest(int size) {
        super(size);
    }

    /**
     * 分页请求对象，用于在分页查询时，通过该对象得知要查询的页数。
     *
     * @param number 页号
     * @param size 内容大小
     */
    public PageRequest(int number, int size) {
        super(size);
        this.number = number;
    }

    /**
     * 获取当前页号
     *
     * @return 页号
     */
    public int getNumber() {
        return number;
    }

    /**
     * 设置当前页号
     *
     * @param number 页号
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * 获取指定第一个返回记录行的偏移量
     *
     * @return 偏移量
     */
    public int getOffset() {
        int offset = (number - DEFAULT_PAGE) * getSize();
        if (offset < 0) {
            offset = 0;
        }
        return offset;
    }

    /**
     * 创建一个分页请求对象，默认第一页
     *
     * @param size 分页大小
     *
     * @return 分页请求对象
     */
    public static PageRequest of(int size) {
        return new PageRequest(DEFAULT_PAGE, size);
    }

    /**
     * 创建一个分页请求对象
     *
     * @param number 第几页
     * @param size 分页大小
     *
     * @return 分页请求对象
     */
    public static PageRequest of(int number, int size) {
        return new PageRequest(number, size);
    }

}
