package com.example.nsamovie.data.local_db


import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * מחלקת ממירי סוגים (TypeConverters) עבור Room Database.
 * היא ממירה בין רשימה של ז'אנרים (`List<String>`) למחרוזת JSON ולהיפך.
 */
class Converters {

    /**
     * ממיר רשימה (`List<String>`) למחרוזת JSON כדי לאחסן אותה במסד הנתונים.
     * @param list - רשימת מחרוזות (ז'אנרים של סרטים)
     * @return מחרוזת בפורמט JSON
     */
    @TypeConverter
    fun fromListToString(list: List<String>?): String {
        return Gson().toJson(list) // שימוש ב-Gson כדי להמיר את הרשימה למחרוזת JSON
    }

    /**
     * ממיר מחרוזת JSON חזרה לרשימה (`List<String>`) בעת שליפת הנתונים מהמסד.
     * @param string - מחרוזת בפורמט JSON
     * @return רשימת מחרוזות (ז'אנרים של סרטים)
     */
    @TypeConverter
    fun fromStringToList(string: String?): List<String> {
        return if (string == null) emptyList() // אם המחרוזת ריקה, נחזיר רשימה ריקה
        else Gson().fromJson(string, object : TypeToken<List<String>>() {}.type)
    }
}
