package com.github.dactiv.healthan.spring.security.test.controller;

import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.security.enumerate.ResourceType;
import com.github.dactiv.healthan.security.plugin.Plugin;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@Plugin(
        name = "OperateController",
        id = "operate",
        type = ResourceType.Menu,
        sources = "test"
)
@RequestMapping("operate")
public class OperateController {

    @GetMapping("isAuthenticated")
    @PreAuthorize("isAuthenticated()")
    public RestResult<?> isAuthenticated() {
        return RestResult.of("isAuthenticated");
    }

    @GetMapping("isFullyAuthenticated")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> isFullyAuthenticated() {
        return RestResult.of("isFullyAuthenticated");
    }

    @GetMapping("permsOperate")
    @PreAuthorize("hasAuthority('perms[operate]')")
    public RestResult<?> permsOperate() {
        return RestResult.of("permsOperate");
    }

    @GetMapping("pluginTestPermsOperate")
    @Plugin(name = "test perms 操作信息", sources = "test")
    @PreAuthorize("hasAuthority('perms[operate]') and isFullyAuthenticated()")
    public RestResult<?> pluginTestPermsOperate() {
        return RestResult.of("pluginTestPermsOperate");
    }

    @PostMapping("pluginTestPermsPostAuditOperate")
    @Plugin(name = "pluginTestPermsGetAuditOperate", sources = "test", audit = true)
    @PreAuthorize("hasAuthority('perms[operate]') and isFullyAuthenticated()")
    public RestResult<?> pluginTestPermsGetAuditOperate(HttpServletRequest request) {
        return RestResult.ofSuccess("pluginTestPermsGetAuditOperate", request.getParameterMap());
    }

    @GetMapping("pluginAnyPermsOperate")
    @Plugin(name = "pluginAnyPermsOperate", sources = "any")
    @PreAuthorize("hasAuthority('perms[operate]') and isFullyAuthenticated()")
    public RestResult<?> pluginAnyPermsOperate() {
        return RestResult.of("pluginAnyPermsOperate");
    }
}
