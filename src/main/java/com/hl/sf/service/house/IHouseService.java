package com.hl.sf.service.house;

import com.hl.sf.service.ServiceResult;
import com.hl.sf.web.dto.HouseDTO;
import com.hl.sf.web.form.HouseForm;

/**
 * 房屋管理服务接口
 * @author hl2333
 */
public interface IHouseService {
    /**
     *
     * @param houseForm
     * @return
     */
    ServiceResult<HouseDTO> save(HouseForm houseForm);
}
