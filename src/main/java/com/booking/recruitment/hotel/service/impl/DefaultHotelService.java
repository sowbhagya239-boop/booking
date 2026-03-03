package com.booking.recruitment.hotel.service.impl;

import com.booking.recruitment.hotel.exception.BadRequestException;
import com.booking.recruitment.hotel.model.Hotel;
import com.booking.recruitment.hotel.repository.HotelRepository;
import com.booking.recruitment.hotel.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
class DefaultHotelService implements HotelService {
  private final HotelRepository hotelRepository;
  private static final EARTH_RADIUS = 6371; //km

  @Autowired
  DefaultHotelService(HotelRepository hotelRepository) {
    this.hotelRepository = hotelRepository;
  }

  @Override
  public List<Hotel> getAllHotels() {
    return hotelRepository.findAll();
  }

  @Override
  public List<Hotel> getHotelsByCity(Long cityId) {
    return hotelRepository.findAll().stream()
        .filter((hotel) -> cityId.equals(hotel.getCity().getId()))
        .collect(Collectors.toList());
  }

  @Override
  public Hotel createNewHotel(Hotel hotel) {
    if (hotel.getId() != null) {
      throw new BadRequestException("The ID must not be provided when creating a new Hotel");
    }

    return hotelRepository.save(hotel);
  }

  @Override
  public boolean deleteHotel(Long id) {
    Hotel hotel = hotelRepository.findById(id);

    if(hotel == null || hotel.isDeleted()) {
      return false;
      }
    hotel.setDeleted(true);
    hotelRepository.save(hotel);
    return true;
  }

  @Override
  public Hotel getHotelById(Long id) {
    Hotel hotel = hotelRepository.findById(id);

    if(hotel == null || hotel.isDeleted()) {
      return null;
      }
    return hotel;
  }

  @Override
  public List<Hotel> findTop3ClosestHotels(Long cityId) {
    List<Hotel> hotels = hotelRespository.findByCityId(cityId);

    if(hotels == null) {
      return Collections.emptyList();
    }
    City city = hotels.get(0).getCity();
    double cityLat = city.getCityCentreLatitude();
    double cityLon = city.getCityCentreLongitude();

    return hotels.stream()
      .filter(h -> !h.isDeleted())
      .sorted(Comparator.comparingDouble(h -> getCalculateDistance(cityLat, cityLon,h.getLatitude(),h.getLongitude())))
      .limit(3)
      .collect(Collectors.toList());
    
  }
  private double getCalculateDistance(double lat1, double lon1, double lat2, double lon2) {
    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);

    double a = Math.sin(latDistance/2) * Math.sin(latDistance /2 ) 
      + Math.cos(Math.toRadians(lat1))
      * Math.cos(Math.toRadians(lat2))
      * Math.sin(lonDistance/2)
      * Math.sin(lonDistance/2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return EARTH_RADIUS *c;
  }

  

  
}
