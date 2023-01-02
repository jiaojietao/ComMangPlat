package vip.xiaonuo.dev.modular.export.service.impl;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import vip.xiaonuo.auth.core.pojo.SaBaseLoginUser;
import vip.xiaonuo.auth.core.util.StpLoginUserUtil;
import vip.xiaonuo.biz.modular.safety.entity.safety;
import vip.xiaonuo.biz.modular.safety.mapper.safetyMapper;
import vip.xiaonuo.dev.modular.export.service.devExportService;
import vip.xiaonuo.dev.modular.file.service.DevFileService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
@Service
public class devExportServiceimpl implements devExportService {

    @Resource
    private safetyMapper safetyMapper;
    @Resource
    private  DevFileService devFileService;
     @Override
    public void exportYinhuan(HttpServletResponse response) throws Exception {

       HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("表");
         //设置文件名称
        String fileName = "隐患台账表" + LocalDate.now() + ".xls";
        //设置表头
        String[] headerList = {"隐患级别","排查类型","排查项目","隐患描述","隐患图片","整改图片","整改措施","责任人","排查时间","完成时间"};
        HSSFRow headerRow = sheet.createRow(0);
        //从数据库中获取数据，通过mybatis-plus获取
        List<safety> saftyList=getSaftyList();
        for (int i = 0; i < headerList.length; i++) {
            HSSFCell cell = headerRow.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headerList[i]);
            cell.setCellValue(text);
        }
        //设置数据行,存放数据到表中
        int rowColumn = 1;
         Cell cell;
        for(safety safety:saftyList) {
            HSSFRow dataRow = sheet.createRow(rowColumn);
            // 设置行高
            dataRow.setHeightInPoints((short) 70);

            dataRow.createCell(0).setCellValue(safety.getTroubles_level());
            dataRow.createCell(1).setCellValue(safety.getTroubleshoot_type());
            dataRow.createCell(2).setCellValue(safety.getTroubleshoot_name());
            dataRow.createCell(3).setCellValue(safety.getTroubles_detail());
            String id1=safety.getTroubles_image().split("=")[1];
            String id2=safety.getUpdate_image().split("=")[1];
            cell=dataRow.createCell(4);
            dataRow.createCell(5);
            // 设置列宽
            sheet.setColumnWidth(4, 256 * 25);
            sheet.setColumnWidth(5, 256 * 25);
            byte[] img1=devFileService.getImage(id1);
            byte[] img2=devFileService.getImage(id2);
            int pictureIdx = workbook.addPicture(img1, workbook.PICTURE_TYPE_JPEG);
            int pictureIdx1 = workbook.addPicture(img2, workbook.PICTURE_TYPE_JPEG);
            CreationHelper helper = workbook.getCreationHelper();
            Drawing drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = helper.createClientAnchor();
            ClientAnchor anchor1 = helper.createClientAnchor();
            // 图片插入坐标
            anchor.setCol1(4);
            anchor.setRow1(rowColumn);
            // 图片插入坐标
            anchor1.setCol1(5);
            anchor1.setRow1(rowColumn);
            // 指定我想要的长宽
            double standardWidth = 150;
            double standardHeight = 100;
            // 计算单元格的长宽
            double cellWidth = sheet.getColumnWidthInPixels(cell.getColumnIndex());
            double cellHeight = cell.getRow().getHeightInPoints() / 72 * 96;
            // 计算需要的长宽比例的系数
            double a = standardWidth / cellWidth;
            double b = standardHeight / cellHeight;
            // 插入图片
           Picture picture= drawing.createPicture(anchor, pictureIdx);
           Picture picture1= drawing.createPicture(anchor1, pictureIdx1);
           picture1.resize(a, b);
           picture.resize(a, b);
            dataRow.createCell(6).setCellValue(safety.getDeal_method());
            dataRow.createCell(7).setCellValue(safety.getTroubleshoot_user());
            dataRow.createCell(8).setCellValue(safety.getTroubleshoot_time());
            dataRow.createCell(9).setCellValue(safety.getComplete_time());
            rowColumn++;

        }
        //设置流格式
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("filename",fileName);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        response.flushBuffer();
        workbook.write(response.getOutputStream());
        workbook.close();
    }
    public List<safety> getSaftyList() {
        QueryWrapper<safety> queryWrapper = new QueryWrapper<>();//mybatisplus
        List<String> loginUserDataScope = StpLoginUserUtil.getLoginUserDataScope();//登录用户工具类，查询数据范围
        SaBaseLoginUser user = StpLoginUserUtil.getLoginUser();//获取登录用户信息
        if (!user.getAccount().equals("superAdmin")) {//如果不是超级用户，获取数据范围，根据数据范围获取结果
            if (ObjectUtil.isNotEmpty(loginUserDataScope)) {
                queryWrapper.lambda().in(safety::getOrg, loginUserDataScope);
            } else {
                queryWrapper.lambda().eq(safety::getId, StpUtil.getLoginIdAsString());
            }
        }
      /*  if (ObjectUtil.isNotEmpty(safetyPageParam.getOrgId())) {//根据组织号不为空
            BizOrg org = bizOrgService.queryEntity(safetyPageParam.getOrgId());
            if (!org.getParentId().equals("0"))
                queryWrapper.lambda().eq(safety::getOrg, safetyPageParam.getOrgId());
        }*/
        return safetyMapper.selectList(queryWrapper);
//          return null;
    }
}
