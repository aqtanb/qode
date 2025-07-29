package com.qodein.core.data.mapper

import com.qodein.core.model.Country
import com.simon.xmaterialccp.data.CountryData
import com.simon.xmaterialccp.data.utils.getFlags

fun CountryData.toDomain(): Country =
    Country(
        code = countryCode,
        name = cNames,
        phoneCode = countryPhoneCode,
        flagResourceId = getFlags(countryCode),
    )

fun Country.toCountryData(): CountryData =
    CountryData(
        cCodes = code,
        countryPhoneCode = phoneCode,
        cNames = name,
        flagResID = flagResourceId,
    )
