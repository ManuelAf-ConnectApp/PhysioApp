package com.connectapp.data.mapper

import com.connectapp.data.database.UserEntity
import com.connectapp.domain.model.User


class UserMapper {

    /**
     * Transforma el modelo de la Base de Datos al modelo de Dominio.
     * Esto es lo que se envía hacia la UI (pasando por el caso de uso).
     */
    fun toDomain(entity: UserEntity): User {
        return User(
            id = entity.id,
            firstName = entity.firstName,
            lastName = entity.lastName,
            email = entity.email,
            password = entity.password
        )
    }

    /**
     * Transforma el modelo de Dominio al modelo de la Base de Datos.
     * Útil si necesitas pasar la entidad completa a otra capa de datos.
     */
    fun toEntity(domain: User): UserEntity {
        return UserEntity(
            id = domain.id,
            firstName = domain.firstName,
            lastName = domain.lastName,
            email = domain.email,
            password = domain.password
        )
    }
}