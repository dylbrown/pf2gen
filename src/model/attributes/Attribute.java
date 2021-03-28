package model.attributes;

import model.ability_scores.AbilityScore;

import static model.util.StringUtils.capitalize;

public interface Attribute {

    static Attribute valueOf(String str) {
        int bracket = str.indexOf('(');
        if (bracket != -1) {
            BaseAttribute baseAttribute = BaseAttribute.robustValueOf(str.substring(0, bracket));
            String data = capitalize(str.trim().substring(bracket).trim().replaceAll("[()]", ""));
            return CustomAttribute.get(baseAttribute, data);
        } else {
            return BaseAttribute.robustValueOf(str);
        }
    }

    static Attribute valueOf(String str1, String str2) {
        if(str2 == null || str2.isBlank())
            return valueOf(str1);
        else
            return CustomAttribute.get(BaseAttribute.robustValueOf(str1), capitalize(str2.trim()));
    }

    BaseAttribute getBase();

    AbilityScore getKeyAbility();

    boolean hasACP();
}
