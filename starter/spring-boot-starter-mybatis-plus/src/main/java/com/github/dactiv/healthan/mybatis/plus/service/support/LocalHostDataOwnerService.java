package com.github.dactiv.healthan.mybatis.plus.service.support;

import com.github.dactiv.healthan.mybatis.plus.service.DataOwnerService;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 本地 ip 拥有者服务实现
 *
 * @author maurice.chen
 */
public class LocalHostDataOwnerService implements DataOwnerService {
    @Override
    public String getOwner() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
