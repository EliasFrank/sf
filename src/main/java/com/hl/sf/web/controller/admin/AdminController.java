package com.hl.sf.web.controller.admin;

import com.hl.sf.base.ApiDataTableResponse;
import com.hl.sf.base.ApiResponse;
import com.hl.sf.entity.SupportAddress;
import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.service.ServiceResult;
import com.hl.sf.service.house.IAddressService;
import com.hl.sf.service.house.IHouseService;
import com.hl.sf.service.house.IQiNiuService;
import com.hl.sf.web.dto.HouseDTO;
import com.hl.sf.web.dto.QiNiuPutRet;
import com.hl.sf.web.dto.SupportAddressDTO;
import com.hl.sf.web.form.DatatableSearch;
import com.hl.sf.web.form.HouseForm;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author hl2333
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    @Qualifier("qiNiuService")
    IQiNiuService qiNiuService;

    @Autowired
    @Qualifier("addressService")
    IAddressService addressService;

    @Autowired
    @Qualifier("houseService")
    IHouseService houseService;

    @GetMapping("center")
    public String adminCenterPage(){
        return "admin/center";
    }

    @GetMapping("welcome")
    public String welcomePage(){
        return "admin/welcome";
    }

    @GetMapping("login")
    public String adminLoginPage(){
        return "admin/login";
    }

    @GetMapping("house/subscribe")
    public String subscribe(){
        return "admin/subscribe";
    }

    @GetMapping("add/house")
    public String addHouse(){
        return "admin/house-add";
    }

    @GetMapping("house/list")
    public String houseListPage(){
        return "admin/house-list";
    }

    @PostMapping("houses")
    @ResponseBody
    public ApiDataTableResponse houses(@ModelAttribute DatatableSearch searchBody){
        ServiceMultiResult<HouseDTO> result = houseService.adminQuery(searchBody);

        ApiDataTableResponse response = new ApiDataTableResponse(ApiResponse.Status.SUCCESS);
        response.setData(result.getResult());
        response.setRecordsFiltered(result.getTotal());
        response.setRecordsTotal(result.getTotal());
        response.setDraw(searchBody.getDraw());

        return response;
    }
    @PostMapping(value = "upload/photo")
    @ResponseBody
    public ApiResponse uploadPhoto(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }
        String fileName = file.getOriginalFilename();
        try {
            InputStream inputStream = file.getInputStream();
            Response response = qiNiuService.uploadFile(inputStream);

            if (response.isOK()){
                QiNiuPutRet qiNiuPutRet = response.jsonToObject(QiNiuPutRet.class);
                return ApiResponse.ofSuccess(qiNiuPutRet);
            }else {
                return ApiResponse.ofMessage(response.statusCode, response.getInfo());
            }
        }catch (QiniuException e){
            Response response = e.response;
            try {
                return ApiResponse.ofMessage(response.statusCode, response.bodyString());
            } catch (QiniuException qiniuException) {
                qiniuException.printStackTrace();
                return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
            }
        }catch (IOException e) {
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("add/house")
    @ResponseBody
    public ApiResponse addHouse(@Valid @ModelAttribute("form-house-add")HouseForm houseForm,
                                BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage(),
                    null);
        }

        if (houseForm.getPhotos() == null || houseForm.getCover() == null){
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),
                    "必须上传图片");
        }

        Map<SupportAddress.Level, SupportAddressDTO> cityAndRegion
                = addressService.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());
        if (cityAndRegion.keySet().size() != 2){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        ServiceResult<HouseDTO> result = houseService.save(houseForm);
        if(result.isSuccess()){
            return ApiResponse.ofSuccess(result.getResult());
        }
        return ApiResponse.ofSuccess(ApiResponse.Status.NOT_VALID_PARAM);
    }
}
