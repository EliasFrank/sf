package com.hl.sf.web.controller.house;

import com.hl.sf.base.ApiResponse;
import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.service.house.IAddressService;
import com.hl.sf.web.dto.SubwayDTO;
import com.hl.sf.web.dto.SubwayStationDTO;
import com.hl.sf.web.dto.SupportAddressDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author hl2333
 */
@Controller
@RequestMapping("address")
@ResponseBody
public class HouseController {

    @Autowired
    @Qualifier("addressService")
    private IAddressService addressService;

    /**
     * 获取所有城市
     * @return
     */
    @GetMapping("support/cities")
    public ApiResponse getSupportCities(){
        ServiceMultiResult<SupportAddressDTO> result = addressService.findAllCities();
        if (result.getResultSize() == 0){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取某个城市的所有区域
     * @param city
     * @return
     */
    @GetMapping("support/regions")
    public ApiResponse getSupportRegions(@RequestParam("city_name")String city){
        ServiceMultiResult<SupportAddressDTO> result = addressService.findAllRegions(city);
        if (result.getResultSize() == 0){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取某个城市的所有地铁线路
     * @param city
     * @return
     */
    @GetMapping("support/subway/line")
    public ApiResponse getSupportSubwayLine(@RequestParam("city_name")String city){
        ServiceMultiResult<SubwayDTO> result = addressService.findAllSubways(city);
        if (result.getResultSize() == 0){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取对应地铁线路所支持的地铁站点
     * @param subwayId
     * @return
     */
    @GetMapping("support/subway/station")
    public ApiResponse getSupportSubwayStation(@RequestParam("subway_id") Integer subwayId){
        ServiceMultiResult<SubwayStationDTO> result = addressService.findAllSubwayStations(subwayId);
        if (result.getResultSize() == 0){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }
}
