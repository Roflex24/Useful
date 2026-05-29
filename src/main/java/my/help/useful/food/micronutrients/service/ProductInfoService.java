package my.help.useful.food.micronutrients.service;

import my.help.useful.food.micronutrients.model.Micronutrient;
import my.help.useful.food.micronutrients.model.MicronutrientType;
import my.help.useful.food.micronutrients.model.ProductInfo;
import my.help.useful.food.micronutrients.repository.MicronutrientRepository;
import my.help.useful.food.micronutrients.repository.ProductInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductInfoService {

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Autowired
    private MicronutrientRepository micronutrientRepository;

    // ==================== Микронутриенты ====================

    public List<Micronutrient> getAllMicronutrients() {
        if (micronutrientRepository.count() == 0) {
            saveDefaultMicronutrients();
        }
        return micronutrientRepository.findAll();
    }

    public List<Micronutrient> getVitamins() {
        return micronutrientRepository.findByType(MicronutrientType.VITAMIN);
    }

    public List<Micronutrient> getMinerals() {
        return micronutrientRepository.findByType(MicronutrientType.MINERAL);
    }

    public List<Micronutrient> getFattyAcids() {
        return micronutrientRepository.findByType(MicronutrientType.FATTY_ACID);
    }

    public List<Micronutrient> getWaterSolubleVitamins() {
        List<String> waterSoluble = Arrays.asList(
                "B1 (Тиамин)", "B2 (Рибофлавин)", "B3 (Ниацин)", "B5 (Пантотеновая кислота)",
                "B6 (Пиридоксин)", "B7 (Биотин)", "B9 (Фолиевая кислота)", "B12 (Кобаламин)",
                "C (Аскорбиновая кислота)"
        );
        return getMicronutrientsByNames(waterSoluble);
    }

    public List<Micronutrient> getFatSolubleVitamins() {
        List<String> fatSoluble = Arrays.asList(
                "A (Ретинол)", "D (Кальциферол)", "E (Токоферол)", "K (Филлохинон)"
        );
        return getMicronutrientsByNames(fatSoluble);
    }

    public List<Micronutrient> getMajorMinerals() {
        List<String> majorMinerals = Arrays.asList(
                "Кальций", "Хлорид", "Магний", "Фосфор", "Калий", "Натрий", "Сера"
        );
        return getMicronutrientsByNames(majorMinerals);
    }

    public List<Micronutrient> getTraceMinerals() {
        List<String> traceMinerals = Arrays.asList(
                "Хром", "Медь", "Фтор", "Йод", "Железо", "Марганец", "Молибден", "Селен", "Цинк"
        );
        return getMicronutrientsByNames(traceMinerals);
    }

    public List<Micronutrient> getMicronutrientsByNames(List<String> names) {
        return micronutrientRepository.findAll().stream()
                .filter(m -> names.contains(m.getName()))
                .collect(Collectors.toList());
    }

    public Optional<Micronutrient> getMicronutrientByName(String name) {
        return micronutrientRepository.findByName(name);
    }

    // ==================== Продукты ====================

    public List<ProductInfo> getAllProducts() {
        return productInfoRepository.findAll();
    }

    public Optional<ProductInfo> getProductById(Long id) {
        return productInfoRepository.findById(id);
    }

    public ProductInfo saveProduct(ProductInfo productInfo) {
        return productInfoRepository.save(productInfo);
    }

    public void deleteProduct(Long id) {
        productInfoRepository.deleteById(id);
    }

    public List<ProductInfo> getProductsByCategory(String category) {
        return productInfoRepository.findByCategory(category);
    }

    public List<ProductInfo> searchProductsByName(String name) {
        return productInfoRepository.findByNameContainingIgnoreCase(name);
    }

    // ==================== Топ продуктов ====================

    public Map<String, List<Map<String, Object>>> getTopProductsByMicronutrient() {
        List<Micronutrient> micronutrients = getAllMicronutrients();
        List<ProductInfo> products = getAllProducts();
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

        for (Micronutrient micronutrient : micronutrients) {
            String micronutrientName = micronutrient.getName();
            List<Map<String, Object>> topProducts = new ArrayList<>();

            for (ProductInfo product : products) {
                String amount = product.getMicronutrients().get(micronutrientName);
                if (amount != null && !amount.isEmpty()) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("productId", product.getId());
                    entry.put("productName", product.getName());
                    entry.put("productCategory", product.getCategory());
                    entry.put("amount", amount);
                    topProducts.add(entry);
                }
            }

            // Сортировка по числовому значению
            topProducts.sort((a, b) -> {
                Double valA = extractNumber((String) a.get("amount"));
                Double valB = extractNumber((String) b.get("amount"));
                return valB.compareTo(valA);
            });

            // Берем топ-5
            result.put(micronutrientName, topProducts.size() > 5 ? topProducts.subList(0, 5) : topProducts);
        }
        return result;
    }

    public Map<String, List<Map<String, Object>>> getTopProductsForMicronutrient(String micronutrientName) {
        Map<String, List<Map<String, Object>>> allTop = getTopProductsByMicronutrient();
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put(micronutrientName, allTop.getOrDefault(micronutrientName, new ArrayList<>()));
        return result;
    }

    private Double extractNumber(String amountStr) {
        try {
            String num = amountStr.replaceAll("[^0-9.]", "");
            if (num.isEmpty()) return 0.0;
            return Double.parseDouble(num);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // ==================== Инициализация данных ====================

    private void saveDefaultMicronutrients() {
        List<Micronutrient> defaultMicronutrients = Arrays.asList(

                // ВОДОРАСТВОРИМЫЕ ВИТАМИНЫ (группа B)
                new Micronutrient("B1 (Тиамин)", "Превращает пищу в энергию, поддерживает нервную систему",
                        "1.2 мг", "Улучшает память, борется с усталостью, здоровье сердца",
                        "Бери-бери, усталость, раздражительность", MicronutrientType.VITAMIN, "мг"),

                new Micronutrient("B2 (Рибофлавин)", "Метаболизм, здоровье кожи и глаз",
                        "1.3 мг", "Поддерживает зрение, здоровье слизистых, производство энергии",
                        "Трещины на губах, дерматит, светобоязнь", MicronutrientType.VITAMIN, "мг"),

                new Micronutrient("B3 (Ниацин)", "Энергетический обмен, здоровье кожи и нервов",
                        "16 мг", "Снижает холестерин, улучшает кровообращение",
                        "Пеллагра, диарея, дерматит, деменция", MicronutrientType.VITAMIN, "мг"),

                new Micronutrient("B5 (Пантотеновая кислота)", "Синтез гормонов и жирных кислот",
                        "5 мг", "Заживление ран, снижение стресса, энергия",
                        "Усталость, онемение ног, головные боли", MicronutrientType.VITAMIN, "мг"),

                new Micronutrient("B6 (Пиридоксин)", "Метаболизм аминокислот, образование нейромедиаторов",
                        "1.7 мг", "Улучшает настроение, работу мозга, иммунитет",
                        "Раздражительность, анемия, депрессия", MicronutrientType.VITAMIN, "мг"),

                new Micronutrient("B7 (Биотин)", "Красота волос и ногтей, метаболизм жиров",
                        "30 мкг", "Укрепляет волосы, ногти, кожу, снижает сахар",
                        "Выпадение волос, сыпь, ломкие ногти", MicronutrientType.VITAMIN, "мкг"),

                new Micronutrient("B9 (Фолиевая кислота)", "Рост клеток, синтез ДНК",
                        "400 мкг", "Важен для развития плода, кроветворение, настроение",
                        "Анемия, дефекты нервной трубки плода", MicronutrientType.VITAMIN, "мкг"),

                new Micronutrient("B12 (Кобаламин)", "Нервная система, образование эритроцитов",
                        "2.4 мкг", "Предотвращает анемию, улучшает память, энергию",
                        "Усталость, онемение, анемия, депрессия", MicronutrientType.VITAMIN, "мкг"),

                // ВИТАМИН C
                new Micronutrient("C (Аскорбиновая кислота)", "Антиоксидант, синтез коллагена, иммунитет",
                        "90 мг", "Укрепляет иммунитет, заживляет раны, здоровье десен",
                        "Цинга, слабый иммунитет, медленное заживление", MicronutrientType.VITAMIN, "мг"),

                // ЖИРОРАСТВОРИМЫЕ ВИТАМИНЫ
                new Micronutrient("A (Ретинол)", "Зрение, иммунитет, здоровье кожи",
                        "900 мкг", "Улучшает зрение, защищает от инфекций, рост клеток",
                        "Куриная слепота, сухость кожи, слабый иммунитет", MicronutrientType.VITAMIN, "мкг"),

                new Micronutrient("D (Кальциферол)", "Усвоение кальция, здоровье костей, иммунитет",
                        "15 мкг", "Предотвращает остеопороз, улучшает настроение",
                        "Рахит, боли в костях, депрессия", MicronutrientType.VITAMIN, "мкг"),

                new Micronutrient("E (Токоферол)", "Антиоксидант, защита клеток, здоровье кожи",
                        "15 мг", "Замедляет старение, защищает сосуды, иммунитет",
                        "Мышечная слабость, анемия, проблемы с нервами", MicronutrientType.VITAMIN, "мг"),

                new Micronutrient("K (Филлохинон)", "Свертываемость крови, здоровье костей",
                        "120 мкг", "Останавливает кровотечения, укрепляет кости",
                        "Кровоточивость, синяки, слабые кости", MicronutrientType.VITAMIN, "мкг"),

                // ОСНОВНЫЕ МИНЕРАЛЫ (Макроэлементы)
                new Micronutrient("Кальций", "Кости, зубы, работа мышц и сердца",
                        "1000 мг", "Крепкие кости, нормальное сердцебиение, свертываемость крови",
                        "Остеопороз, судороги, кариес", MicronutrientType.MINERAL, "мг"),

                new Micronutrient("Хлорид", "Баланс жидкостей, пищеварение",
                        "2300 мг", "Поддержка pH баланса, производство желудочного сока",
                        "Нарушение кислотно-щелочного баланса", MicronutrientType.MINERAL, "мг"),

                new Micronutrient("Магний", "Работа мышц и нервов, энергия, сон",
                        "400 мг", "Снимает стресс, улучшает сон, здоровье сердца",
                        "Судороги, бессонница, тревога, аритмия", MicronutrientType.MINERAL, "мг"),

                new Micronutrient("Фосфор", "Кости, энергия, клеточные мембраны",
                        "700 мг", "Крепкие кости, производство энергии, здоровье почек",
                        "Слабость, боли в костях, анемия", MicronutrientType.MINERAL, "мг"),

                new Micronutrient("Калий", "Работа сердца, водный баланс, нервы",
                        "3500 мг", "Контроль давления, сокращение мышц, баланс жидкости",
                        "Слабость, аритмия, судороги, высокое давление", MicronutrientType.MINERAL, "мг"),

                new Micronutrient("Натрий", "Водный баланс, работа нервов и мышц",
                        "1500 мг", "Поддержка давления, передача нервных импульсов",
                        "Головокружение, слабость, судороги", MicronutrientType.MINERAL, "мг"),

                new Micronutrient("Сера", "Состав белков, здоровье кожи, волос, ногтей",
                        "Нет точной нормы", "Поддержка соединительной ткани, детоксикация",
                        "Проблемы с кожей, волосами, суставами", MicronutrientType.MINERAL, "мг"),

                // МИКРОЭЛЕМЕНТЫ
                new Micronutrient("Хром", "Метаболизм сахара, чувствительность к инсулину",
                        "35 мкг", "Контроль сахара в крови, снижение тяги к сладкому",
                        "Проблемы с сахаром, усталость, набор веса", MicronutrientType.MINERAL, "мкг"),

                new Micronutrient("Медь", "Кроветворение, нервы, иммунитет",
                        "900 мкг", "Иммунитет, здоровье сосудов, выработка энергии",
                        "Анемия, неврологические проблемы, слабость", MicronutrientType.MINERAL, "мкг"),

                new Micronutrient("Фтор", "Зубы, кости, профилактика кариеса",
                        "4 мг", "Укрепляет эмаль зубов, поддерживает кости",
                        "Кариес, слабые кости", MicronutrientType.MINERAL, "мг"),

                new Micronutrient("Йод", "Щитовидная железа, метаболизм",
                        "150 мкг", "Производство гормонов щитовидной железы, энергия",
                        "Зоб, усталость, набор веса, проблемы с кожей", MicronutrientType.MINERAL, "мкг"),

                new Micronutrient("Железо", "Кроветворение, транспорт кислорода",
                        "18 мг", "Энергия, иммунитет, когнитивные функции",
                        "Анемия, усталость, бледность, одышка", MicronutrientType.MINERAL, "мг"),

                new Micronutrient("Марганец", "Метаболизм, кости, антиоксидант",
                        "2.3 мг", "Здоровье костей, заживление ран, метаболизм",
                        "Слабость, проблемы с костями, судороги", MicronutrientType.MINERAL, "мг"),

                new Micronutrient("Молибден", "Работа ферментов, детоксикация",
                        "45 мкг", "Метаболизм аминокислот, выведение токсинов",
                        "Редко, возможны аллергии", MicronutrientType.MINERAL, "мкг"),

                new Micronutrient("Селен", "Антиоксидант, щитовидная железа, иммунитет",
                        "55 мкг", "Защита клеток, здоровье щитовидки, репродукция",
                        "Усталость, выпадение волос, слабый иммунитет", MicronutrientType.MINERAL, "мкг"),

                new Micronutrient("Цинк", "Иммунитет, заживление ран, синтез ДНК",
                        "11 мг", "Укрепляет иммунитет, здоровье кожи, волос, вкус",
                        "Выпадение волос, частые инфекции, задержка роста", MicronutrientType.MINERAL, "мг"),

                // ЖИРНЫЕ КИСЛОТЫ
                new Micronutrient("Омега-3 (ALA)", "Растительная форма омега-3, противовоспалительное",
                        "1.6 г", "Здоровье сердца, мозга, снижение воспалений",
                        "Сухость кожи, воспаления, плохая память", MicronutrientType.FATTY_ACID, "г"),

                new Micronutrient("Омега-3 (EPA)", "Эйкозапентаеновая кислота, из рыбы",
                        "250 мг", "Снижение воспалений, здоровье сердца, настроение",
                        "Депрессия, воспаления, боли в суставах", MicronutrientType.FATTY_ACID, "мг"),

                new Micronutrient("Омега-3 (DHA)", "Докозагексаеновая кислота, для мозга",
                        "250 мг", "Здоровье мозга, зрения, нервной системы",
                        "Проблемы с памятью, зрением, развитием", MicronutrientType.FATTY_ACID, "мг"),

                new Micronutrient("Омега-6", "Линолевая кислота, энергия",
                        "12 г", "Здоровье кожи, рост волос, иммунитет",
                        "Сухость кожи, экзема, выпадение волос", MicronutrientType.FATTY_ACID, "г"),

                new Micronutrient("Омега-9", "Олеиновая кислота, для сердца",
                        "Нет точной нормы", "Здоровье сердца, снижение холестерина, энергия",
                        "Повышенный холестерин, болезни сердца", MicronutrientType.FATTY_ACID, "г")
        );

        micronutrientRepository.saveAll(defaultMicronutrients);
    }
}