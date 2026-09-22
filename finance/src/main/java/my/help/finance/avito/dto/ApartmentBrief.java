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
        Boolean studio,
        Integer floor,
        Integer totalFloors,
        String address,
        String metroName,
        Integer metroMinutes,
        String renovation,
        Boolean rosreestrHasRestrictions,
        Boolean rosreestrDataMatches,
        String url
) {
    public static ApartmentBrief from(Apartment a) {
        return new ApartmentBrief(
                a.getId(), a.getAvitoId(), a.getTitle(),
                a.getPrice(), a.getPricePerMeter(), a.getTotalArea(),
                a.getRooms(), a.getStudio(), a.getFloor(), a.getTotalFloors(),
                a.getAddress() != null ? a.getAddress() : a.getFullAddress(),
                a.getMetroName() != null ? a.getMetroName() : a.getMetro(),
                a.getMetroMinutes(),
                a.getRenovation(),
                a.getRosreestrHasRestrictions(),
                a.getRosreestrDataMatches(),
                a.getUrl()
        );
    }
}