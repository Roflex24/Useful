package my.help.food.common.enums;

import lombok.Getter;

@Getter
public enum Shop {
    CHISHIK("Чижик"),
    SMART("Смарт"),
    PIATIOROCHKA("Пятерочка"),
    MAGNIT("Магнит"),
    SVETOFOR("Светофор");

    private final String displayName;

    Shop(String displayName) {
        this.displayName = displayName;
    }
}

/*
BEGIN;

ALTER TABLE products ADD COLUMN shop_str VARCHAR(255);

UPDATE products
SET shop_str = CASE shop
WHEN 0 THEN 'CHISHIK'
WHEN 1 THEN 'SMART'
WHEN 2 THEN 'PIATIOROCHKA'
WHEN 3 THEN 'MAGNIT'
WHEN 4 THEN 'SVETOFOR'
ELSE NULL
END;

ALTER TABLE products DROP COLUMN shop;

ALTER TABLE products RENAME COLUMN shop_str TO shop;

COMMIT;
*/
