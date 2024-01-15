package com.github.dactiv.healthan.mybatis.plus.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.healthan.mybatis.plus.test.entity.CryptoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CryptoEntityMapper extends BaseMapper<CryptoEntity> {
}
