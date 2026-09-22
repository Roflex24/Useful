package my.help.finance.avito.dto;

import my.help.finance.avito.entity.Apartment;
import my.help.finance.avito.entity.ApartmentBadge;

import java.util.List;

public record ApartmentDetails(
        Long id,
        String avitoId,
        String title,
        Long price,
        Long pricePerMeter,
        String currency,
        Double totalArea,
        Double kitchenArea,
        Double livingArea,
        Integer rooms,
        Boolean studio,
        Integer floor,
        Integer totalFloors,
        Double ceilingHeight,
        String address,
        String fullAddress,
        String district,
        String metroName,
        Integer metroMinutes,
        Double latitude,
        Double longitude,
        String renovation,
        String bathroomType,
        String balconyOrLoggia,
        String windowsView,
        String buildingType,
        Integer yearBuilt,
        String passengerElevator,
        String freightElevator,
        String parking,
        String yardFeatures,
        String houseUtilities,
        Double houseRating,
        Integer houseReviewsCount,
        String sellerName,
        Integer sellerCompletedListingsCount,
        String publishedDateRaw,
        Boolean isNew,
        Boolean isPromoted,
        Integer totalViews,
        Integer todayViews,
        Boolean rosreestrHasRestrictions,
        Boolean rosreestrDataMatches,
        String rosreestrOwnersCountRaw,
        String rosreestrLastOwnerChangeRaw,
        String rosreestrCadastralNumber,
        List<String> badges,
        String description,
        String url
) {
    public static ApartmentDetails from(Apartment a) {
        String desc = firstNonBlank(a.getDescriptionFullDetail(), a.getDescriptionFull(), a.getDescription());
        List<String> badges = a.getBadges() == null ? List.of()
                : a.getBadges().stream()
                .map(ApartmentBadge::getLabel)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();

        return new ApartmentDetails(
                a.getId(), a.getAvitoId(), a.getTitle(),
                a.getPrice(), a.getPricePerMeter(), a.getCurrency(),
                a.getTotalArea(), a.getKitchenArea(), a.getLivingArea(),
                a.getRooms(), a.getStudio(), a.getFloor(), a.getTotalFloors(),
                a.getCeilingHeight(),
                a.getAddress(), a.getFullAddress(), a.getDistrict(),
                a.getMetroName() != null ? a.getMetroName() : a.getMetro(),
                a.getMetroMinutes(), a.getLatitude(), a.getLongitude(),
                a.getRenovation(), a.getBathroomType(), a.getBalconyOrLoggia(),
                a.getWindowsView(), a.getBuildingType(), a.getYearBuilt(),
                a.getPassengerElevator(), a.getFreightElevator(), a.getParking(),
                a.getYardFeatures(), a.getHouseUtilities(), a.getHouseRating(),
                a.getHouseReviewsCount(),
                a.getSellerName(), a.getSellerCompletedListingsCount(),
                a.getPublishedDateRaw(), a.getIsNew(), a.getIsPromoted(),
                a.getTotalViews(), a.getTodayViews(),
                a.getRosreestrHasRestrictions(), a.getRosreestrDataMatches(),
                a.getRosreestrOwnersCountRaw(), a.getRosreestrLastOwnerChangeRaw(),
                a.getRosreestrCadastralNumber(),
                badges,
                desc, a.getUrl()
        );
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return null;
    }
}