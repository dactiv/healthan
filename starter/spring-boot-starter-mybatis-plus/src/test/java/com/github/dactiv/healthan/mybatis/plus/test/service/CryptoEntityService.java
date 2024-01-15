package com.github.dactiv.healthan.mybatis.plus.test.service;

import com.github.dactiv.healthan.mybatis.plus.service.BasicService;
import com.github.dactiv.healthan.mybatis.plus.test.entity.CryptoEntity;
import com.github.dactiv.healthan.mybatis.plus.test.mapper.CryptoEntityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class CryptoEntityService extends BasicService<CryptoEntityMapper, CryptoEntity> {
}
