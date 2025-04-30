package com.example.electricianappnew.data.local.converters

import androidx.room.TypeConverter
import com.example.electricianappnew.data.model.NecCuValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NecDataTypeConverters {

    @TypeConverter
    fun fromCuValueList(cuValues: List<NecCuValue>?): String? {
        if (cuValues == null) {
            return null
        }
        val type = object : TypeToken<List<NecCuValue>>() {}.type
        return Gson().toJson(cuValues, type)
    }

    @TypeConverter
    fun toCuValueList(cuValuesString: String?): List<NecCuValue>? {
        if (cuValuesString == null) {
            return null
        }
        val type = object : TypeToken<List<NecCuValue>>() {}.type
        return Gson().fromJson(cuValuesString, type)
    }
}
