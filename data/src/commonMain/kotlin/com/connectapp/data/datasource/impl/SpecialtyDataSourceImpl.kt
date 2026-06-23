package com.connectapp.data.datasource.impl

import com.connectapp.data.datasource.SpecialtyDataSource
import com.connectapp.domain.model.Specialty

class SpecialtyDataSourceImpl: SpecialtyDataSource {
    override suspend fun getSpecialties(): List<Specialty> {
       return listOf(
            Specialty(id = "1", name = "specialty_musculoskeletal"),
            Specialty(id = "2", name = "specialty_sports"),
            Specialty(id = "3", name = "specialty_neurological"),
            Specialty(id = "4", name = "specialty_respiratory"),
            Specialty(id = "5", name = "specialty_pediatric"),
            Specialty(id = "6", name = "specialty_geriatric"),
            Specialty(id = "7", name = "specialty_pelvic"),
            Specialty(id = "8", name = "specialty_cardiovascular"),
            Specialty(id = "9", name = "specialty_oncological"),
            Specialty(id = "10", name = "specialty_palliative"),
            Specialty(id = "11", name = "specialty_chronic_pain"),
            Specialty(id = "12", name = "specialty_occupational"),
            Specialty(id = "13", name = "specialty_manual_therapy"),
            Specialty(id = "14", name = "specialty_invasive")
        )
    }
}
