/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.common.compose.previews

import androidx.compose.ui.tooling.preview.Preview

private const val Group = "Device Previews"

@TabletPreview
@SmallTabletPreview
@PhonePreview
@SmallPhonePreview
annotation class DevicePreview

@Preview(
    name = "Tablet",
    device = "spec:shape=Normal,width=1280,height=800,unit=dp,dpi=480",
    group = Group,
    showSystemUi = true,
)
annotation class TabletPreview

@Preview(
    name = "Tablet",
    device = "spec:shape=Normal,width=600,height=480,unit=dp,dpi=480",
    group = Group,
    showSystemUi = true,
)
annotation class SmallTabletPreview

@Preview(
    name = "Phone",
    group = Group,
    device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480",
    showSystemUi = true,
)
annotation class PhonePreview

@Preview(
    name = "Phone",
    group = Group,
    device = "spec:shape=Normal,width=270,height=480,unit=dp,dpi=480",
    showSystemUi = true,
)
annotation class SmallPhonePreview
