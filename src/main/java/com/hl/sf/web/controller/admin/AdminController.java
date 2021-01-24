package com.hl.sf.web.controller.admin;

import com.hl.sf.base.ApiResponse;
import com.hl.sf.service.house.IQiNiuService;
import com.hl.sf.web.dto.QiNiuPutRet;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author hl2333
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    @Qualifier("qiNiuService")
    IQiNiuService qiNiuService;


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
        }
        catch (QiniuException e){
            Response response = e.response;
            try {
                return ApiResponse.ofMessage(response.statusCode, response.bodyString());
            } catch (QiniuException qiniuException) {
                qiniuException.printStackTrace();
                return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
            }
        }
        catch (IOException e) {
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
