package com.hl.sf.web.controller.house;

import com.hl.sf.base.ApiResponse;
import com.hl.sf.base.RentValueBlock;
import com.hl.sf.entity.SupportAddress;
import com.hl.sf.service.ISmsService;
import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.service.ServiceResult;
import com.hl.sf.service.house.IAddressService;
import com.hl.sf.service.house.IHouseService;
import com.hl.sf.service.search.ISearchService;
import com.hl.sf.service.user.IUserService;
import com.hl.sf.utils.LoginUserUtil;
import com.hl.sf.web.dto.*;
import com.hl.sf.web.form.MapSearch;
import com.hl.sf.web.form.RentSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * @author hl2333
 */
@Controller
public class HouseController {

    @Autowired
    @Qualifier("smsService")
    private ISmsService smsService;

    @Autowired
    @Qualifier("addressService")
    private IAddressService addressService;

    @Autowired
    @Qualifier("houseService")
    private IHouseService houseService;

    @Autowired
    @Qualifier("realUserService")
    private IUserService realUserService;

    @Autowired
    @Qualifier("searchService")
    private ISearchService searchService;

    public static final Logger logger = LoggerFactory.getLogger(HouseController.class);

    /**
     * 获取所有城市
     * @return
     */
    @GetMapping("address/support/cities")
    @ResponseBody
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
    @GetMapping("address/support/regions")
    @ResponseBody
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
    @GetMapping("address/support/subway/line")
    @ResponseBody
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
    @GetMapping("address/support/subway/station")
    @ResponseBody
    public ApiResponse getSupportSubwayStation(@RequestParam("subway_id") Integer subwayId){
        ServiceMultiResult<SubwayStationDTO> result = addressService.findAllSubwayStations(subwayId);
        if (result.getResultSize() == 0){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    @GetMapping("rent/house/autocomplete")
    @ResponseBody
    public ApiResponse autoComplete(@RequestParam(value = "prefix") String prefix){
        if (prefix.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }
        ServiceResult<List<String>> result = this.searchService.suggest(prefix);
        return ApiResponse.ofSuccess(result.getResult());
    }

    @GetMapping("rent/house")
    public String rentHousePage(@ModelAttribute RentSearch rentSearch,
                                Model model, HttpSession session,
                                RedirectAttributes redirectAttributes){
        if (rentSearch.getCityEnName() == null) {
            String cityEnNameInSession = (String) session.getAttribute("cityEnName");
            if (cityEnNameInSession == null) {
                redirectAttributes.addAttribute("msg", "must_chose_city");
                return "redirect:/index";
            } else {
                rentSearch.setCityEnName(cityEnNameInSession);
            }
        } else {
            session.setAttribute("cityEnName", rentSearch.getCityEnName());
        }

        ServiceResult<SupportAddressDTO> city = addressService.findCity(rentSearch.getCityEnName());

        if (!city.isSuccess()){
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }

        model.addAttribute("currentCity", city.getResult());

        ServiceMultiResult<SupportAddressDTO> addressResult = addressService.findAllRegions(rentSearch.getCityEnName());

        if (addressResult.getResult() == null || addressResult.getTotal() < 1){
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }

        ServiceMultiResult<HouseDTO> serviceMultiResult = houseService.query(rentSearch);

        model.addAttribute("total", serviceMultiResult.getTotal());
//        model.addAttribute("total", 0);
        model.addAttribute("houses", serviceMultiResult.getResult());
//        model.addAttribute("houses", new ArrayList<>());

        if (rentSearch.getRegionEnName() == null){
            rentSearch.setRegionEnName("*");
        }

        model.addAttribute("searchBody", rentSearch);
        model.addAttribute("regions", addressResult.getResult());

        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        model.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);

        model.addAttribute("currentPriceBlock", RentValueBlock.matchPrice(rentSearch.getPriceBlock()));
        model.addAttribute("currentAreaBlock", RentValueBlock.matchArea(rentSearch.getAreaBlock()));

        return "rent-list";
    }

    @GetMapping("rent/house/show/{id}")
    public String show(@PathVariable(value = "id") Long houseId,
                       Model model) {
        if (houseId <= 0) {
            return "404";
        }

        ServiceResult<HouseDTO> serviceResult = houseService.findCompleteOne(houseId);
        if (!serviceResult.isSuccess()) {
            return "404";
        }

        HouseDTO houseDTO = serviceResult.getResult();
        Map<SupportAddress.Level, SupportAddressDTO>
                addressMap = addressService.findCityAndRegion(houseDTO.getCityEnName(), houseDTO.getRegionEnName());

        SupportAddressDTO city = addressMap.get(SupportAddress.Level.CITY);
        SupportAddressDTO region = addressMap.get(SupportAddress.Level.REGION);

        model.addAttribute("city", city);
        model.addAttribute("region", region);

        ServiceResult<UserDTO> userDTOServiceResult = realUserService.findById(houseDTO.getAdminId());
        model.addAttribute("agent", userDTOServiceResult.getResult());
        model.addAttribute("house", houseDTO);

        ServiceResult<Long> aggResult = searchService.aggregateDistrictHouse(city.getEnName(), region.getEnName(), houseDTO.getDistrict());
        model.addAttribute("houseCountInDistrict", aggResult.getResult());

        return "house-detail";
    }

    @GetMapping("rent/house/map")
    public String rentMapPage(@RequestParam(value = "cityEnName") String cityEnName,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes){
        ServiceResult<SupportAddressDTO> city = addressService.findCity(cityEnName);
        if (!city.isSuccess()) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        } else {
            session.setAttribute("cityName", cityEnName);
            model.addAttribute("city", city.getResult());
        }
        ServiceMultiResult<SupportAddressDTO> regions = addressService.findAllRegions(cityEnName);

        ServiceMultiResult<HouseBucketDTO> serviceResult = searchService.mapAggregate(cityEnName);
        model.addAttribute("aggData", serviceResult.getResult());
        model.addAttribute("total", serviceResult.getTotal());
        model.addAttribute("regions", regions.getResult());
        return "rent-map";
    }
    @GetMapping("rent/house/map/houses")
    @ResponseBody
    public ApiResponse rentMapHouses(@ModelAttribute MapSearch mapSearch) {
        if (mapSearch.getCityEnName() == null) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "必须选择城市");
        }
        ServiceMultiResult<HouseDTO> serviceMultiResult;
        if (mapSearch.getLevel() < 13) {
            serviceMultiResult = houseService.wholeMapQuery(mapSearch);
        } else {
//             小地图查询必须要传递地图边界参数
            serviceMultiResult = houseService.boundMapQuery(mapSearch);
        }

        ApiResponse response = ApiResponse.ofSuccess(serviceMultiResult.getResult());
        response.setMore(serviceMultiResult.getTotal() > (mapSearch.getStart() + mapSearch.getSize()));
        return response;
    }

    @GetMapping(value = "sms/code")
    @ResponseBody
    public ApiResponse smsCode(@RequestParam("telephone") String telephone) {
        if (!LoginUserUtil.checkTelephone(telephone)) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "请输入正确的手机号");
        }
        ServiceResult<String> result = smsService.sendSms(telephone);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess("");
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }
    }
}
