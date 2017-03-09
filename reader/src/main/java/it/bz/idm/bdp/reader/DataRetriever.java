package it.bz.idm.bdp.reader;


import it.bz.idm.bdp.dal.Alarm;
import it.bz.idm.bdp.dal.AlarmSpecification;
import it.bz.idm.bdp.dal.DataType;
import it.bz.idm.bdp.dal.Station;
import it.bz.idm.bdp.dal.bluetooth.Linkstation;
import it.bz.idm.bdp.dal.parking.ParkingStation;
import it.bz.idm.bdp.dal.util.JPAUtil;
import it.bz.idm.bdp.dto.ChildDto;
import it.bz.idm.bdp.dto.ParkingRecordDto;
import it.bz.idm.bdp.dto.RecordDto;
import it.bz.idm.bdp.dto.StationDto;
import it.bz.idm.bdp.dto.TypeDto;
import it.bz.idm.bdp.dto.parking.ParkingStationDto;
import it.bz.tis.integreen.util.IntegreenException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

public class DataRetriever {

	//Utility
	private List<String> getStationTypes(EntityManager em){
		List<String> result = null;
		result = Station.findStationTypes(em);
		return result;

	}

	//API additions for V2 
	public List<ChildDto> getChildren(String type, String parent){
		EntityManager em = JPAUtil.createEntityManager();
		try{
			Station station = (Station) JPAUtil.getInstanceByType(em, type);
			List<ChildDto> children = null;
			if (station != null){

				children = station.findChildren(em,parent);
				return children;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			em.close();	
		}
		return null;
	}
	public List<TypeDto> getTypes(String type, String stationId) {
		List<TypeDto> dataTypes = null;
		EntityManager em = JPAUtil.createEntityManager();
		try{
			Station station = (Station) JPAUtil.getInstanceByType(em, type);
			if (station!=null)
				dataTypes = station.findTypes(em, stationId);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			em.close();
		}
		return dataTypes;
	}
	
	//APIv1
	
	public List<StationDto> getStationDetails(String type, String id) {
		List<StationDto> stations = new ArrayList<StationDto>();
		EntityManager em = JPAUtil.createEntityManager();
		try{
			Station station = (Station) JPAUtil.getInstanceByType(em, type);
			Station stationById = null;
			if (id != null) {
				stationById = station.findStation(em,id);
			}
			if ((stationById == null && id == null) || (stationById != null && station.getClass().equals(stationById.getClass())))
				stations = station.findStationsDetails(em,stationById);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			em.close();	
		}
		return stations;
	}

	public List<String> getStations(String type){
		List<String> stations = null;
		EntityManager em = JPAUtil.createEntityManager();
		try{
			if (getStationTypes(em).contains(type))
				stations = Station.findActiveStations(em,type);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			em.close();	
		}
		return stations;
	}


	public List<StationDto> getAvailableStations(){
		List<StationDto> availableStations = new ArrayList<StationDto>();
		EntityManager em = JPAUtil.createEntityManager();
		try{
			availableStations = new Linkstation().findAvailableStations(em);

		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			em.close();
		}
		return availableStations;
	}

	public List<String[]> getDataTypes(String type, String stationId) {
		List<String[]> dataTypes = null;
		EntityManager em = JPAUtil.createEntityManager();
		try{
			Station station = (Station) JPAUtil.getInstanceByType(em, type);
			if (station!=null)
				dataTypes = station.findDataTypes(em,stationId);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			em.close();
		}
		return dataTypes;
	}

	public List<String[]> getDataTypes(String type){
		return getDataTypes(type, null);
	}

	public Date getDateOfLastRecord(String stationTypology, String stationcode,String cname,Integer period) {
		EntityManager em = JPAUtil.createEntityManager();
		Date dateOfLastRecord = null;
		try{
			Station s = (Station) JPAUtil.getInstanceByType(em, stationTypology);
			Station station = s.findStation(em, stationcode);
			DataType type = DataType.findByCname(em,cname);
			dateOfLastRecord = s.getDateOfLastRecord(em,station, type, period);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			em.close();
		}
		return dateOfLastRecord;
	}

	public Object getLastRecord(String stationTypology, String stationcode, String cname, Integer period) {
		ParkingRecordDto dto = null;
		EntityManager em = JPAUtil.createEntityManager();
		try{
			Station s = (Station) JPAUtil.getInstanceByType(em, stationTypology);
			Station station = s.findStation(em, stationcode);
			dto = (ParkingRecordDto) station.findLastRecord(em,cname,period);
			List<Alarm> alarms = Alarm.findAlarmByStationAndTimestamp(em, station,new Date(dto.getTimestamp()));
			if (!alarms.isEmpty()){
				AlarmSpecification alarmSpec= alarms.get(0).getSpecification();
				return new IntegreenException(alarmSpec.getName(),alarmSpec.getDescription());
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			em.close();
		}
		return null;
	}
	public Object getNewestRecord(String typology, List<Object> params) {
		RecordDto dto = null;

		EntityManager em = JPAUtil.createEntityManager();
		try{
			Station s = (Station) JPAUtil.getInstanceByType(em, typology);
			if (s != null){
				String stationcode = (String) params.get(0);
				Station station = s.findStation(em, stationcode);
				String cname = (String) params.get(1);
				Integer period = (Integer) params.get(2);
				dto = (RecordDto) station.findLastRecord(em,cname,period);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			em.close();
		}
		return dto;
	}
	public List<RecordDto> getRecords(String stationtypology,String identifier, String type, Long seconds, Integer period){
		Date end = new Date();
		Date start = new Date(end.getTime()-(seconds*1000l));
		return getRecords(stationtypology, identifier, type, start, end, period);
	}
	public List<RecordDto> getRecords(String stationtypology,String identifier, String type, Date start, Date end, Integer period){
		EntityManager em = JPAUtil.createEntityManager();
		List<RecordDto> records = new ArrayList<RecordDto>();
		try{
			Station s = (Station) JPAUtil.getInstanceByType(em, stationtypology);
			Station station = s.findStation(em, identifier);
			if (station != null) {
				records.addAll(station.getRecords(em,type,start,end,period));
				return records;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		finally{
			em.close();
		}
		return records;
	}

	//LEGACY API FOR PARKING
	@Deprecated
	public List<String> getParkingIds(){
		EntityManager em = JPAUtil.createEntityManager();
		List<String> stations = Station.findActiveStations(em,"ParkingStation");
		em.close();
		return stations ;
	}
	@Deprecated
	public Map<String,Object> getParkingStation(String identifier){
		return ParkingStation.findParkingStation(identifier);
	}

	@Deprecated
	public List<ParkingStationDto> getParkingStations(){
		return ParkingStation.findParkingStationsMetadata();
	}
	@Deprecated
	public Integer getNumberOfFreeSlots(String identifier){
		return ParkingStation.findNumberOfFreeSlots(identifier);
	}
	@Deprecated
	public List<Object[]> getStoricData(String identifier,Integer minutes){
		return ParkingStation.findStoricData(identifier,minutes);

	}
	@Deprecated
	public List<Object> getFreeSlotsByTimeFrame(String identifier, String startDateString, String endDateString,
			String datePattern) {
		return ParkingStation.findFreeSlotsByTimeFrame(identifier,startDateString, endDateString,datePattern);
	}

}