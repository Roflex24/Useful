package my.help.food.diet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DietModel {

    private Long id;
    private String name;
    private String description;
    private List<DietItemModel> items;
}
