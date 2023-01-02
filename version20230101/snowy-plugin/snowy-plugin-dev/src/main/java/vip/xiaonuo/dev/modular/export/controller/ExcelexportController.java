package vip.xiaonuo.dev.modular.export.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.dev.modular.export.service.devExportService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(tags = "Excel导出控制器")
@ApiSupport(author = "weichai",order=3)
@RestController
@Validated
public class ExcelexportController {
    @Resource
    private devExportService devExportService;
    /**
     * 导出隐患排查台账excel文件
     *
     **/
    @ApiOperationSupport(order = 1)
    @ApiOperation("导出Excel")
    @CommonLog("导出Excel")
    @GetMapping(value = "/dev/export/exportExcel")
    public void exportExcel(HttpServletResponse response) throws Exception {//请求参数，
        devExportService.exportYinhuan(response);

    }
}
