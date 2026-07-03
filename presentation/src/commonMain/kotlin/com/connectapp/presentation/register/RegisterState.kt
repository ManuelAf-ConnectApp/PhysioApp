/*
Author: Manuel María Alconchel Fernández
E-mail: incidencias@connectapp.es
Date: 30/06/2006

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.connectapp.presentation.register

import com.connectapp.domain.validator.model.ValidationError

data class RegisterState(
    val firstName: String,
    val firstNameError: ValidationError? = null,
    val lastName: String,
    val lastNameError: ValidationError? = null,
    val email: String,
    val emailError: ValidationError? = null,
    val password: String,
    val passwordError: ValidationError? = null,
    val confirmPassword: String,
    val confirmPasswordError: ValidationError? = null,
    val isLoading: Boolean,
    val isRegistered: Boolean,
    val isPasswordVisible: Boolean,
    val isConfirmPasswordVisible: Boolean,
    val errorMessage: String?
) {
    companion object {
        val EMPTY = RegisterState(
            firstName = "",
            lastName = "",
            email = "",
            password = "",
            confirmPassword = "",
            isLoading = false,
            isRegistered = false,
            isPasswordVisible = false,
            isConfirmPasswordVisible = false,
            errorMessage = null
        )
    }

    val isFormValid: Boolean =
        firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() &&
                firstNameError == null && lastNameError == null && emailError == null && passwordError == null && confirmPasswordError == null
}
