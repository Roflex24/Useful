package my.help.finance.avito;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Эндпоинт построения топа квартир по настраиваемым весам.
 *
 * POST /api/apartments/rank
 * Тело запроса — {@link ScoringRequest}:
 * {
 *   "weights": {
 *     "pricePerMeter": 3,
 *     "metroDistance": 2,
 *     "trust": 2,
 *     "floorPosition": 1,
 *     "area": 1,
 *     "sellerExperience": 0,
 *     "freshness": 0,
 *     "priceTotal": 0
 *   }
 * }
 * Критерии с весом 0 или не упомянутые в весах не влияют на итоговую
 * оценку, но всё равно показываются в разбивке breakdown у каждой квартиры.
 *
 * Необязательный параметр ?limit=N — вернуть только первые N мест топа.
 * Ранжирование считается относительно ВСЕХ квартир, что сейчас есть в базе
 * (repository.findAll()) — если нужно ранжировать только часть (например,
 * один район), сначала отфильтруйте на уровне запроса/фронтенда.
 */
@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApartmentRankingController {

    private final ApartmentRepository repository;
    private final ApartmentScoringService scoringService;

    @PostMapping("/rank")
    public List<ApartmentScoreResult> rank(
            @RequestBody ScoringRequest request,
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        List<Apartment> apartments = repository.findAll();
        List<ApartmentScoreResult> ranked = scoringService.scoreAndRank(apartments, request.weights());

        if (limit != null && limit > 0 && limit < ranked.size()) {
            return ranked.subList(0, limit);
        }
        return ranked;
    }
}
