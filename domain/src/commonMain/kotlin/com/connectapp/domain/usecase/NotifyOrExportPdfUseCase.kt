package com.connectapp.domain.usecase

import com.connectapp.domain.resources.GeneratePdfResource

class NotifyOrExportPdfUseCase(
    private val generatePdfResource: GeneratePdfResource
) {
    operator fun invoke(fileName: String) {
        generatePdfResource.notifyOrShareFile(fileName = fileName)
    }
}
