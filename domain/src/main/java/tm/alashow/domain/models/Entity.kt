/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain.models

interface Entity {
    val id: String
    var params: String
}

interface PaginatedEntity : Entity {
    var page: Int
}

abstract class BasePaginatedEntity : PaginatedEntity {

    companion object {
        const val defaultParams = ""
        const val defaultPage = 0
    }
}
