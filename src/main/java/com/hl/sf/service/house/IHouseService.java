package com.hl.sf.service.house;

import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.service.ServiceResult;
import com.hl.sf.web.dto.HouseDTO;
import com.hl.sf.web.form.DatatableSearch;
import com.hl.sf.web.form.HouseForm;
import com.hl.sf.web.form.RentSearch;

/**
 * 房屋管理服务接口
 * @author hl2333
 */
public interface IHouseService {
    /**
     * 保存房源信息
     * @param houseForm
     * @return
     */
    ServiceResult<HouseDTO> save(HouseForm houseForm);

    ServiceMultiResult<HouseDTO> adminQuery (DatatableSearch searchBody);

    ServiceResult<HouseDTO> findCompleteOne (Long id);

    ServiceResult update(HouseForm houseForm);

    ServiceResult removePhoto(Long id);

    ServiceResult updateCover(Long coverId, Long targetId);

    ServiceResult addTag(Long houseId, String tag);

    ServiceResult removeTag(Long houseId, String tag);

    ServiceResult updateStatus(Long id, int value);

    ServiceMultiResult<HouseDTO> simpleQuery(RentSearch rentSearch);
}
