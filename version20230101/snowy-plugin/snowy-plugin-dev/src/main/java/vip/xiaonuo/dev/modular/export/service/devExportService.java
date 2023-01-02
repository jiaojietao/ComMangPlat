package vip.xiaonuo.dev.modular.export.service;

import javax.servlet.http.HttpServletResponse;

public interface devExportService {
    /**
     * 导出隐患排查中的台账列表
     * */
    void exportYinhuan(HttpServletResponse response) throws Exception;
}
