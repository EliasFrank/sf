package com.hl.sf.service.house.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.hl.sf.base.HouseStatus;
import com.hl.sf.base.HouseSubscribeStatus;
import com.hl.sf.entity.*;
import com.hl.sf.repository.*;
import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.service.ServiceResult;
import com.hl.sf.service.house.IHouseService;
import com.hl.sf.service.search.impl.SearchServiceImpl;
import com.hl.sf.service.user.IUserService;
import com.hl.sf.utils.LoginUserUtil;
import com.hl.sf.web.dto.HouseDTO;
import com.hl.sf.web.dto.HouseDetailDTO;
import com.hl.sf.web.dto.HousePictureDTO;
import com.hl.sf.web.dto.HouseSubscribeDTO;
import com.hl.sf.web.form.*;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service("houseService")
public class HouseServiceImpl implements IHouseService {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private HouseDao houseDao;

    @Autowired
    private HouseDetailDao houseDetailDao;

    @Autowired
    private HousePictureDao housePictureDao;

    @Autowired
    private HouseTagDao houseTagDao;

    @Autowired
    private SubwayDao subwayDao;

    @Autowired
    private HouseSubscribeDao subscribeDao;

    @Autowired
    private SubwayStationDao subwayStationDao;

    @Autowired
    private QiNiuServiceImpl qiNiuService;

    @Autowired
    @Qualifier("realUserService")
    private IUserService userService;

    @Autowired
    @Qualifier("searchService")
    private SearchServiceImpl searchService;

    @Value("${qiniu.cdn.prefix}")
    private String cdnPrefix;

    private static final Logger logger = LoggerFactory.getLogger(IHouseService.class);
    @Override
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {
        HouseDetail houseDetail = new HouseDetail();
        ServiceResult<HouseDTO> subwayResult = wrapperDetailInfo(houseDetail, houseForm);
        if (subwayResult != null){
            return subwayResult;
        }

        House house = new House();
        modelMapper.map(houseForm, house);

        Date now = new Date();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setAdminId(userService.getLoginUserId());
        houseDao.save(house);

        houseDetail.setHouseId(house.getId());
        houseDetailDao.save(houseDetail);

        List<HousePicture> pictures = generatePictures(houseForm, house.getId());
        housePictureDao.save(pictures);

        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
        HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);

        houseDTO.setHouseDetail(houseDetailDTO);

        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        pictures.forEach(housePicture -> pictureDTOS.add(modelMapper.map(housePicture, HousePictureDTO.class)));
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setCover(this.cdnPrefix + houseDTO.getCover());

        List<String> tags = houseForm.getTags();
        if (tags != null && !tags.isEmpty()) {
            List<HouseTag> houseTags = new ArrayList<>();
            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(), tag));
            }
            houseTagDao.save(houseTags);
            houseDTO.setTags(tags);
        }

        return new ServiceResult<HouseDTO>(true, null, houseDTO);
    }

    @Override
    public ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody) {
        List<HouseDTO> houseDTOS = new ArrayList<>();

//        PageHelper.startPage(searchBody.getStart(), searchBody.getLength());
        PageHelper.startPage(0, 20);
        List<House> houses = houseDao.findAll(searchBody);
        PageInfo<House> pageInfo = new PageInfo<>(houses);


        pageInfo.getList().forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            houseDTOS.add(houseDTO);
        });

        return new ServiceMultiResult<HouseDTO>(Integer.toUnsignedLong(houseDTOS.size()), houseDTOS);
    }

    @Override
    public ServiceResult<HouseDTO> findCompleteOne(Long id) {
        House house = houseDao.findById(id);
        if (house == null){
            return ServiceResult.notFound();
        }

        HouseDetail detail = houseDetailDao.findByHouseId(id);
        List<HousePicture> pictures = housePictureDao.findAllByHouseId(id);

        HouseDetailDTO detailDTO = modelMapper.map(detail, HouseDetailDTO.class);
        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        for (HousePicture picture : pictures) {
            HousePictureDTO pictureDTO = modelMapper.map(picture, HousePictureDTO.class);
            pictureDTOS.add(pictureDTO);
        }

        List<HouseTag> tags = houseTagDao.findAllByHouseId(id);
        List<String> tagList = new ArrayList<>();
        for (HouseTag tag : tags) {
            tagList.add(tag.getName());
        }

        HouseDTO result = modelMapper.map(house, HouseDTO.class);
        result.setHouseDetail(detailDTO);
        result.setPictures(pictureDTOS);
        result.setTags(tagList);

        if (userService.getLoginUserId() > 0) {
            HouseSubscribe subscribe = subscribeDao.findByHouseIdAndUserId(house.getId(), userService.getLoginUserId());
            if (subscribe != null) {
                result.setSubscribeStatus(subscribe.getStatus());
            }
        }

        return ServiceResult.of(result);
    }

    @Override
    @Transactional
    public ServiceResult update(HouseForm houseForm) {
        House house = this.houseDao.findById(houseForm.getId());
        if (house == null){
            return ServiceResult.notFound();
        }

        HouseDetail detail = houseDetailDao.findByHouseId(house.getId());
        if (detail == null){
            return ServiceResult.notFound();
        }

        ServiceResult wrapperResult = wrapperDetailInfo(detail, houseForm);
        if (wrapperResult != null){
            return wrapperResult;
        }

        houseDetailDao.update(detail);

        List<HousePicture> pictures = generatePictures(houseForm, houseForm.getId());
        housePictureDao.save(pictures);

        if (houseForm.getCover() == null){
            houseForm.setCover(house.getCover());
        }

        modelMapper.map(houseForm, house);
        house.setLastUpdateTime(new Date());
        houseDao.update(house);

        if (house.getStatus() == HouseStatus.PASSES.getValue()) {
            searchService.index(house.getId());
        }

        return ServiceResult.success();
    }

    @Override
    public ServiceResult removePhoto(Long id) {
        HousePicture picture = housePictureDao.findById(id);
        if (picture == null){
            return ServiceResult.notFound();
        }

        try{
            Response response = this.qiNiuService.deleteFile(picture.getPath());
            if (response.isOK()){
                housePictureDao.delete(id);
                return ServiceResult.success();
            }else {
                return new ServiceResult(false, response.error);
            }
        }catch (QiniuException e){
            e.printStackTrace();
            return new ServiceResult(false, e.getMessage());
        }
    }

    @Override
    @Transactional
    public ServiceResult updateCover(Long coverId, Long targetId) {
        HousePicture cover = housePictureDao.findById(coverId);

        if (cover == null){
            return ServiceResult.notFound();
        }
        houseDao.updateCover(targetId, cover.getPath());
        return ServiceResult.success();
    }

    @Override
    public ServiceResult addTag(Long houseId, String tag) {
        return null;
    }

    @Override
    public ServiceResult removeTag(Long houseId, String tag) {

        return null;
    }

    @Override
    public ServiceResult updateStatus(Long id, int status) {
        House house = houseDao.findById(id);
        if (house == null) {
            return ServiceResult.notFound();
        }

        if (house.getStatus() == status) {
            return new ServiceResult(false, "状态没有发生变化");
        }

        if (house.getStatus() == HouseStatus.RENTED.getValue()) {
            return new ServiceResult(false, "已出租的房源不允许修改状态");
        }

        if (house.getStatus() == HouseStatus.DELETED.getValue()) {
            return new ServiceResult(false, "已删除的资源不允许操作");
        }

        houseDao.updateStatus(id, status);

        //上架更新索引，其他情况都要删除索引
        if (status == HouseStatus.PASSES.getValue()) {
            searchService.index(id);
        } else {
            searchService.remove(id);
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceMultiResult<HouseDTO> query(RentSearch rentSearch) {
        if (rentSearch.getKeywords() != null && !rentSearch.getKeywords().isEmpty()) {
            ServiceMultiResult<Long> serviceResult = searchService.query(rentSearch);
            if (serviceResult.getTotal() == 0) {
                return new ServiceMultiResult<HouseDTO>(0L, new ArrayList<>());
            }

            return new ServiceMultiResult<HouseDTO>(serviceResult.getTotal(),
                    wrapperHouseResult(serviceResult.getResult()));
        }

        return simpleQuery(rentSearch);

    }

    @Override
    public ServiceMultiResult<HouseDTO> wholeMapQuery(MapSearch mapSearch) {
        ServiceMultiResult<Long> serviceResult = searchService.mapQuery(mapSearch.getCityEnName(), mapSearch.getOrderBy(),
                mapSearch.getOrderDirection(), mapSearch.getStart(), mapSearch.getSize());
        if (serviceResult == null || serviceResult.getTotal() == 0){
            return new ServiceMultiResult<HouseDTO>(0L, new ArrayList<>());
        }
        List<HouseDTO> houses = wrapperHouseResult(serviceResult.getResult());
        return new ServiceMultiResult<HouseDTO>(serviceResult.getTotal(), houses);
    }

    @Override
    @Transactional
    public ServiceResult addSubscribeOrder(Long houseId) {
        Long userId = userService.getLoginUserId();
        HouseSubscribe subscribe = subscribeDao.findByHouseIdAndUserId(houseId, userId);
        if (subscribe != null) {
            return new ServiceResult(false, "已加入预约");
        }

        House house = houseDao.findById(houseId);
        if (house == null) {
            return new ServiceResult(false, "查无此房");
        }

        subscribe = new HouseSubscribe();
        Date now = new Date();
        subscribe.setCreateTime(now);
        subscribe.setLastUpdateTime(now);
        subscribe.setUserId(userId);
        subscribe.setHouseId(houseId);
        subscribe.setStatus(HouseSubscribeStatus.IN_ORDER_LIST.getValue());
        subscribe.setAdminId(house.getAdminId());
        subscribeDao.save(subscribe);
        return ServiceResult.success();
    }

    @Override
    public ServiceMultiResult<HouseDTO> boundMapQuery(MapSearch mapSearch) {
        ServiceMultiResult<Long> serviceResult = searchService.mapQuery(mapSearch);
        if (serviceResult.getTotal() == 0) {
            return new ServiceMultiResult<HouseDTO>(0L, new ArrayList<>());
        }

        List<HouseDTO> houses = wrapperHouseResult(serviceResult.getResult());
        return new ServiceMultiResult<HouseDTO>(serviceResult.getTotal(), houses);
    }

    private List<HouseDTO> wrapperHouseResult(List<Long> houseIds) {
        List<HouseDTO> result = new ArrayList<>();

        Map<Long, HouseDTO> idToHouseMap = new HashMap<>();
        List<House> houses = houseDao.findAllByIds(houseIds);
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            idToHouseMap.put(house.getId(), houseDTO);
        });

        wrapperHouseList(houseIds, idToHouseMap);

        // 矫正顺序
        for (Long houseId : houseIds) {
            result.add(idToHouseMap.get(houseId));
        }
        return result;
    }

    @Override
    public ServiceMultiResult<HouseDTO> simpleQuery(RentSearch rentSearch) {
        PageHelper.startPage(rentSearch.getStart(), rentSearch.getSize());

        List<House> houses = houseDao.findAllByCondition(HouseStatus.PASSES.getValue(), rentSearch.getCityEnName(), rentSearch.getOrderBy(), rentSearch.getOrderDirection());

        PageInfo<House> pageInfo = new PageInfo<>(houses);

        List<HouseDTO> houseDTOS = new ArrayList<>();

        List<Long> houseIds = new ArrayList<>();
        Map<Long, HouseDTO> idToHouseMap = Maps.newHashMap();

        pageInfo.getList().forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            houseDTOS.add(houseDTO);

            houseIds.add(house.getId());
            idToHouseMap.put(house.getId(), houseDTO);
        });

        wrapperHouseList(houseIds, idToHouseMap);
        return new ServiceMultiResult<>(pageInfo.getTotal(), houseDTOS);
    }
    /**
     * 渲染详细信息 及 标签
     * @param houseIds
     * @param idToHouseMap
     */
    private void wrapperHouseList(List<Long> houseIds, Map<Long, HouseDTO> idToHouseMap) {
        List<HouseDetail> details = houseDetailDao.findAllByHouseIdIn(houseIds);
        details.forEach(houseDetail -> {
            HouseDTO houseDTO = idToHouseMap.get(houseDetail.getHouseId());
            HouseDetailDTO detailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
            houseDTO.setHouseDetail(detailDTO);
        });

        List<HouseTag> houseTags = houseTagDao.findAllByHouseIdIn(houseIds);
        houseTags.forEach(houseTag -> {
            HouseDTO house = idToHouseMap.get(houseTag.getHouseId());
            house.getTags().add(houseTag.getName());
        });
    }


    /**
     * 图片对象列表信息填充
     * @param form
     * @param houseId
     * @return
     */
    private List<HousePicture> generatePictures(HouseForm form, Long houseId) {
        List<HousePicture> pictures = new ArrayList<>();
        if (form.getPhotos() == null || form.getPhotos().isEmpty()) {
            return pictures;
        }

        for (PhotoForm photoForm : form.getPhotos()) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(houseId);
            picture.setCdnPrefix(cdnPrefix);
            picture.setPath(photoForm.getPath());
            picture.setWidth(photoForm.getWidth());
            picture.setHeight(photoForm.getHeight());
            pictures.add(picture);
        }
        return pictures;
    }

    /**
     * 房源信息填充
     * @param houseDetail 房源详细信息
     * @param houseForm 房源的表单信息
     * @return 房源的所有信息
     */
    private ServiceResult<HouseDTO> wrapperDetailInfo(HouseDetail houseDetail, HouseForm houseForm){
        Subway subway = subwayDao.findById(houseForm.getSubwayLineId());
        if (subway == null){
            return new ServiceResult<HouseDTO>(false, "Not valid subway line!", null);
        }

        SubwayStation subwayStation = subwayStationDao.findById(houseForm.getSubwayStationId());
        if (subwayStation == null || !subway.getId().equals(subwayStation.getSubwayId())){
            return new ServiceResult<>(false, "Not valid subway station", null);
        }
        houseDetail.setSubwayLineId(subway.getId());
        houseDetail.setSubwayLineName(subway.getName());

        houseDetail.setSubwayStationId(subwayStation.getId());
        houseDetail.setSubwayStationName(subwayStation.getName());

        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setAddress(houseForm.getDetailAddress());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setTraffic(houseForm.getTraffic());
        return null;
    }

    @Override
    public ServiceResult subscribe(Long houseId, Date orderTime, String telephone, String desc) {
        Long userId = userService.getLoginUserId();
        HouseSubscribe subscribe = subscribeDao.findByHouseIdAndUserId(houseId, userId);
        if (subscribe == null) {
            return new ServiceResult(false, "无预约记录");
        }

        if (subscribe.getStatus() != HouseSubscribeStatus.IN_ORDER_LIST.getValue()) {
            return new ServiceResult(false, "无法预约");
        }

        subscribe.setStatus(HouseSubscribeStatus.IN_ORDER_TIME.getValue());
        subscribe.setLastUpdateTime(new Date());
        subscribe.setTelephone(telephone);
        subscribe.setDesc(desc);
        subscribe.setOrderTime(orderTime);
        subscribeDao.update(subscribe);
        return ServiceResult.success();
    }

    @Override
    public ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> querySubscribeList(
            HouseSubscribeStatus status,
            int start,
            int size) {
        Long userId = userService.getLoginUserId();
        PageHelper.startPage(start/size, size);
        List<HouseSubscribe> allByUserIdAndStatus = subscribeDao.findAllByUserIdAndStatus(userId, status.getValue(), "create_time", "desc");
        PageInfo<HouseSubscribe> page = new PageInfo<>(allByUserIdAndStatus);
        return wrapper(page);
    }

    private ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> wrapper(PageInfo<HouseSubscribe> page) {
        List<Pair<HouseDTO, HouseSubscribeDTO>> result = new ArrayList<>();

        if (page.getSize() < 1) {
            return new ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>>(page.getTotal(), result);
        }

        List<HouseSubscribeDTO> subscribeDTOS = new ArrayList<>();
        List<Long> houseIds = new ArrayList<>();
        page.getList().forEach(houseSubscribe -> {
            subscribeDTOS.add(modelMapper.map(houseSubscribe, HouseSubscribeDTO.class));
            houseIds.add(houseSubscribe.getHouseId());
        });

        Map<Long, HouseDTO> idToHouseMap = new HashMap<>();
        Iterable<House> houses = houseDao.findAllByIds(houseIds);
        houses.forEach(house -> {
            idToHouseMap.put(house.getId(), modelMapper.map(house, HouseDTO.class));
        });

        for (HouseSubscribeDTO subscribeDTO : subscribeDTOS) {
            Pair<HouseDTO, HouseSubscribeDTO> pair = Pair.of(idToHouseMap.get(subscribeDTO.getHouseId()), subscribeDTO);
            result.add(pair);
        }

        return new ServiceMultiResult<>(page.getTotal(), result);
    }
    @Override
    @Transactional
    public ServiceResult cancelSubscribe(Long houseId) {
        Long userId = userService.getLoginUserId();
        HouseSubscribe subscribe = subscribeDao.findByHouseIdAndUserId(houseId, userId);
        if (subscribe == null) {
            return new ServiceResult(false, "无预约记录");
        }

        subscribeDao.delete(subscribe.getId());
        return ServiceResult.success();
    }
    @Override
    public ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> findSubscribeList(int start, int size) {
        Long userId = userService.getLoginUserId();
        PageHelper.startPage(start/size, size);
        List<HouseSubscribe> allByAdminIdAndStatus = subscribeDao.findAllByAdminIdAndStatus(userId, HouseSubscribeStatus.IN_ORDER_TIME.getValue(), "order_time", "desc");
        PageInfo<HouseSubscribe> page = new PageInfo<>(allByAdminIdAndStatus);
        return wrapper(page);
    }
    @Override
    @Transactional
    public ServiceResult finishSubscribe(Long houseId) {
        Long adminId = userService.getLoginUserId();
        HouseSubscribe subscribe = subscribeDao.findByHouseIdAndAdminId(houseId, adminId);
        if (subscribe == null) {
            return new ServiceResult(false, "无预约记录");
        }

        subscribeDao.updateStatus(subscribe.getId(), HouseSubscribeStatus.FINISH.getValue());
        houseDao.updateWatchTimes(houseId);
        return ServiceResult.success();
    }
}
