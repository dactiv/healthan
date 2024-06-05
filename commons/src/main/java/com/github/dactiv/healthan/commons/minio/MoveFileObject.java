package com.github.dactiv.healthan.commons.minio;

import java.io.Serializable;

/**
 * 移动文件对象，用于拷贝或剪切文件对象使用
 *
 * @author maurice.chen
 */
public class MoveFileObject implements Serializable {

    private static final long serialVersionUID = -8637215027233473463L;

    /**
     * 原文件对象
     */
    private FileObject source;

    /**
     * 目标文件对象
     */
    private FileObject target;

    /**
     * 移动完成后是否删除原文件对象
     */
    private boolean deleteSourceIfSuccess;

    /**
     * 删除源文件后是否删除空桶
     */
    private boolean deleteBucketIfEmpty;

    public MoveFileObject() {
    }

    public MoveFileObject(FileObject source, FileObject target) {
        this(source, target, false);
    }

    public MoveFileObject(FileObject source, FileObject target, boolean deleteSourceIfSuccess) {
        this(source, target, false, false);
    }

    public MoveFileObject(FileObject source, FileObject target, boolean deleteSourceIfSuccess, boolean deleteBucketIfEmpty) {
        this.source = source;
        this.target = target;
        this.deleteSourceIfSuccess = deleteSourceIfSuccess;
        this.deleteBucketIfEmpty = deleteBucketIfEmpty;
    }

    public FileObject getSource() {
        return source;
    }

    public void setSource(FileObject source) {
        this.source = source;
    }

    public FileObject getTarget() {
        return target;
    }

    public void setTarget(FileObject target) {
        this.target = target;
    }

    public boolean isDeleteSourceIfSuccess() {
        return deleteSourceIfSuccess;
    }

    public void setDeleteSourceIfSuccess(boolean deleteSourceIfSuccess) {
        this.deleteSourceIfSuccess = deleteSourceIfSuccess;
    }

    public boolean isDeleteBucketIfEmpty() {
        return deleteBucketIfEmpty;
    }

    public void setDeleteBucketIfEmpty(boolean deleteBucketIfEmpty) {
        this.deleteBucketIfEmpty = deleteBucketIfEmpty;
    }
}
