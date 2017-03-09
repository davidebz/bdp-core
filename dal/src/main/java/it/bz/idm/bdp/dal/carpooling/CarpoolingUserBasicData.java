package it.bz.idm.bdp.dal.carpooling;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.TypedQuery;

import it.bz.idm.bdp.dal.BasicData;
import it.bz.idm.bdp.dal.Station;

@Entity
public class CarpoolingUserBasicData extends BasicData{

	@Override
	public BasicData findByStation(EntityManager em, Station station) {
		TypedQuery<CarpoolingUserBasicData> query = em.createQuery("Select basic from CarpoolingUserBasicData basic where basic.station=:station", CarpoolingUserBasicData.class);
		query.setParameter("station", station);
		List<CarpoolingUserBasicData> resultList = query.getResultList();
		return !resultList.isEmpty()?resultList.get(0):null;
	}
	
	private Character gender;
	private Boolean pendular;
	private String name;
	private String type;
	private String carType;
	
	@OneToMany(cascade = CascadeType.ALL)
	private Map<Locale, Translation> location = new HashMap<Locale, Translation>();	
	@ManyToOne
	private Carpoolinghub hub;
	private String arrival;
	private String departure;
	
	
	public String getArrival() {
		return arrival;
	}
	public void setArrival(String arrival) {
		this.arrival = arrival;
	}
	public String getDeparture() {
		return departure;
	}
	public void setDeparture(String departure) {
		this.departure = departure;
	}
	public Carpoolinghub getHub() {
		return hub;
	}
	public void setHub(Carpoolinghub hub) {
		this.hub = hub;
	}
	public Character getGender() {
		return gender;
	}
	public void setGender(Character gender) {
		this.gender = gender;
	}

	public Boolean getPendular() {
		return pendular;
	}
	public void setPendular(Boolean pendular) {
		this.pendular = pendular;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCarType() {
		return carType;
	}
	public void setCarType(String carType) {
		this.carType = carType;
	}
	public Map<Locale, Translation> getLocation() {
		return location;
	}
	public void setLocation(Map<Locale, Translation> location) {
		this.location = location;
	}
}