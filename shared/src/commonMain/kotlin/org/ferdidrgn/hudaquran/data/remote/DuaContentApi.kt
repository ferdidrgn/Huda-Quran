package org.ferdidrgn.hudaquran.data.remote

import org.ferdidrgn.hudaquran.domain.model.DuaContentDto

/** Provider boundary for future licensed dua feeds; no unverified endpoint is hard-coded. */
interface DuaContentApi {
    suspend fun getDuas(language: String = "tr"): List<DuaContentDto>
}
