/**
 * BDP data - Data Access Layer for the Big Data Platform
 * Copyright © 2018 IDM Südtirol - Alto Adige (info@idm-suedtirol.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see LICENSES/GPL-3.0.txt). If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0
 */
package it.bz.idm.bdp.dal.charger;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;

import it.bz.idm.bdp.dal.MeasurementStation;
import it.bz.idm.bdp.dal.Station;
import it.bz.idm.bdp.dal.authentication.BDPRole;
import it.bz.idm.bdp.dto.ChildDto;
import it.bz.idm.bdp.dto.SimpleRecordDto;
import it.bz.idm.bdp.dto.StationDto;
import it.bz.idm.bdp.dto.emobility.ChildPlugDto;
import it.bz.idm.bdp.dto.emobility.EchargingStationDto;
import it.bz.idm.bdp.dto.emobility.OutletDtoV2;

@Entity
public class EChargingStation extends MeasurementStation{

	@Override
	public List<StationDto> convertToDtos(EntityManager em, List<Station> resultList) {
		List<StationDto> stationList = new ArrayList<StationDto>();
		for (Station station: resultList){
			EchargingstationBasicData basic = (EchargingstationBasicData) new EchargingstationBasicData().findByStation(em, station);
			Double x = null,y = null;
			if (station.getPointprojection() != null){
				y = station.getPointprojection().getY();
				x = station.getPointprojection().getX();
			}
			if (basic != null) {
				EchargingStationDto dto = new EchargingStationDto(station.getStationcode(),station.getName(),y,x,basic.getChargingPointsCount(),basic.getAssetProvider(),basic.getCity(),basic.getState(),basic.getAddress());
				dto.setAccessInfo(basic.getAccessInfo());
				dto.setAccessType(basic.getAccessType());
				dto.setFlashInfo(basic.getFlashInfo());
				dto.setLocationServiceInfo(basic.getLocationServiceInfo());
				dto.setPaymentInfo(basic.getPaymentInfo());
				dto.setReservable(basic.getReservable());
				dto.setMunicipality(station.getMunicipality());
				dto.setCategories((basic.getCategories()!= null) ? basic.getCategories().split(",") : null);
				stationList.add(dto);
			}
		}
		return stationList;
	}

	@Override
	public void sync(EntityManager em, Station station, StationDto dto) {
		if (dto instanceof EchargingStationDto){
			EchargingStationDto charger = (EchargingStationDto) dto;
			EChargingStation eStation = (EChargingStation) station;
			EchargingstationBasicData basic = EchargingstationBasicData.findBasicByStation(em,eStation);
			if (basic == null){
				basic = new EchargingstationBasicData();
				basic.setStation(eStation);
				em.persist(basic);
			}
			if ("REMOVED".equals(charger.getState()))
				eStation.setActive(false);
			if ("ACTIVE".equals(charger.getState()))
				eStation.setAvailable(true);
			eStation.setName(dto.getName());
			basic.setAssetProvider(charger.getProvider());
			basic.setCity(charger.getCity());
			basic.setChargingPointsCount(charger.getCapacity());
			basic.setState(charger.getState());
			basic.setPaymentInfo(charger.getPaymentInfo());
			basic.setFlashInfo(charger.getFlashInfo());
			basic.setLocationServiceInfo(charger.getLocationServiceInfo());
			basic.setAccessInfo(charger.getAccessInfo());
			basic.setAddress(charger.getAddress());
			basic.setReservable(charger.getReservable());
			basic.setAccessType(charger.getAccessType());
			basic.setCategories(charger.getCategories() != null ? String.join(",",charger.getCategories()) : null);
			em.merge(basic);
			em.merge(eStation);
		}
	}

	@Override
	public List<ChildDto> findChildren(EntityManager em, String parent) {
		List<ChildDto> dtos = new ArrayList<ChildDto>();
		List<Station> plugs = new ArrayList<Station>();
		if (parent == null)
			plugs = new EChargingPlug().findStations(em);
		else {
			Station station = this.findStation(em, parent);
			if (station!= null && station instanceof EChargingStation)
				plugs = new EChargingPlug().findByParent(em,station);
		}
		BDPRole role = BDPRole.fetchGuestRole(em);
		if (! plugs.isEmpty())
			for (Station s : plugs){
				EChargingPlug plug = (EChargingPlug) s;
				EChargingPlugBasicData basic = (EChargingPlugBasicData) new EChargingPlugBasicData().findByStation(em, plug);
				ChildPlugDto dto = new ChildPlugDto();
				dto.setIdentifier(plug.getStationcode());
				SimpleRecordDto record = (SimpleRecordDto) plug.findLastRecord(em, null, null, role);
				Integer value = ((Double) record.getValue()).intValue();
				dto.setAvailable(value == 1);

				if (basic!= null){
					if (basic.geteStation() != null)
						dto.setStation(basic.geteStation().getStationcode());
					List<OutletDtoV2> outletDtos = EChargingPlugOutlet.toDto(plug.getOutlets());
					dto.setOutlets(outletDtos);
					dto.setIdentifier(plug.getStationcode());
				}
				dtos.add(dto);
			}
		return dtos;
	}

}
