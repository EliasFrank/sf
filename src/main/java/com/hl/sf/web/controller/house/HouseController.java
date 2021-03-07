package com.hl.sf.web.controller.house;

import com.hl.sf.base.ApiResponse;
import com.hl.sf.base.RentValueBlock;
import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.service.ServiceResult;
import com.hl.sf.service.house.IAddressService;
import com.hl.sf.service.house.IHouseService;
import com.hl.sf.web.dto.HouseDTO;
import com.hl.sf.web.dto.SubwayDTO;
import com.hl.sf.web.dto.SubwayStationDTO;
import com.hl.sf.web.dto.SupportAddressDTO;
import com.hl.sf.web.form.RentSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

/**
 * @author hl2333
 */
@Controller
public class HouseController {

    @Autowired
    @Qualifier("addressService")
    private IAddressService addressService;

    @Autowired
    @Qualifier("houseService")
    private IHouseService houseService;

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

        ServiceMultiResult<HouseDTO> serviceMultiResult = houseService.simpleQuery(rentSearch);

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
}
