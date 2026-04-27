package se.hse.assistant_web_editor.backend.entity;

public enum TaxonomyType {
    RUBRIC,
    TAG,
    KEYWORD;

    public static TaxonomyType fromString(String text) {
        if (text == null) return null;
        for (TaxonomyType b : TaxonomyType.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип элемента справочника: " + text);
    }
}
