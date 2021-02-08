package com.hl.sf.service.house.impl;

import com.hl.sf.entity.Subway;
import com.hl.sf.entity.SubwayStation;
import com.hl.sf.entity.SupportAddress;
import com.hl.sf.repository.SupportAddressDao;
import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.service.house.IAddressService;
import com.hl.sf.web.dto.SubwayDTO;
import com.hl.sf.web.dto.SubwayStationDTO;
import com.hl.sf.web.dto.SupportAddressDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hl2333
 */
@Service("addressService")
public class AddressServiceImpl implements IAddressService {

    @Autowired
    SupportAddressDao supportAddressDao;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllCities() {
        List<SupportAddress> addresses =
                supportAddressDao.findAllByLevel(SupportAddress.Level.CITY.getValue());
        List<SupportAddressDTO> addressDTOS = new ArrayList<>(20);

        for (SupportAddress supportAddress : addresses){
            SupportAddressDTO target =
                    modelMapper.map(supportAddress, SupportAddressDTO.class);
            addressDTOS.add(target);
        }
        return new ServiceMultiResult<SupportAddressDTO>(Integer.toUnsignedLong(addressDTOS.size()), addressDTOS);
    }

    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllRegions(String city) {
        List<SupportAddress> addresses =
                supportAddressDao.findAllRegionsByCity(city);
        List<SupportAddressDTO> addressDTOS = new ArrayList<>(20);

        for (SupportAddress supportAddress : addresses){
            SupportAddressDTO target =
                    modelMapper.map(supportAddress, SupportAddressDTO.class);
            addressDTOS.add(target);
        }
        return new ServiceMultiResult<SupportAddressDTO>(Integer.toUnsignedLong(addressDTOS.size()), addressDTOS);
    }

    @Override
    public ServiceMultiResult<SubwayDTO> findAllSubways(String city) {
        List<Subway> subways =
                supportAddressDao.findAllSubwayByCity(city);
        List<SubwayDTO> subwayDTOS = new ArrayList<>(20);

        for (Subway subway : subways){
            SubwayDTO target =
                    modelMapper.map(subway, SubwayDTO.class);
            subwayDTOS.add(target);
        }
        return new ServiceMultiResult<SubwayDTO>(Integer.toUnsignedLong(subwayDTOS.size()), subwayDTOS);
    }

    @Override
    public ServiceMultiResult<SubwayStationDTO> findAllSubwayStations(Integer subwayId) {
        List<SubwayStation> subwayStations =
                supportAddressDao.findAllSubwayStationBySubwayLine(subwayId);
        List<SubwayStationDTO> subwayStationDTOS = new ArrayList<>(20);

        for (SubwayStation subway : subwayStations){
            SubwayStationDTO target =
                    modelMapper.map(subway, SubwayStationDTO.class);
            subwayStationDTOS.add(target);
        }
        return new ServiceMultiResult<SubwayStationDTO>(Integer.toUnsignedLong(subwayStationDTOS.size()), subwayStationDTOS);
    }
}
