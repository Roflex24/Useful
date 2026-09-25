package my.help.finance.avito.dto;

import my.help.finance.avito.entity.Apartment;

public record ApartmentBrief(
        Long id,
        String avitoId,
        String title,
        Long price,
        Long pricePerMeter,
        Double totalArea,
        Integer rooms,
        Integer floor,
        Integer totalFloors,
        String address,
        String metroName,
        Integer metroMinutes,
        String renovation,
        String buildingType,
        Integer yearBuilt,
        String url
) {
    public static ApartmentBrief from(Apartment a) {
        return new ApartmentBrief(
                a.getId(), a.getAvitoId(), a.getTitle(),
                a.getPrice(), a.getPricePerMeter(), a.getTotalArea(),
                a.getRooms(), a.getFloor(), a.getTotalFloors(),
                a.getAddress(),
                a.getMetroName(),
                a.getMetroMinutes(),
                a.getRenovation(),
                a.getBuildingType(),
                a.getYearBuilt(),
                a.getUrl()
        );
    }
}