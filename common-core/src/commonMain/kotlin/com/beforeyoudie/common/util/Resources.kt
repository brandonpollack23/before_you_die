package com.beforeyoudie.common.util

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc

fun getLocalizedResource(res: StringResource) = StringDesc.Resource(res).localized()