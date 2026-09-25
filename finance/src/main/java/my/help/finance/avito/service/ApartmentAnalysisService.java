package my.help.finance.avito.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.avito.entity.Apartment;
import my.help.finance.avito.repository.ApartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApartmentAnalysisService {

    /** Верхняя граница размера промпта — защита от слишком длинных чатов. */
    private static final int MAX_PROMPT_LENGTH = 60_000;
    /** Ограничение количества квартир, попадающих в промпт. */
    private static final int MAX_APARTMENTS_IN_PROMPT = 400;

    private final ApartmentRepository repository;

    @Transactional(readOnly = true)
    public String buildAnalysisPrompt() {
        List<Apartment> apartments = repository.findAll().stream()
                .filter(a -> a.getPrice() != null)
                .sorted(Comparator.comparing(Apartment::getPrice))
                .limit(MAX_APARTMENTS_IN_PROMPT)
                .toList();

        if (apartments.isEmpty()) {
            return "В базе нет квартир с указанной ценой — анализировать нечего.";
        }

        StringBuilder sb = new StringBuilder(16_384);
        sb.append("Ты — эксперт по рынку недвижимости. Проанализируй базу объявлений ")
                .append("о продаже квартир и дай структурированные выводы.\n\n");
        sb.append("Всего объявлений в выборке: ").append(apartments.size()).append("\n\n");
        sb.append("Формат строки: avitoId | цена ₽ | ₽/м² | комн. | площадь м² | этаж | ")
                .append("метро | адрес | ремонт | год | тип дома\n\n");

        for (Apartment a : apartments) {
            sb.append("- ")
                    .append(nz(a.getAvitoId())).append(" | ")
                    .append(nz(a.getPrice())).append(" | ")
                    .append(nz(a.getPricePerMeter())).append(" | ")
                    .append(nz(a.getRooms())).append(" | ")
                    .append(nz(a.getTotalArea())).append(" | ")
                    .append(floorLabel(a)).append(" | ")
                    .append(nz(a.getMetroName())).append(" | ")
                    .append(nz(a.getAddress())).append(" | ")
                    .append(nz(a.getRenovation())).append(" | ")
                    .append(nz(a.getYearBuilt())).append(" | ")
                    .append(nz(a.getBuildingType()))
                    .append("\n");
        }

        sb.append("\nДай ответ строго по пунктам:\n")
                .append("1. Общая картина рынка: диапазон цен, медиана, средняя цена за м².\n")
                .append("2. Распределение по числу комнат и по районам / станциям метро.\n")
                .append("3. Топ-5 самых выгодных предложений по цене за м² — с обоснованием.\n")
                .append("4. Топ-5 переоценённых предложений — с обоснованием.\n")
                .append("5. Замеченные тренды и аномалии.\n")
                .append("6. Практические рекомендации: на что обратить внимание при покупке.\n");

        String result = sb.toString();
        if (result.length() > MAX_PROMPT_LENGTH) {
            result = result.substring(0, MAX_PROMPT_LENGTH) + "\n\n[...промпт усечён...]";
        }
        log.info("Собран промпт для анализа: {} символов, {} квартир",
                result.length(), apartments.size());
        return result;
    }

    private static String nz(Object o) { return o == null ? "—" : o.toString(); }

    private static String floorLabel(Apartment a) {
        if (a.getFloor() == null) return "—";
        return a.getTotalFloors() == null
                ? a.getFloor() + "/—"
                : a.getFloor() + "/" + a.getTotalFloors();
    }
}