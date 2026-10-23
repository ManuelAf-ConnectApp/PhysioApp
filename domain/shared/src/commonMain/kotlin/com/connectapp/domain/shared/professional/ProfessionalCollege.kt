package com.connectapp.domain.shared.professional

/**
 * Closed catalogue of the 17 regional physiotherapy associations that make up the Consejo General
 * de Colegios de Fisioterapeutas de España (D9, checked 2026-10-20).
 * Ceuta and Melilla have no association of their own: their professionals pick the one they are
 * actually registered with.
 *
 * [name] is the internal id stored in the database: never rename an entry. [displayName] is the
 * simplified readable name, not the exact legal name; being the name of
 * a Spanish institution it is the same in every UI language.
 */
enum class ProfessionalCollege(val displayName: String) {
    ANDALUCIA("Colegio de Fisioterapeutas de Andalucía"),
    ARAGON("Colegio de Fisioterapeutas de Aragón"),
    ASTURIAS("Colegio de Fisioterapeutas de Asturias"),
    BALEARES("Colegio de Fisioterapeutas de Illes Balears"),
    CANARIAS("Colegio de Fisioterapeutas de Canarias"),
    CANTABRIA("Colegio de Fisioterapeutas de Cantabria"),
    CASTILLA_LA_MANCHA("Colegio de Fisioterapeutas de Castilla-La Mancha"),
    CASTILLA_Y_LEON("Colegio de Fisioterapeutas de Castilla y León"),
    CATALUNA("Colegio de Fisioterapeutas de Cataluña"),
    COMUNIDAD_VALENCIANA("Colegio de Fisioterapeutas de la Comunitat Valenciana"),
    EXTREMADURA("Colegio de Fisioterapeutas de Extremadura"),
    GALICIA("Colegio de Fisioterapeutas de Galicia"),
    LA_RIOJA("Colegio de Fisioterapeutas de La Rioja"),
    MADRID("Colegio de Fisioterapeutas de Madrid"),
    MURCIA("Colegio de Fisioterapeutas de la Región de Murcia"),
    NAVARRA("Colegio de Fisioterapeutas de Navarra"),
    PAIS_VASCO("Colegio de Fisioterapeutas del País Vasco"),
    ;

    companion object {
        /** The college stored as [id], or null for an id that is not in the catalogue. */
        fun fromId(id: String): ProfessionalCollege? = entries.firstOrNull { it.name == id }
    }
}
