/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Snowy采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改Snowy源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://www.xiaonuo.vip
 * 5.不可二次分发开源参与同类竞品，如有想法可联系团队xiaonuobase@qq.com商议合作。
 * 6.若您的项目无法满足以上几点，需要更多功能代码，获取Snowy商业授权许可，请在官网购买授权，地址为 https://www.xiaonuo.vip
 */
package vip.xiaonuo.biz.modular.accident.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.biz.modular.org.entity.BizOrg;
import vip.xiaonuo.biz.modular.position.entity.BizPosition;
import vip.xiaonuo.biz.modular.safety.entity.safety;
import vip.xiaonuo.biz.modular.safety.param.*;
import vip.xiaonuo.biz.modular.safety.param.BizUserGrantRoleParam;
import vip.xiaonuo.biz.modular.safety.service.safetyService;
import vip.xiaonuo.biz.modular.user.entity.BizUser;
import vip.xiaonuo.biz.modular.user.param.*;
import vip.xiaonuo.biz.modular.user.param.BizUserExportParam;
import vip.xiaonuo.biz.modular.user.param.BizUserSelectorOrgListParam;
import vip.xiaonuo.biz.modular.user.param.BizUserSelectorPositionParam;
import vip.xiaonuo.biz.modular.user.param.BizUserSelectorRoleParam;
import vip.xiaonuo.biz.modular.user.param.BizUserSelectorUserParam;
import vip.xiaonuo.biz.modular.user.result.BizUserRoleResult;
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.common.pojo.CommonValidList;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

/**
 * 人员控制器
 *
 * @author xuyuxiang
 * @date 2022/4/22 9:34
 **/
@Api(tags = "隐患排查")
@ApiSupport(author = "SNOWY_TEAM", order = 9)
@RestController
@Validated
public class AccidentController {

    @Resource
    private safetyService safetySevice;

    /**
     * 获取人员分页
     *
     * @author xuyuxiang
     * @date 2022/4/24 20:00
     */
    @ApiOperationSupport(order = 1)
    @ApiOperation("获取隐患排查分页")
    @SaCheckPermission("/biz/report/page")//获取权限
    @GetMapping("/biz/report/page")//Get用于获取数据
    public CommonResult<Page<safety>> page(safetyPageParam safetyPageParam) {
        return CommonResult.data(safetySevice.page(safetyPageParam));
    }
}
