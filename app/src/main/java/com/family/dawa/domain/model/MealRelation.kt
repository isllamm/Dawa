package com.family.dawa.domain.model

enum class MealRelation(val arabicLabel: String, val icon: String) {
    NONE("بدون ارتباط بالأكل", "⏰"),
    BEFORE_BREAKFAST("قبل الفطار", "🍽️⬅"),
    AFTER_BREAKFAST("بعد الفطار", "🍽️➡"),
    BEFORE_LUNCH("قبل الغدا", "🍽️⬅"),
    AFTER_LUNCH("بعد الغدا", "🍽️➡"),
    BEFORE_DINNER("قبل العشا", "🍽️⬅"),
    AFTER_DINNER("بعد العشا", "🍽️➡"),
    BEFORE_SLEEP("قبل النوم", "🛏️")
}
